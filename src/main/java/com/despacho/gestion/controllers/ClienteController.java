package com.despacho.gestion.controllers;

import com.despacho.gestion.models.Cliente;
import com.despacho.gestion.models.Usuario;
import com.despacho.gestion.repositories.ClienteRepository;
import com.despacho.gestion.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public List<Cliente> getAll() {
        return clienteRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public ResponseEntity<Cliente> getById(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_ABOGADO', 'ROLE_IT_MANAGER')")
    public List<Cliente> buscar(@RequestParam String nombre) {
        return clienteRepository.findByNombreCompletoContainingIgnoreCase(nombre);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_IT_MANAGER')")
    public ResponseEntity<?> create(@RequestBody Cliente cliente,
                                    Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        cliente.setCreatedBy(usuario);
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Cliente> update(@PathVariable Long id,
                                        @RequestBody Cliente datos,
                                        Authentication authentication) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setNombreCompleto(datos.getNombreCompleto());
                    cliente.setTelefono(datos.getTelefono());
                    cliente.setEmail(datos.getEmail());
                    cliente.setNotas(datos.getNotas());
                    return ResponseEntity.ok(clienteRepository.save(cliente));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}