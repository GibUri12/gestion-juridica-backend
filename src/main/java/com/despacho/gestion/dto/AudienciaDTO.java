package com.despacho.gestion.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AudienciaDTO {

    private Long   id;
    private String estado;
    private LocalDate fecha;
    private LocalTime hora;
    private String resultado;

    // Expediente — solo lo que la tabla necesita mostrar
    private Long   expedienteId;
    private String expedienteNumero;
    private String clienteNombre;
    private String empresaNombre;

    // Catálogos
    private Long   tipoAudienciaId;
    private String tipoAudienciaAbreviatura;
    private String tipoAudienciaDescripcion;

    private Long   tribunalId;
    private String tribunalClave;
    private String tribunalNombre;

    // Abogados asignados
    private List<AbogadoAudienciaDTO> abogados;

    // Audiencia padre (seguimiento)
    private Long   audienciaPadreId;
    private String audienciaPadreFecha; // solo la fecha, no todo el objeto

    // ── DTO interno para abogados ──────────────────────────────────
    @Data
    public static class AbogadoAudienciaDTO {
        private Long    usuarioId;
        private String  nombreCompleto;
        private String  claveAbogado;
        private boolean esTitular;
    }
}