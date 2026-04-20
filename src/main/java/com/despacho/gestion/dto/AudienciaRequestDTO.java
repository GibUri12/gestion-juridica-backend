package com.despacho.gestion.dto;

import lombok.Data;
import java.util.List;

@Data
public class AudienciaRequestDTO {

    private Long        expedienteId;
    private Long        tipoAudienciaId;
    private Long        tribunalId;
    private String      fecha;           // "yyyy-MM-dd"
    private String      hora;            // "HH:mm" opcional
    private List<Long>  abogadoIds;
    private Long        abogadoTitularId;
    private Long        audienciaPadreId; // opcional, para audiencias de seguimiento
}