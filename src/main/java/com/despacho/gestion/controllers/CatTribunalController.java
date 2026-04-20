package com.despacho.gestion.controllers;

import com.despacho.gestion.models.CatTribunal;
import com.despacho.gestion.models.TipoTribunal;
import com.despacho.gestion.repositories.CatTribunalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalogos/tribunales")
@CrossOrigin(origins = "*")
public class CatTribunalController {

    @Autowired
    private CatTribunalRepository repository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public List<CatTribunal> getAll(@RequestParam(required = false) String q) {
        List<CatTribunal> todos = repository.findByActivoTrue();
        if (q == null || q.isBlank()) {
            return todos;
        }
        String filtro = q.toLowerCase();
        return todos.stream()
                .filter(t -> t.getClave().toLowerCase().contains(filtro)
                        || t.getNombreCompleto().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatTribunal> getById(@PathVariable long id) {
        return repository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/por-tipo/{tipo}")
    public List<CatTribunal> getByTipo(@PathVariable TipoTribunal tipo) {
        return repository.findByTipoAndActivoTrue(tipo);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public CatTribunal create(@RequestBody CatTribunal tribunal) {
        return repository.save(tribunal);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<CatTribunal> update(@PathVariable long id,
            @RequestBody CatTribunal datos) {
        return repository.findById(id).map(tribunal -> {
            tribunal.setClave(datos.getClave());
            tribunal.setNombreCompleto(datos.getNombreCompleto());
            tribunal.setTipo(datos.getTipo());
            tribunal.setActivo(datos.getActivo());
            return ResponseEntity.ok(repository.save(tribunal));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        return repository.findById(id).map(tribunal -> {
            tribunal.setActivo(false);
            repository.save(tribunal);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
