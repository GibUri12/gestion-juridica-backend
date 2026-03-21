package com.despacho.gestion.dto;

import com.despacho.gestion.models.CatJunta;
import lombok.Data;
import java.time.LocalDate;

@Data // Lombok generará los getters y setters automáticamente
public class ExpedienteDTO {
    // Campos que ya tenías (se mantienen para no romper el editar)
    private String numeroExpediente;
    private String sufijoExpediente;
    private Long clienteId;
    private String nombreEmpresa; 
    private CatJunta junta;

    // --- NUEVOS CAMPOS PARA EL MÓDULO LEGAL ---
    private String nombreCliente; // Para recibir el nombre desde el front si es necesario
    private String litis;
    private String amparo;
    private String anotacion;
    private LocalDate proximaAudiencia;
    private LocalDate fechaRecordatorio;
}