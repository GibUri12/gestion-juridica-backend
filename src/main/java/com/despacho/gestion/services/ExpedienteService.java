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
        // Usamos el campo 'amparo' de la entidad para guardar las 'amparoNotas' del DTO
        if (Boolean.TRUE.equals(dto.getTieneAmparo())) {
            expediente.setAmparoNumero(dto.getAmparoNumero());
            expediente.setAmparoFechaAudiencia(dto.getAmparoFechaAudiencia());
            expediente.setAmparo(dto.getAmparo()); // Notas o Estatus (ej. PONENCIA)
            
            if (dto.getAmparoTribunalId() != null) {
                // Buscamos el T.C.C. en la tabla de tribunales
                tribunalRepository.findById(dto.getAmparoTribunalId())
                    .ifPresent(expediente::setAmparoTribunal);
            }
        } else {
            // Si no tiene amparo, nos aseguramos de limpiar los campos por seguridad
            expediente.setAmparoNumero(null);
            expediente.setAmparoFechaAudiencia(null);
            expediente.setAmparo(null);
            expediente.setAmparoTribunal(null);
        }
        
        // 4. Estado y Auditoría
        expediente.setEstado(EstadoExpediente.ACTIVO); 
        expediente.setCreatedBy(creador);

        // 5. Vincular Cliente (ID viene del Autocomplete)
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        expediente.setCliente(cliente);

        // 6. Lógica de Empresa (Crear si no existe por nombre)
        if (dto.getNombreEmpresa() == null || dto.getNombreEmpresa().isEmpty()) {
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