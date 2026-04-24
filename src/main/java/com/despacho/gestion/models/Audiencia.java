package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "audiencias")
@Data
public class Audiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    private CatTipoAudiencia tipoAudiencia;

    @ManyToOne(fetch = FetchType.LAZY)
    private CatTribunal tribunal;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column
    private LocalTime hora;

    @Column(columnDefinition = "TEXT")
    private String resultado;

    @Column(name = "notas_tipo", columnDefinition = "TEXT")
    private String notasTipo;

    @Column(name = "notas_agenda", columnDefinition = "TEXT")
    private String notasAgenda;

    @Column(name = "es_virtual", nullable = false)
    private Boolean esVirtual = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAudiencia estado = EstadoAudiencia.PROGRAMADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"abogados", "expediente", "audienciaPadre"})
    private Audiencia audienciaPadre;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", columnDefinition = "INT",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Usuario createdBy;


    @OneToMany(mappedBy = "audiencia", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<AudienciaAbogado> abogados;
}
