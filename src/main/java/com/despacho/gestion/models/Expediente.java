package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expedientes")
@Data
public class Expediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_expediente", nullable = false, unique = true, length = 70)
    private String numeroExpediente;

    @Column(name = "sufijo_expediente", length = 10)
    private String sufijoExpediente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "junta_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CatJunta junta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tribunal_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CatTribunal tribunal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Empresa empresa;

    @Column(columnDefinition = "TEXT")
    private String litis;

    @Column(name = "proxima_audiencia")
    private LocalDate proximaAudiencia;

    @Column(columnDefinition = "TEXT")
    private String amparo;

    @Column(columnDefinition = "LONGTEXT")
    private String anotacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoExpediente estado = EstadoExpediente.ACTIVO;

    @Column(name = "fecha_recordatorio")
    private LocalDate fechaRecordatorio;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private Usuario createdBy;
}