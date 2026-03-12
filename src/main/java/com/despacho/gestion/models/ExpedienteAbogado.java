package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "expediente_abogados")
@Data
public class ExpedienteAbogado {

    @EmbeddedId
    private ExpedienteAbogadoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("expedienteId")
    @JoinColumn(name = "expediente_id")
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", updatable = false)
    private Instant fechaAsignacion;

    @Column(nullable = false)
    private Boolean activo = true;
}
