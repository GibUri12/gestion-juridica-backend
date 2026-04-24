package com.despacho.gestion.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "audiencia_abogados")
@Data
public class AudienciaAbogado {

    @EmbeddedId
    private AudienciaAbogadoId id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("audienciaId")
    @JoinColumn(name = "audiencia_id")
    private Audiencia audiencia;

    @ManyToOne(fetch = FetchType.EAGER) 
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "es_titular", nullable = false)
    private Boolean esTitular = true;
}