package com.despacho.gestion.controllers;

import com.despacho.gestion.dto.AudienciaDTO;
import com.despacho.gestion.dto.AudienciaRequestDTO;

import com.despacho.gestion.models.EstadoAudiencia;
import com.despacho.gestion.services.AudienciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/audiencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AudienciaController {

    private final AudienciaService audienciaService;

    /** GET /api/audiencias → listado con filtros opcionales */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<AudienciaDTO>> getAll(@RequestParam(required = false) String estado,
            @RequestParam(required = false) Long abogadoId,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        return ResponseEntity
                .ok(audienciaService.findAll(estado, abogadoId, fechaDesde, fechaHasta));
    }

    /** GET /api/audiencias/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ABOGADO')")
    public ResponseEntity<AudienciaDTO> getById(@PathVariable Long id) {
        return audienciaService.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/audiencias → crear audiencia */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AudienciaDTO> crear(@RequestBody AudienciaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(audienciaService.crear(request));
    }

    /** PUT /api/audiencias/{id} → editar audiencia */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AudienciaDTO> editar(@PathVariable Long id,
            @RequestBody AudienciaRequestDTO request) {
        return ResponseEntity.ok(audienciaService.editar(id, request));
    }

    /** PATCH /api/audiencias/{id}/estado → cambiar estado */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AudienciaDTO> cambiarEstado(@PathVariable Long id,
            @RequestParam EstadoAudiencia estado) {
        return ResponseEntity.ok(audienciaService.cambiarEstado(id, estado));
    }

    /** PATCH /api/audiencias/{id}/resultado → abogado registra resultado */
    @PatchMapping("/{id}/resultado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ABOGADO')")
    public ResponseEntity<AudienciaDTO> registrarResultado(@PathVariable Long id,
            @RequestBody ResultadoRequest request) {
        return ResponseEntity.ok(audienciaService.registrarResultado(id, request.resultado()));
    }

    record ResultadoRequest(String resultado) {
    }
}
