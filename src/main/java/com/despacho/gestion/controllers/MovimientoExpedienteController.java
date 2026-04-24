package com.despacho.gestion.controllers;

import com.despacho.gestion.models.MovimientoExpediente;
import com.despacho.gestion.models.Usuario;
import com.despacho.gestion.repositories.ExpedienteRepository;
import com.despacho.gestion.repositories.MovimientoExpedienteRepository;
import com.despacho.gestion.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;


@RestController
@RequestMapping("/api/expedientes/{expedienteId}/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoExpedienteController {

    @Autowired private MovimientoExpedienteRepository movimientoRepository;
    @Autowired private ExpedienteRepository expedienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll(@PathVariable Long expedienteId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return expedienteRepository.findById(expedienteId)
                .map(exp -> {
                    Page<MovimientoExpediente> movimientos =
                            movimientoRepository
                                    .findByExpedienteOrderByCreatedAtDesc(
                                            exp, PageRequest.of(page, size));
                    return ResponseEntity.ok(movimientos);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long expedienteId,
                                    @RequestBody MovimientoExpediente movimiento,
                                    Authentication authentication) {
        return expedienteRepository.findById(expedienteId)
                .map(exp -> {
                    Usuario usuario = usuarioRepository
                            .findByUsername(authentication.getName())
                            .orElseThrow();

                    movimiento.setExpediente(exp);
                    movimiento.setUsuario(usuario);
                    return ResponseEntity.ok(
                            movimientoRepository.save(movimiento));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}