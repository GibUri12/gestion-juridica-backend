package com.despacho.gestion.models;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class AudienciaAbogadoId implements Serializable {
    private Long audienciaId;
    private Long usuarioId;
}