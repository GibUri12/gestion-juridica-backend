package com.despacho.gestion.models;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class ExpedienteAbogadoId implements Serializable {
    private Long expedienteId;
    private Long usuarioId;
}