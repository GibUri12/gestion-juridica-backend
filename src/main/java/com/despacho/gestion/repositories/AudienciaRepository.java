package com.despacho.gestion.repositories;

import com.despacho.gestion.models.Audiencia;
import com.despacho.gestion.models.EstadoAudiencia;
import com.despacho.gestion.models.Expediente;
import com.despacho.gestion.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AudienciaRepository extends JpaRepository<Audiencia, Long> {

    // Audiencias de un expediente
    List<Audiencia> findByExpediente(Expediente expediente);

    // Audiencias por estado
    List<Audiencia> findByEstado(EstadoAudiencia estado);

    // Audiencias por fecha
    List<Audiencia> findByFecha(LocalDate fecha);

    // Audiencias en rango de fechas
    List<Audiencia> findByFechaBetween(LocalDate inicio, LocalDate fin);

    // Audiencias asignadas a un abogado
    @Query("""
        SELECT a FROM Audiencia a
        JOIN AudienciaAbogado aa ON aa.audiencia = a
        WHERE aa.usuario = :usuario
        AND a.estado = 'PROGRAMADA'
        ORDER BY a.fecha ASC
    """)
    List<Audiencia> findProgramadasByAbogado(@Param("usuario") Usuario usuario);

    // Seguimientos de una audiencia padre
    List<Audiencia> findByAudienciaPadre(Audiencia padre);
}