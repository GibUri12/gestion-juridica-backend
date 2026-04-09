package com.despacho.gestion.controllers;

import com.despacho.gestion.dto.ExpedienteDTO;
import com.despacho.gestion.models.*;
import com.despacho.gestion.repositories.*;
import com.despacho.gestion.services.ExpedienteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expedientes")
@CrossOrigin(origins = "*")
public class ExpedienteController {

    @Autowired private ExpedienteRepository expedienteRepository;
    @Autowired private ExpedienteAbogadoRepository expedienteAbogadoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ExpedienteService expedienteService;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CatJuntaRepository juntaRepository;
    @Autowired private CatTribunalRepository tribunalRepository;


    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public List<Expediente> getAll(Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName()).orElseThrow();

        // El abogado solo ve sus expedientes asignados
        if (usuario.getRol() == Role.ABOGADO) {
            return expedienteRepository.findByAbogado(usuario);
        }

        // Admin e IT_Manager ven todos
        return expedienteRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public ResponseEntity<Expediente> getById(@PathVariable Long id) {
        return expedienteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public List<Expediente> buscar(@RequestParam String numero) {
        return expedienteRepository
                .findByNumeroExpedienteContainingIgnoreCase(numero);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Expediente> porEstado(@PathVariable EstadoExpediente estado) {
        return expedienteRepository.findByEstado(estado);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_IT_MANAGER')")
    public ResponseEntity<?> create(@RequestBody Expediente expediente,
                                     Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName()).orElseThrow();

        // Validar fecha_recordatorio >= hoy + 45 días
        if (expediente.getFechaRecordatorio() != null &&
            expediente.getFechaRecordatorio()
                .isBefore(LocalDate.now().plusDays(45))) {
            return ResponseEntity.badRequest()
                    .body("La fecha de recordatorio debe ser al menos 45 días en el futuro");
        }

        expediente.setCreatedBy(usuario);
        return ResponseEntity.ok(expedienteRepository.save(expediente));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @RequestBody Expediente datos,
                                     Authentication authentication) {
        return expedienteRepository.findById(id)
                .map(expediente -> {

                    // No se puede modificar un expediente FINALIZADO
                    if (expediente.getEstado() == EstadoExpediente.FINALIZADO) {
                        return ResponseEntity.badRequest()
                                .<Expediente>body(null);
                    }

                    // Validar fecha_recordatorio
                    if (datos.getFechaRecordatorio() != null &&
                        datos.getFechaRecordatorio()
                            .isBefore(LocalDate.now().plusDays(45))) {
                        return ResponseEntity.badRequest()
                                .<Expediente>body(null);
                    }

                    expediente.setNumeroExpediente(datos.getNumeroExpediente());
                    expediente.setSufijoExpediente(datos.getSufijoExpediente());
                    expediente.setJunta(datos.getJunta());
                    expediente.setTribunal(datos.getTribunal());
                    expediente.setCliente(datos.getCliente());
                    expediente.setEmpresa(datos.getEmpresa());
                    expediente.setLitis(datos.getLitis());
                    expediente.setProximaAudiencia(datos.getProximaAudiencia());
                    expediente.setAmparo(datos.getAmparo());
                    expediente.setAnotacion(datos.getAnotacion());
                    expediente.setEstado(datos.getEstado());
                    expediente.setFechaRecordatorio(datos.getFechaRecordatorio());

                    return ResponseEntity.ok(expedienteRepository.save(expediente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Asignar abogado a expediente
    @PostMapping("/{id}/abogados/{usuarioId}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> asignarAbogado(@PathVariable Long id,
                                            @PathVariable Long usuarioId) {
        Expediente expediente = expedienteRepository.findById(id).orElse(null);
        Usuario abogado = usuarioRepository.findById(usuarioId).orElse(null);

        if (expediente == null || abogado == null) {
            return ResponseEntity.notFound().build();
        }

        if (abogado.getRol() != Role.ABOGADO) {
            return ResponseEntity.badRequest()
                    .body("El usuario no tiene rol de ABOGADO");
        }

        ExpedienteAbogadoId pivotId = new ExpedienteAbogadoId();
        pivotId.setExpedienteId(id);
        pivotId.setUsuarioId(usuarioId);

        ExpedienteAbogado asignacion = new ExpedienteAbogado();
        asignacion.setId(pivotId);
        asignacion.setExpediente(expediente);
        asignacion.setUsuario(abogado);
        asignacion.setActivo(true);

        expedienteAbogadoRepository.save(asignacion);
        return ResponseEntity.ok().build();
    }

    // Remover abogado de expediente (soft delete)
    @DeleteMapping("/{id}/abogados/{usuarioId}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> removerAbogado(@PathVariable Long id,
                                            @PathVariable Long usuarioId) {
        ExpedienteAbogadoId pivotId = new ExpedienteAbogadoId();
        pivotId.setExpedienteId(id);
        pivotId.setUsuarioId(usuarioId);

        return expedienteAbogadoRepository.findById(pivotId)
                .map(asignacion -> {
                    asignacion.setActivo(false);
                    expedienteAbogadoRepository.save(asignacion);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Autocomplete para Empresas
    @GetMapping("/autocomplete-empresa")
    public List<Empresa> autocompleteEmpresa(@RequestParam String term) {
        return empresaRepository.findByNombreCompletoContainingIgnoreCaseAndActivoTrue(term);
    }

    // Autocomplete para Clientes
    @GetMapping("/autocomplete-cliente")
    public List<Cliente> autocompleteCliente(@RequestParam String term) {
        // Asumiendo que añadiste este método al ClienteRepository
        return clienteRepository.findByNombreCompletoContainingIgnoreCaseAndActivoTrue(term);
    }

    // PASO 1: Crear expediente (Admin e IT Manager)
    @PostMapping("/paso1")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_IT_MANAGER')")
    public ResponseEntity<Expediente> crearPaso1(@RequestBody ExpedienteDTO dto, Authentication auth) {
        Usuario creador = usuarioRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(expedienteService.crearPaso1(dto, creador));
    }
    // Nuevo Endpoint dedicado para el módulo legal
    @PostMapping("/legal")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_IT_MANAGER', 'ROLE_ABOGADO')")
    public ResponseEntity<Expediente> crearExpedienteCompleto(@RequestBody ExpedienteDTO dto, Authentication auth) {
        Usuario creador = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Llamamos a un nuevo método en el service
        Expediente nuevo = expedienteService.crearDesdeModuloLegal(dto, creador);
        return ResponseEntity.ok(nuevo);
    }

    @PutMapping("/{id}/completar")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> completarExpediente(@PathVariable Long id, @RequestBody ExpedienteDTO datos) {
        return expedienteRepository.findById(id).map(expediente -> {
            
            // 1. Validación de Estado (Evitar edición accidental de finalizados)
            if (expediente.getEstado() == EstadoExpediente.FINALIZADO && datos.getEstado() == null) {
                return ResponseEntity.badRequest().body("El expediente está finalizado y no se puede editar.");
            }

            // 2. Lógica de JUNTA (Buscar o Crear)
            if (datos.getNombreJunta() != null && !datos.getNombreJunta().isBlank()) {
                String nombreLimpio = datos.getNombreJunta().trim();
                CatJunta junta = juntaRepository.findByNombreIgnoreCase(nombreLimpio)
                    .orElseGet(() -> {
                        CatJunta nueva = new CatJunta();
                        nueva.setNombre(nombreLimpio);
                        nueva.setNumero(0); 
                        nueva.setActivo(true);
                        return juntaRepository.save(nueva);
                    });
                expediente.setJunta(junta);
            }

            // 3. Lógica de TRIBUNAL DE AMPARO (Solución al error de Nullability)
            if (Boolean.TRUE.equals(datos.getTieneAmparo())) {
                expediente.setAmparoNumero(datos.getAmparoNumero());
                expediente.setAmparoFechaAudiencia(datos.getAmparoFechaAudiencia());
                
                if (datos.getAmparoTribunalId() != null) {
                    // Si ya existe en el catálogo, solo lo vinculamos
                    tribunalRepository.findById(datos.getAmparoTribunalId())
                        .ifPresent(expediente::setAmparoTribunal);
                } else if (datos.getNombreTribunal() != null && !datos.getNombreTribunal().isBlank()) {
                    // Si es un nombre nuevo, buscamos por texto o CREAMOS
                    String nombreTCC = datos.getNombreTribunal().trim();
                    
                    CatTribunal tcc = tribunalRepository.findByNombreCompletoIgnoreCase(nombreTCC)
                        .orElseGet(() -> {
                            CatTribunal nuevoT = new CatTribunal();
                            nuevoT.setNombreCompleto(nombreTCC);
                            nuevoT.setClave("AUTO-" + System.currentTimeMillis()); // Clave única temporal
                            nuevoT.setActivo(true);
                            
                            // IMPORTANTE: Solución al error de Hibernate (CatTribunal.tipo)
                            // Si el DTO trae el tipo lo usamos, si no, usamos TRIBUNAL_FEDERAL por defecto
                            if (datos.getAmparoTribunalTipo() != null) {
                                nuevoT.setTipo(datos.getAmparoTribunalTipo());
                            } else {
                                nuevoT.setTipo(TipoTribunal.TRIBUNAL_FEDERAL);
                            }
                            
                            return tribunalRepository.save(nuevoT);
                        });
                    expediente.setAmparoTribunal(tcc);
                }
            } else {
                // Si el switch "Tiene Amparo" está apagado, limpiamos los campos
                expediente.setAmparoTribunal(null);
                expediente.setAmparoNumero(null);
                expediente.setAmparoFechaAudiencia(null);
            }

            // 4. Actualización del Estado (ACTIVO, EN_PROCESO, FINALIZADO)
            if (datos.getEstado() != null) {
                expediente.setEstado(datos.getEstado());
            }

            // 5. Actualización de campos de texto
            expediente.setLitis(datos.getLitis());
            expediente.setAmparo(datos.getAmparo()); // Notas/Estatus adicionales
            expediente.setAnotacion(datos.getAnotacion());
            expediente.setProximaAudiencia(datos.getProximaAudiencia());
            
            // 6. Validación lógica de fecha de recordatorio
            if (datos.getFechaRecordatorio() != null) {
                // Solo validamos si la fecha es distinta a la que ya tenía guardada
                if (!datos.getFechaRecordatorio().equals(expediente.getFechaRecordatorio()) &&
                    datos.getFechaRecordatorio().isBefore(LocalDate.now().plusDays(45))) {
                    return ResponseEntity.badRequest().body("El recordatorio requiere un margen mínimo de 45 días.");
                }
                expediente.setFechaRecordatorio(datos.getFechaRecordatorio());
            }

            // Guardamos el objeto actualizado
            return ResponseEntity.ok(expedienteRepository.save(expediente));
            
        }).orElse(ResponseEntity.notFound().build());
    }
    
    // Endpoint para el Autocomplete de Empresas
    @GetMapping("/buscar-empresa")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Empresa> buscarEmpresa(@RequestParam String term) {
        return empresaRepository.findByNombreCompletoContainingIgnoreCaseAndActivoTrue(term);
    }

    // Dentro de ExpedienteController.java

    @GetMapping("/catalogos/juntas")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_IT_MANAGER', 'ROLE_ABOGADO')")
    public ResponseEntity<List<CatJunta>> buscarJuntas(@RequestParam String term) {
        // Supongamos que tu repository tiene un método para buscar por nombre
        return ResponseEntity.ok(juntaRepository.findByNombreContainingIgnoreCaseAndActivoTrue(term));
    }
}
