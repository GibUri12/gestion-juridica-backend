package com.despacho.gestion.controllers;

import com.despacho.gestion.models.CatTipoAudiencia;
import com.despacho.gestion.repositories.CatTipoAudienciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/tipos-audiencia")
@CrossOrigin(origins = "*")
public class CatTipoAudienciaController {

    @Autowired
    private CatTipoAudienciaRepository repository;

    @GetMapping
    public List<CatTipoAudiencia> getAll() {
        return repository.findByActivoTrue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatTipoAudiencia> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CatTipoAudiencia create(@RequestBody CatTipoAudiencia tipo) {
        return repository.save(tipo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatTipoAudiencia> update(@PathVariable Integer id,
                                                    @RequestBody CatTipoAudiencia datos) {
        return repository.findById(id)
                .map(tipo -> {
                    tipo.setAbreviatura(datos.getAbreviatura());
                    tipo.setDescripcion(datos.getDescripcion());
                    tipo.setActivo(datos.getActivo());
                    return ResponseEntity.ok(repository.save(tipo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return repository.findById(id)
                .map(tipo -> {
                    tipo.setActivo(false);
                    repository.save(tipo);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}