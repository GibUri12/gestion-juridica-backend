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

    // PASO 2: Editar/Completar (Solo Admin)
    @PutMapping("/{id}/completar")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> completarExpediente(@PathVariable Long id, @RequestBody Expediente datos) {
        return expedienteRepository.findById(id).map(expediente -> {
            // No editar si está FINALIZADO
            if (expediente.getEstado() == EstadoExpediente.FINALIZADO) {
                return ResponseEntity.badRequest().body("Expediente finalizado no editable");
            }

            // Actualizar campos del Paso 2
            expediente.setLitis(datos.getLitis());
            expediente.setAmparo(datos.getAmparo());
            expediente.setAnotacion(datos.getAnotacion());
            expediente.setProximaAudiencia(datos.getProximaAudiencia());
            expediente.setTribunal(datos.getTribunal());
            expediente.setEstado(datos.getEstado());
            
            // Validar fecha recordatorio (45 días) si se proporciona
            if (datos.getFechaRecordatorio() != null) {
                if (datos.getFechaRecordatorio().isBefore(LocalDate.now().plusDays(45))) {
                    return ResponseEntity.badRequest().body("El recordatorio requiere mín. 45 días");
                }
                expediente.setFechaRecordatorio(datos.getFechaRecordatorio());
            }

            return ResponseEntity.ok(expedienteRepository.save(expediente));
        }).orElse(ResponseEntity.notFound().build());
    }
    // Endpoint para el Autocomplete de Empresas
    @GetMapping("/buscar-empresa")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Empresa> buscarEmpresa(@RequestParam String term) {
        return empresaRepository.findByNombreCompletoContainingIgnoreCaseAndActivoTrue(term);
    }
}
