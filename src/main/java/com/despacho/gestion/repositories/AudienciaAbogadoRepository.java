package com.despacho.gestion.repositories;

import com.despacho.gestion.models.AudienciaAbogado;
import com.despacho.gestion.models.AudienciaAbogadoId;
import com.despacho.gestion.models.Audiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudienciaAbogadoRepository
        extends JpaRepository<AudienciaAbogado, AudienciaAbogadoId> {

    List<AudienciaAbogado> findByAudiencia(Audiencia audiencia);
}