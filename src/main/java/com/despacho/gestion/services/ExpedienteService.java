package com.despacho.gestion.services;
import com.despacho.gestion.dto.ExpedienteDTO;
import com.despacho.gestion.models.*;
import com.despacho.gestion.repositories.ClienteRepository;
import com.despacho.gestion.repositories.EmpresaRepository;
import com.despacho.gestion.repositories.ExpedienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ExpedienteService {

    @Autowired private ExpedienteRepository expedienteRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ClienteRepository clienteRepository;

    @Transactional
    public Expediente crearDesdeModuloLegal(ExpedienteDTO dto, Usuario creador) {
        Expediente expediente = new Expediente();
        
        // 1. Datos básicos del DTO (Ahora ya existen los getters)
        expediente.setNumeroExpediente(dto.getNumeroExpediente());
        expediente.setSufijoExpediente(dto.getSufijoExpediente());
        expediente.setLitis(dto.getLitis());
        expediente.setAmparo(dto.getAmparo());
        expediente.setAnotacion(dto.getAnotacion());
        expediente.setProximaAudiencia(dto.getProximaAudiencia());
        expediente.setFechaRecordatorio(dto.getFechaRecordatorio());
        
        // 2. Estado y Auditoría (Usando los nombres exactos de tu modelo)
        expediente.setEstado(EstadoExpediente.ACTIVO); 
        expediente.setCreatedBy(creador); // En tu modelo es createdBy, no creador

        // 3. Vincular Cliente (ID viene del Autocomplete)
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        expediente.setCliente(cliente);

        // 4. Lógica de Empresa (Crear si no existe por nombre)
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