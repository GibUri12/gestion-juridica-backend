package com.despacho.gestion.repositories;

import com.despacho.gestion.models.CatTribunal;
import com.despacho.gestion.models.TipoTribunal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface CatTribunalRepository extends JpaRepository<CatTribunal, Long> {
    List<CatTribunal> findByActivoTrue();
    List<CatTribunal> findByTipoAndActivoTrue(TipoTribunal tipo);
    List<CatTribunal> findByNombreCompletoContainingIgnoreCase(String nombre);
    Optional<CatTribunal> findByNombreCompletoIgnoreCase(String nombreCompleto);


}