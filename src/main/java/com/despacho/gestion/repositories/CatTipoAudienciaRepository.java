package com.despacho.gestion.repositories;

import com.despacho.gestion.models.CatTipoAudiencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatTipoAudienciaRepository extends JpaRepository<CatTipoAudiencia, Long> {

    List<CatTipoAudiencia> findByActivoTrueOrderByDescripcion();

    List<CatTipoAudiencia> findByDescripcionContainingIgnoreCaseAndActivoTrueOrderByDescripcion(String descripcion);
}