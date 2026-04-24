package com.despacho.gestion.controllers;

import com.despacho.gestion.models.Role;
import com.despacho.gestion.models.Usuario;
import com.despacho.gestion.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    /** GET /api/usuarios/abogados → lista de abogados activos para asignación */
    @GetMapping("/abogados")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Usuario>> getAbogados() {
        return ResponseEntity.ok(
            usuarioRepository.findByRolAndActivoTrue(Role.ABOGADO)
        );
    }
}