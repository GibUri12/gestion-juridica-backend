package com.despacho.gestion.services;

import com.despacho.gestion.models.Cliente;
import com.despacho.gestion.models.Usuario;
import com.despacho.gestion.repositories.ClienteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final com.despacho.gestion.repositories.UsuarioRepository usuarioRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          com.despacho.gestion.repositories.UsuarioRepository usuarioRepository) {
        this.clienteRepository  = clienteRepository;
        this.usuarioRepository  = usuarioRepository;
    }

    public List<Cliente> findAll(boolean soloActivos) {
        if (soloActivos) return clienteRepository.findByActivoTrueOrderByNombreCompleto();
        return clienteRepository.findAllByOrderByNombreCompleto();
    }

    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente crear(Cliente cliente) {
        cliente.setActivo(true);
        cliente.setNombreCompleto(cliente.getNombreCompleto().toUpperCase().trim());
        cliente.setCreatedBy(usuarioActual());
        return clienteRepository.save(cliente);
    }

    public Cliente editar(Long id, Cliente datos) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
        existente.setNombreCompleto(datos.getNombreCompleto().toUpperCase().trim());
        existente.setTelefono(datos.getTelefono());
        existente.setEmail(datos.getEmail());
        existente.setNotas(datos.getNotas());
        return clienteRepository.save(existente);
    }

    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("No hay sesión activa");
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + auth.getName()));
    }
}