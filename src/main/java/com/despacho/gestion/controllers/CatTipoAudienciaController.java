package com.despacho.gestion.controllers;

import com.despacho.gestion.models.CatTipoAudiencia;
import com.despacho.gestion.repositories.CatTipoAudienciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-audiencia")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CatTipoAudienciaController {

    private final CatTipoAudienciaRepository repository;

    /** GET /api/tipos-audiencia?q=texto → autocomplete */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CatTipoAudiencia>> buscar(
            @RequestParam(required = false, defaultValue = "") String q) {
        if (q.isBlank()) {
            return ResponseEntity.ok(
                repository.findByActivoTrueOrderByDescripcion()
                    .stream().limit(20).toList()
            );
        }
        return ResponseEntity.ok(
            repository.findByDescripcionContainingIgnoreCaseAndActivoTrueOrderByDescripcion(q)
                .stream().limit(10).toList()
        );
    }
}