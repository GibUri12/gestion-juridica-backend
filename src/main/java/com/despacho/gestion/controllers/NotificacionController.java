package com.despacho.gestion.controllers;

import com.despacho.gestion.models.Notificacion;
import com.despacho.gestion.models.Usuario;
import com.despacho.gestion.repositories.NotificacionRepository;
import com.despacho.gestion.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // Todas las notificaciones del usuario autenticado
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Notificacion> getMias(Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName()).orElseThrow();
        return notificacionRepository
                .findByUsuarioDestinatarioOrderByCreatedAtDesc(usuario);
    }

    // Solo las no leídas
    @GetMapping("/no-leidas")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public List<Notificacion> getNoLeidas(Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName()).orElseThrow();
        return notificacionRepository
                .findByUsuarioDestinatarioAndLeidaFalse(usuario);
    }

    // Marcar una notificación como leída
    @PatchMapping("/{id}/leer")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id,
                                        Authentication authentication) {
        return notificacionRepository.findById(id)
                .map(notificacion -> {
                    notificacion.setLeida(true);
                    notificacion.setFechaLectura(Instant.now());
                    return ResponseEntity.ok(
                            notificacionRepository.save(notificacion));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Marcar todas como leídas
    @PatchMapping("/leer-todas")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO')")
    public ResponseEntity<?> marcarTodasLeidas(Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName()).orElseThrow();

        List<Notificacion> noLeidas = notificacionRepository
                .findByUsuarioDestinatarioAndLeidaFalse(usuario);

        noLeidas.forEach(n -> {
            n.setLeida(true);
            n.setFechaLectura(Instant.now());
        });

        notificacionRepository.saveAll(noLeidas);
        return ResponseEntity.ok().build();
    }
}
