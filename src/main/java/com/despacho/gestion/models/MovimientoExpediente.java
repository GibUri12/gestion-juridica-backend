package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@Entity
@Table(name = "movimientos_expediente")
@Data
public class MovimientoExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "cliente", "empresa", "junta"})
    private Expediente expediente;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}