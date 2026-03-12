package com.despacho.gestion.repositories;

import com.despacho.gestion.models.EstadoExpediente;
import com.despacho.gestion.models.Expediente;
import com.despacho.gestion.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {

    // Buscar por número exacto
    Optional<Expediente> findByNumeroExpediente(String numeroExpediente);

    // Buscar por estado
    List<Expediente> findByEstado(EstadoExpediente estado);

    // Expedientes asignados a un abogado específico
    @Query("""
        SELECT e FROM Expediente e
        JOIN ExpedienteAbogado ea ON ea.expediente = e
        WHERE ea.usuario = :usuario
        AND ea.activo = true
    """)
    List<Expediente> findByAbogado(@Param("usuario") Usuario usuario);

    // Buscar por número parcial
    List<Expediente> findByNumeroExpedienteContainingIgnoreCase(String numero);
}
