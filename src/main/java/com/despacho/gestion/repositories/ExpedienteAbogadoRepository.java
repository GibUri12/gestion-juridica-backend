package com.despacho.gestion.repositories;

import com.despacho.gestion.models.ExpedienteAbogado;
import com.despacho.gestion.models.ExpedienteAbogadoId;
import com.despacho.gestion.models.Expediente;
import com.despacho.gestion.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpedienteAbogadoRepository
        extends JpaRepository<ExpedienteAbogado, ExpedienteAbogadoId> {

    List<ExpedienteAbogado> findByExpedienteAndActivoTrue(Expediente expediente);
    List<ExpedienteAbogado> findByUsuarioAndActivoTrue(Usuario usuario);
}