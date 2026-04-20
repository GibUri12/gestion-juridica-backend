package com.despacho.gestion.repositories;

import com.despacho.gestion.models.Audiencia;
import com.despacho.gestion.models.EstadoAudiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AudienciaRepository extends JpaRepository<Audiencia, Long> {

    /** Todas ordenadas por fecha desc */
    @Query("SELECT a FROM Audiencia a " +
        "LEFT JOIN FETCH a.expediente e " +
        "LEFT JOIN FETCH e.cliente " +
        "LEFT JOIN FETCH e.empresa " +
        "LEFT JOIN FETCH a.tipoAudiencia " +
        "LEFT JOIN FETCH a.tribunal " +
        "LEFT JOIN FETCH a.abogados ab " +
        "LEFT JOIN FETCH ab.usuario " +
        "ORDER BY a.fecha DESC")
    List<Audiencia> findAllConDetalle();

    /** Por estado */
    List<Audiencia> findByEstadoOrderByFechaDesc(EstadoAudiencia estado);

    /** Por expediente */
    List<Audiencia> findByExpedienteIdOrderByFechaDesc(Long expedienteId);

    /** Por abogado asignado */
    @Query("SELECT a FROM Audiencia a " +
        "JOIN a.abogados rel " + // 'rel' es la entidad AudienciaAbogado
        "WHERE rel.usuario.id = :abogadoId " +
        "ORDER BY a.fecha DESC")
    List<Audiencia> findByAbogadoId(@Param("abogadoId") Long abogadoId);

    /** Filtros combinados */
    @Query("SELECT DISTINCT a FROM Audiencia a " +
           "LEFT JOIN a.abogados rel " +
           "LEFT JOIN FETCH a.expediente e " +
           "LEFT JOIN FETCH e.cliente " +
           "LEFT JOIN FETCH e.empresa " +
           "LEFT JOIN FETCH a.tipoAudiencia " +
           "LEFT JOIN FETCH a.tribunal " +
           "LEFT JOIN FETCH a.abogados ab " +
           "LEFT JOIN FETCH ab.usuario " +
           "WHERE (:estado IS NULL OR a.estado = :estado) AND " +
           "(:fechaDesde IS NULL OR a.fecha >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR a.fecha <= :fechaHasta) AND " +
           "(:abogadoId IS NULL OR rel.usuario.id = :abogadoId) " +
           "ORDER BY a.fecha DESC")
    List<Audiencia> findFiltrosCombinados(
            @Param("estado") EstadoAudiencia estado,
            @Param("abogadoId") Long abogadoId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta);

    /** Audiencias del abogado autenticado — para módulo abogado */
    @Query("SELECT a FROM Audiencia a JOIN a.abogados ab " +
           "WHERE ab.usuario.id = :usuarioId AND a.estado = 'PROGRAMADA' " +
           "ORDER BY a.fecha ASC")
    List<Audiencia> findProgramadasByAbogado(@Param("usuarioId") Long usuarioId);

    @Query("SELECT a FROM Audiencia a " +
       "LEFT JOIN FETCH a.tipoAudiencia " +
       "LEFT JOIN FETCH a.tribunal " +
       "LEFT JOIN FETCH a.abogados ab " +
       "LEFT JOIN FETCH ab.usuario " +
       "WHERE a.expediente.id = :expedienteId " +
       "ORDER BY a.fecha DESC")
    List<Audiencia> findByExpedienteConDetalle(@Param("expedienteId") Long expedienteId);

    @Query("SELECT a FROM Audiencia a " +
       "LEFT JOIN FETCH a.expediente e " +
       "LEFT JOIN FETCH e.cliente " +
       "LEFT JOIN FETCH e.empresa " +
       "LEFT JOIN FETCH a.tipoAudiencia " +
       "LEFT JOIN FETCH a.tribunal " +
       "LEFT JOIN FETCH a.abogados ab " +
       "LEFT JOIN FETCH ab.usuario " +
       "WHERE a.id = :id")
    Optional<Audiencia> findByIdConDetalle(@Param("id") Long id);
}