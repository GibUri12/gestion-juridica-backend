package com.despacho.gestion.repositories;

import com.despacho.gestion.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findByActivoTrue();
    List<Empresa> findByNombreCompletoContainingIgnoreCaseAndActivoTrue(String nombre);
    
    // Para verificar si ya existe antes de crear una nueva en el autocomplete
    Optional<Empresa> findByNombreCompletoIgnoreCase(String nombre);
}