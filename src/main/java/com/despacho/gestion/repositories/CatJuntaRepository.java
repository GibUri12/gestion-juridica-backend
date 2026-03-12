package com.despacho.gestion.repositories;

import com.despacho.gestion.models.CatJunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatJuntaRepository extends JpaRepository<CatJunta, Integer> {
    List<CatJunta> findByActivoTrue();
}