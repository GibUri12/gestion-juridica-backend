package com.despacho.gestion.services;
import com.despacho.gestion.dto.ExpedienteDTO;
import com.despacho.gestion.models.*;
import com.despacho.gestion.repositories.CatJuntaRepository;
import com.despacho.gestion.repositories.ClienteRepository;
import com.despacho.gestion.repositories.EmpresaRepository;
import com.despacho.gestion.repositories.ExpedienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.despacho.gestion.repositories.CatTribunalRepository;


@Service
public class ExpedienteService {

    @Autowired private ExpedienteRepository expedienteRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CatJuntaRepository juntaRepository;
    @Autowired private CatTribunalRepository tribunalRepository;


    @Transactional
    public Expediente crearDesdeModuloLegal(ExpedienteDTO dto, Usuario creador) {
        Expediente expediente = new Expediente();
        
        // 1. Datos básicos del Juicio Principal
        expediente.setNumeroExpediente(dto.getNumeroExpediente());
        expediente.setSufijoExpediente(dto.getSufijoExpediente());
        expediente.setLitis(dto.getLitis());
        expediente.setAnotacion(dto.getAnotacion());
        expediente.setProximaAudiencia(dto.getProximaAudiencia());
        expediente.setFechaRecordatorio(dto.getFechaRecordatorio());

        // 2. Lógica para JUNTA / LOCALIDAD (Juicio Principal)
        if (dto.getNombreJunta() != null && !dto.getNombreJunta().isBlank()) {
            String nombreLimpio = dto.getNombreJunta().trim();
            CatJunta junta = juntaRepository.findByNombreIgnoreCase(nombreLimpio)
                .orElseGet(() -> {
                    CatJunta nuevaJunta = new CatJunta();
                    nuevaJunta.setNombre(nombreLimpio);
                    nuevaJunta.setNumero(0); 
                    nuevaJunta.setActivo(true);
                    return juntaRepository.saveAndFlush(nuevaJunta); 
                });
            expediente.setJunta(junta);
        }
        
        // 3. Lógica de AMPARO (Estructura Espejo)
        if (Boolean.TRUE.equals(dto.getTieneAmparo())) {
            expediente.setAmparoNumero(dto.getAmparoNumero());
            expediente.setAmparoFechaAudiencia(dto.getAmparoFechaAudiencia());
            expediente.setAmparo(dto.getAmparo()); 

            // --- LÓGICA CORREGIDA PARA TRIBUNAL ---
            if (dto.getAmparoTribunalId() != null) {
                tribunalRepository.findById(dto.getAmparoTribunalId())
                    .ifPresent(expediente::setAmparoTribunal);
            } else if (dto.getNombreTribunal() != null && !dto.getNombreTribunal().isBlank()) {
                String nombreTCC = dto.getNombreTribunal().trim();
                
                // Usamos findByNombreCompletoIgnoreCase (asegúrate que devuelva Optional o maneja la lista)
                // Aquí lo manejamos como Optional según la lógica previa
                CatTribunal tribunal = tribunalRepository.findByNombreCompletoIgnoreCase(nombreTCC)
                    .orElseGet(() -> {
                        CatTribunal nuevo = new CatTribunal();
                        nuevo.setNombreCompleto(nombreTCC);
                        // Clave única para evitar errores de Constraint Violation
                        nuevo.setClave("AUTO-" + System.currentTimeMillis()); 
                        nuevo.setActivo(true);
                        
                        // ASIGNACIÓN DEL TIPO (Crucial para evitar PropertyValueException)
                        if (dto.getAmparoTribunalTipo() != null) {
                            nuevo.setTipo(dto.getAmparoTribunalTipo());
                        } else {
                            // Fallback por seguridad
                            nuevo.setTipo(TipoTribunal.TRIBUNAL_FEDERAL);
                        }
                        
                        return tribunalRepository.save(nuevo);
                    });
                expediente.setAmparoTribunal(tribunal);
            }
        }

        // 4. Estado y Auditoría
        expediente.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoExpediente.ACTIVO);
        expediente.setCreatedBy(creador);

        // 5. Vincular Cliente
        if (dto.getClienteId() == null) {
            throw new RuntimeException("El ID del cliente es obligatorio para crear el expediente");
        }
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId()));
        expediente.setCliente(cliente);

        // 6. Lógica de Empresa (Crear si no existe)
        if (dto.getNombreEmpresa() == null || dto.getNombreEmpresa().isBlank()) {
            throw new RuntimeException("El nombre de la empresa es obligatorio");
        }
        
        String nombreEmp = dto.getNombreEmpresa().trim();
        Empresa empresa = empresaRepository.findByNombreCompletoIgnoreCase(nombreEmp)
                .orElseGet(() -> {
                    Empresa nueva = new Empresa();
                    nueva.setNombreCompleto(nombreEmp);
                    nueva.setActivo(true);
                    return empresaRepository.save(nueva);
                });
        expediente.setEmpresa(empresa);

        return expedienteRepository.save(expediente);
    }

    @Transactional
    public Expediente crearPaso1(ExpedienteDTO dto, Usuario creador) {
        Expediente expediente = new Expediente();
        expediente.setNumeroExpediente(dto.getNumeroExpediente());
        expediente.setSufijoExpediente(dto.getSufijoExpediente());
        expediente.setJunta(dto.getJunta()); // Viene del dropdown de 19 juntas
        
        // Asignar Cliente (Autocomplete ya validado desde el Front)
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        expediente.setCliente(cliente);

        // Lógica de Empresa (Autocomplete + Creación)
        Empresa empresa = empresaRepository.findByNombreCompletoIgnoreCase(dto.getNombreEmpresa())
                .orElseGet(() -> {
                    Empresa nueva = new Empresa();
                    nueva.setNombreCompleto(dto.getNombreEmpresa());
                    nueva.setActivo(true);
                    return empresaRepository.save(nueva);
                });
        
        expediente.setEmpresa(empresa);
        expediente.setEstado(EstadoExpediente.ACTIVO);
        expediente.setCreatedBy(creador);

        return expedienteRepository.save(expediente);
    }
}