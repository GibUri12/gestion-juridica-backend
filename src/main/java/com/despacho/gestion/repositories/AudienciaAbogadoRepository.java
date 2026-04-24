package com.despacho.gestion.repositories;

import com.despacho.gestion.models.AudienciaAbogado;
import com.despacho.gestion.models.AudienciaAbogadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudienciaAbogadoRepository extends JpaRepository<AudienciaAbogado, AudienciaAbogadoId> {

    /** Eliminar todas las asignaciones de una audiencia (para reasignar) */
    void deleteByIdAudienciaId(Long audienciaId);

    /** Abogados de una audiencia */
    List<AudienciaAbogado> findByIdAudienciaId(Long audienciaId);
}