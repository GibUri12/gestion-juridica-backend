package com.despacho.gestion.dto;

import com.despacho.gestion.models.CatJunta;
import lombok.Data;
import java.time.LocalDate;

@Data 
public class ExpedienteDTO {
    
    // --- DATOS DEL JUICIO PRINCIPAL ---
    private String numeroExpediente;
    private String sufijoExpediente;
    private Long clienteId;
    private String nombreCliente; 
    private String nombreEmpresa; 
    
    
    // Lógica de Junta (Catalogo)
    private CatJunta junta; // Para compatibilidad con Paso 1
    private String nombreJunta; // Para el Autocomplete del Módulo Legal

    // --- DATOS DE SEGUIMIENTO (PRINCIPAL) ---
    private String litis;
    private String anotacion;
    private LocalDate proximaAudiencia;
    private LocalDate fechaRecordatorio;

    // --- SECCIÓN DE AMPARO (EL "ESPEJO") ---
    private Boolean tieneAmparo;           // Controla el Switch en el Front
    private Long amparoTribunalId;        // ID del Tribunal Colegiado (T.C.C.)
    private String amparoNumero;          // El número D.T. del amparo
    private LocalDate amparoFechaAudiencia; // Fecha específica del amparo
    private String amparo;;           // Mapea al campo 'amparo' (TEXT) de la Entidad
}