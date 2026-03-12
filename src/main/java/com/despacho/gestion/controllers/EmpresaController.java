package com.despacho.gestion.controllers;

import com.despacho.gestion.models.Empresa;
import com.despacho.gestion.repositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaController {

    @Autowired
    private EmpresaRepository repository;

    @GetMapping
    public List<Empresa> getAll() {
        return repository.findByActivoTrue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Empresa> buscar(@RequestParam String nombre) {
        return repository
                .findByNombreCompletoContainingIgnoreCaseAndActivoTrue(nombre);
    }

    @PostMapping
    public Empresa create(@RequestBody Empresa empresa) {
        return repository.save(empresa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> update(@PathVariable Long id,
                                        @RequestBody Empresa datos) {
        return repository.findById(id)
                .map(empresa -> {
                    empresa.setNombreCompleto(datos.getNombreCompleto());
                    empresa.setActivo(datos.getActivo());
                    return ResponseEntity.ok(repository.save(empresa));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repository.findById(id)
                .map(empresa -> {
                    empresa.setActivo(false);
                    repository.save(empresa);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
