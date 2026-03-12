package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "audiencias")
@Data
public class Audiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_audiencia_id", nullable = false)
    private CatTipoAudiencia tipoAudiencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tribunal_id", nullable = false)
    private CatTribunal tribunal;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column
    private LocalTime hora;

    @Column(columnDefinition = "TEXT")
    private String resultado;

    @Column(name = "notas_tipo", columnDefinition = "TEXT")
    private String notas_tipo;

    @Column(name = "notas_agenda", columnDefinition = "TEXT")
    private String notasAgenda;

    @Column(name = "es_virtual", nullable = false)
    private Boolean esVirtual = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAudiencia estado = EstadoAudiencia.PROGRAMADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audiencia_padre_id")
    private Audiencia audienciaPadre;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Usuario createdBy;
}