package com.despacho.gestion.services;
import com.despacho.gestion.dto.ExpedienteDTO;
import com.despacho.gestion.models.*;
import com.despacho.gestion.repositories.CatJuntaRepository;
import com.despacho.gestion.repositories.ClienteRepository;
import com.despacho.gestion.repositories.EmpresaRepository;
import com.despacho.gestion.repositories.ExpedienteRepository;
import com.despacho.gestion.repositories.MovimientoExpedienteRepository;

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
    @Autowired private MovimientoExpedienteRepository movimientoRepository;


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
                
                CatTribunal tribunal = tribunalRepository.findByNombreCompletoIgnoreCase(nombreTCC)
                    .orElseGet(() -> {
                        CatTribunal nuevo = new CatTribunal();
                        nuevo.setNombreCompleto(nombreTCC);
                        nuevo.setClave("AUTO-" + System.currentTimeMillis()); 
                        nuevo.setActivo(true);
                        
                        if (dto.getAmparoTribunalTipo() != null) {
                            nuevo.setTipo(dto.getAmparoTribunalTipo());
                        } else {
                            nuevo.setTipo(TipoTribunal.TRIBUNAL_FEDERAL);
                        }
                        return tribunalRepository.save(nuevo);
                    });
                expediente.setAmparoTribunal(tribunal);
            }
        }

        // 4. Estado y Auditoría interna del objeto
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

        // 7. Guardar el expediente primero para obtener el ID
        Expediente expedienteGuardado = expedienteRepository.save(expediente);

        // 8. REGISTRO INICIAL EN EL HISTORIAL (Movimientos)
        MovimientoExpediente movimientoInicial = new MovimientoExpediente();
        movimientoInicial.setExpediente(expedienteGuardado);
        movimientoInicial.setUsuario(creador);
        movimientoInicial.setDescripcion("Creación del expediente. Registro inicial de datos en el Módulo Legal.");
        movimientoRepository.save(movimientoInicial);

        return expedienteGuardado;
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

    @Transactional
    public Expediente completarOActualizar(Long id, ExpedienteDTO dto, Usuario editor) {
        Expediente actual = expedienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));

        StringBuilder cambios = new StringBuilder("Detalle de modificaciones:\n");
        boolean huboCambios = false;

        // 1. Estado
        if (dto.getEstado() != null && !actual.getEstado().equals(dto.getEstado())) {
            cambios.append(String.format("• Estado: [%s] → [%s]\n", actual.getEstado(), dto.getEstado()));
            actual.setEstado(dto.getEstado());
            huboCambios = true;
        }

        // 2. Litis
        if (debioCambiarTexto(actual.getLitis(), dto.getLitis())) {
            cambios.append(String.format("• Litis: [%s] → [%s]\n", formatearNulo(actual.getLitis()), dto.getLitis()));
            actual.setLitis(dto.getLitis());
            huboCambios = true;
        }

        // 3. Anotaciones Generales
        if (debioCambiarTexto(actual.getAnotacion(), dto.getAnotacion())) {
           cambios.append(String.format("• Anotaciones: [%s] → [%s]\n", formatearNulo(actual.getAnotacion()), dto.getAnotacion()));
            actual.setAnotacion(dto.getAnotacion());
            huboCambios = true;
        }

        // 4. No. de Amparo
        if (debioCambiarTexto(actual.getAmparoNumero(), dto.getAmparoNumero())) {
            cambios.append(String.format("• No. Amparo: [%s] → [%s]\n", formatearNulo(actual.getAmparoNumero()), dto.getAmparoNumero()));
            actual.setAmparoNumero(dto.getAmparoNumero());
            huboCambios = true;
        }

        // 5. Fecha Audiencia Constitucional (Amparo)
        String fechaAmpAnt = actual.getAmparoFechaAudiencia() != null ? actual.getAmparoFechaAudiencia().toString() : "Sin fecha";
        String fechaAmpNva = dto.getAmparoFechaAudiencia() != null ? dto.getAmparoFechaAudiencia().toString() : "Sin fecha";
        if (!fechaAmpAnt.equals(fechaAmpNva)) {
            cambios.append(String.format("• F. Audiencia Amparo: [%s] → [%s]\n", fechaAmpAnt, fechaAmpNva));
            actual.setAmparoFechaAudiencia(dto.getAmparoFechaAudiencia());
            huboCambios = true;
        }

        // 6. Próxima Audiencia (Principal)
        String fechaAudAnt = actual.getProximaAudiencia() != null ? actual.getProximaAudiencia().toString() : "Sin fecha";
        String fechaAudNva = dto.getProximaAudiencia() != null ? dto.getProximaAudiencia().toString() : "Sin fecha";
        if (!fechaAudAnt.equals(fechaAudNva)) {
            cambios.append(String.format("• Próxima Audiencia: [%s] → [%s]\n", fechaAudAnt, fechaAudNva));
            actual.setProximaAudiencia(dto.getProximaAudiencia());
            huboCambios = true;
        }

        // 7. Estatus / Recursos Amparo (Campo amparo en la entidad)
        if (debioCambiarTexto(actual.getAmparo(), dto.getAmparo())) {
            cambios.append(String.format("•Recursos Amparo: [%s] → [%s]\n", formatearNulo(actual.getAmparo()), dto.getAmparo()));
            actual.setAmparo(dto.getAmparo());
            huboCambios = true;
        }

        // 8. Tribunal Colegiado (T.C.C.)
        Long idTribAnt = actual.getAmparoTribunal() != null ? actual.getAmparoTribunal().getId() : null;
        if (dto.getAmparoTribunalId() != null && !dto.getAmparoTribunalId().equals(idTribAnt)) {
            CatTribunal nuevoTrib = tribunalRepository.findById(dto.getAmparoTribunalId()).orElse(null);
            if (nuevoTrib != null) {
                String nombreAnt = actual.getAmparoTribunal() != null ? actual.getAmparoTribunal().getNombreCompleto() : "Ninguno";
                cambios.append(String.format("• Tribunal: [%s] → [%s]\n", nombreAnt, nuevoTrib.getNombreCompleto()));
                actual.setAmparoTribunal(nuevoTrib);
                huboCambios = true;
            }
        }

        // --- GUARDADO ---
        if (huboCambios) {
            Expediente guardado = expedienteRepository.save(actual);
            MovimientoExpediente mov = new MovimientoExpediente();
            mov.setExpediente(guardado);
            mov.setUsuario(editor);
            mov.setDescripcion(cambios.toString());
            movimientoRepository.save(mov);
            return guardado;
        }

        return actual;
    }

    // Funciones auxiliares para limpiar el código
    private boolean debioCambiarTexto(String actual, String nuevo) {
        if (nuevo == null) return false;
        String valActual = (actual == null) ? "" : actual.trim();
        return !valActual.equals(nuevo.trim());
    }

    private String formatearNulo(String texto) {
        return (texto == null || texto.isBlank()) ? "Vacío" : texto;
    }
}