package com.despacho.gestion.controllers;

import com.despacho.gestion.models.CatJunta;
import com.despacho.gestion.repositories.CatJuntaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/juntas")
@CrossOrigin(origins = "*")
public class CatJuntaController {

    @Autowired
    private CatJuntaRepository repository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public List<CatJunta> getAll() {
        return repository.findByActivoTrue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatJunta> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public CatJunta create(@RequestBody CatJunta junta) {
        return repository.save(junta);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<CatJunta> update(@PathVariable Integer id,
                                            @RequestBody CatJunta datos) {
        return repository.findById(id)
                .map(junta -> {
                    junta.setNombre(datos.getNombre());
                    junta.setDescripcion(datos.getDescripcion());
                    junta.setActivo(datos.getActivo());
                    return ResponseEntity.ok(repository.save(junta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return repository.findById(id)
                .map(junta -> {
                    junta.setActivo(false);
                    repository.save(junta);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
