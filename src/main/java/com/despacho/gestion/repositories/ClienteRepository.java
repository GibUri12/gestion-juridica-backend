package com.despacho.gestion.repositories;

import com.despacho.gestion.models.Cliente;
import com.despacho.gestion.models.Empresa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Solo activos, ordenados alfabéticamente */
    List<Cliente> findByActivoTrueOrderByNombreCompleto();
    // Debe devolver List<Cliente>
    List<Cliente> findByNombreCompletoContainingIgnoreCaseAndActivoTrue(String nombre);

    /** Todos (activos e inactivos), ordenados alfabéticamente */
    List<Cliente> findAllByOrderByNombreCompleto();
}