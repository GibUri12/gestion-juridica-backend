package com.despacho.gestion.dto;

import com.despacho.gestion.dto.AudienciaDTO;
import com.despacho.gestion.models.Audiencia;
import com.despacho.gestion.models.AudienciaAbogado;

import java.util.Collections;
import java.util.List;

public class AudienciaMapper {

    // Constructor privado — clase utilitaria, no se instancia
    private AudienciaMapper() {}

    public static AudienciaDTO toDTO(Audiencia a) {
        AudienciaDTO dto = new AudienciaDTO();

        dto.setId(a.getId());
        dto.setEstado(a.getEstado() != null ? a.getEstado().name() : null);
        dto.setFecha(a.getFecha());
        dto.setHora(a.getHora());
        dto.setResultado(a.getResultado());

        // ── Expediente ───────────────────────────────────────────────
        if (a.getExpediente() != null) {
            dto.setExpedienteId(a.getExpediente().getId());
            dto.setExpedienteNumero(a.getExpediente().getNumeroExpediente());

            if (a.getExpediente().getCliente() != null) {
                dto.setClienteNombre(a.getExpediente().getCliente().getNombreCompleto());
            }
            if (a.getExpediente().getEmpresa() != null) {
                dto.setEmpresaNombre(a.getExpediente().getEmpresa().getNombreCompleto());
            }
        }

        // ── Tipo de audiencia ────────────────────────────────────────
        if (a.getTipoAudiencia() != null) {
            dto.setTipoAudienciaId(a.getTipoAudiencia().getId());
            dto.setTipoAudienciaAbreviatura(a.getTipoAudiencia().getAbreviatura());
            dto.setTipoAudienciaDescripcion(a.getTipoAudiencia().getDescripcion());
        }

        // ── Tribunal ─────────────────────────────────────────────────
        if (a.getTribunal() != null) {
            dto.setTribunalId(a.getTribunal().getId());
            dto.setTribunalClave(a.getTribunal().getClave());
            dto.setTribunalNombre(a.getTribunal().getNombreCompleto());
        }

        // ── Audiencia padre ──────────────────────────────────────────
        if (a.getAudienciaPadre() != null) {
            dto.setAudienciaPadreId(a.getAudienciaPadre().getId());
            dto.setAudienciaPadreFecha(
                a.getAudienciaPadre().getFecha() != null
                    ? a.getAudienciaPadre().getFecha().toString()
                    : null
            );
        }

        // ── Abogados ─────────────────────────────────────────────────
        List<AudienciaAbogado> abogados = a.getAbogados();
        if (abogados != null && !abogados.isEmpty()) {
            dto.setAbogados(
                abogados.stream()
                    .map(AudienciaMapper::toAbogadoDTO)
                    .toList()
            );
        } else {
            dto.setAbogados(Collections.emptyList());
        }

        return dto;
    }

    private static AudienciaDTO.AbogadoAudienciaDTO toAbogadoDTO(AudienciaAbogado aa) {
        AudienciaDTO.AbogadoAudienciaDTO dto = new AudienciaDTO.AbogadoAudienciaDTO();
        dto.setUsuarioId(aa.getUsuario().getId());
        dto.setNombreCompleto(aa.getUsuario().getNombreCompleto());
        dto.setClaveAbogado(aa.getUsuario().getClaveAbogado());
        dto.setEsTitular(Boolean.TRUE.equals(aa.getEsTitular()));
        return dto;
    }

    // Convierte lista completa
    public static List<AudienciaDTO> toDTOList(List<Audiencia> audiencias) {
        return audiencias.stream()
            .map(AudienciaMapper::toDTO)
            .toList();
    }
}