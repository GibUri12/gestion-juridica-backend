package com.despacho.gestion.repositories;

import com.despacho.gestion.models.Expediente;
import com.despacho.gestion.models.MovimientoExpediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoExpedienteRepository
        extends JpaRepository<MovimientoExpediente, Long> {

    // Paginado porque puede crecer mucho
    Page<MovimientoExpediente> findByExpedienteOrderByCreatedAtDesc(
            Expediente expediente, Pageable pageable);
}