package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "audiencia_abogados")
@Data
public class AudienciaAbogado {

    @EmbeddedId
    private AudienciaAbogadoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("audienciaId")
    @JoinColumn(name = "audiencia_id")
    private Audiencia audiencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "es_titular", nullable = false)
    private Boolean esTitular = true;
}