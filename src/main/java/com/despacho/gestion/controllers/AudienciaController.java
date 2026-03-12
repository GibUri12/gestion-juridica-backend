package com.despacho.gestion.controllers;

import com.despacho.gestion.models.*;
import com.despacho.gestion.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/audiencias")
@CrossOrigin(origins = "*")
public class AudienciaController {

    @Autowired private AudienciaRepository audienciaRepository;
    @Autowired private AudienciaAbogadoRepository audienciaAbogadoRepository;
    @Autowired private ExpedienteRepository expedienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Audiencia> getAll(Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName()).orElseThrow();

        if (usuario.getRol() == Role.ABOGADO) {
            return audienciaRepository.findProgramadasByAbogado(usuario);
        }

        return audienciaRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public ResponseEntity<Audiencia> getById(@PathVariable Long id) {
        return audienciaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Audiencia> porFecha(@PathVariable LocalDate fecha) {
        return audienciaRepository.findByFecha(fecha);
    }

    @GetMapping("/rango")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Audiencia> porRango(@RequestParam LocalDate inicio,
                                     @RequestParam LocalDate fin) {
        return audienciaRepository.findByFechaBetween(inicio, fin);
    }

    @GetMapping("/expediente/{expedienteId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public ResponseEntity<?> porExpediente(@PathVariable Long expedienteId) {
        return expedienteRepository.findById(expedienteId)
                .map(exp -> ResponseEntity.ok(
                        audienciaRepository.findByExpediente(exp)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> create(@RequestBody Audiencia audiencia,
                                     Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName()).orElseThrow();

        // No se pueden crear audiencias en expedientes FINALIZADOS
        if (audiencia.getExpediente() != null) {
            Expediente exp = expedienteRepository
                    .findById(audiencia.getExpediente().getId())
                    .orElse(null);
            if (exp != null &&
                exp.getEstado() == EstadoExpediente.FINALIZADO) {
                return ResponseEntity.badRequest()
                        .body("No se pueden agregar audiencias a un expediente FINALIZADO");
            }
        }

        audiencia.setCreatedBy(usuario);
        return ResponseEntity.ok(audienciaRepository.save(audiencia));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @RequestBody Audiencia datos,
                                     Authentication authentication) {
        return audienciaRepository.findById(id)
                .map(audiencia -> {
                    audiencia.setFecha(datos.getFecha());
                    audiencia.setHora(datos.getHora());
                    audiencia.setTipoAudiencia(datos.getTipoAudiencia());
                    audiencia.setTribunal(datos.getTribunal());
                    audiencia.setEstado(datos.getEstado());
                    audiencia.setResultado(datos.getResultado());
                    audiencia.setNotas_tipo(datos.getNotas_tipo());
                    audiencia.setNotasAgenda(datos.getNotasAgenda());
                    audiencia.setEsVirtual(datos.getEsVirtual());
                    audiencia.setAudienciaPadre(datos.getAudienciaPadre());
                    return ResponseEntity.ok(audienciaRepository.save(audiencia));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Asignar abogado a audiencia
    @PostMapping("/{id}/abogados/{usuarioId}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<?> asignarAbogado(@PathVariable Long id,
                                              @PathVariable Long usuarioId,
                                              @RequestParam(defaultValue = "true")
                                              Boolean esTitular) {
        Audiencia audiencia = audienciaRepository.findById(id).orElse(null);
        Usuario abogado = usuarioRepository.findById(usuarioId).orElse(null);

        if (audiencia == null || abogado == null) {
            return ResponseEntity.notFound().build();
        }

        if (abogado.getRol() != Role.ABOGADO) {
            return ResponseEntity.badRequest()
                    .body("El usuario no tiene rol de ABOGADO");
        }

        // Máximo 2 abogados por audiencia
        List<AudienciaAbogado> actuales =
                audienciaAbogadoRepository.findByAudiencia(audiencia);
        if (actuales.size() >= 2) {
            return ResponseEntity.badRequest()
                    .body("Una audiencia no puede tener más de 2 abogados");
        }

        AudienciaAbogadoId pivotId = new AudienciaAbogadoId();
        pivotId.setAudienciaId(id);
        pivotId.setUsuarioId(usuarioId);

        AudienciaAbogado asignacion = new AudienciaAbogado();
        asignacion.setId(pivotId);
        asignacion.setAudiencia(audiencia);
        asignacion.setUsuario(abogado);
        asignacion.setEsTitular(esTitular);

        audienciaAbogadoRepository.save(asignacion);
        return ResponseEntity.ok().build();
    }

    // Registrar resultado de audiencia
    @PatchMapping("/{id}/resultado")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public ResponseEntity<?> registrarResultado(@PathVariable Long id,
                                            @RequestBody String resultado) {
        return audienciaRepository.findById(id)
                .map(audiencia -> {
                    audiencia.setResultado(resultado);
                    audiencia.setEstado(EstadoAudiencia.REALIZADA);
                    return ResponseEntity.ok(audienciaRepository.save(audiencia));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
