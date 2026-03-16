package com.despacho.gestion.controllers;

import com.despacho.gestion.models.Cliente;
import com.despacho.gestion.services.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClienteController {

    private final ClienteService clienteService;

    /** GET /api/clientes?activo=true  → lista clientes */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'IT_MANAGER')")
    public ResponseEntity<List<Cliente>> getAll(
            @RequestParam(defaultValue = "true") boolean activo) {
        return ResponseEntity.ok(clienteService.findAll(activo));
    }

    /** GET /api/clientes/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'IT_MANAGER')")
    public ResponseEntity<Cliente> getById(@PathVariable Long id) {
        return clienteService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/clientes */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'IT_MANAGER')")
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        Cliente nuevo = clienteService.crear(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    /** PUT /api/clientes/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'IT_MANAGER')")
    public ResponseEntity<Cliente> editar(
            @PathVariable Long id,
            @RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.editar(id, cliente));
    }

    /** DELETE /api/clientes/{id}  → soft-delete (activo = false) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}