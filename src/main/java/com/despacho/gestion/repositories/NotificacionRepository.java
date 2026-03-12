package com.despacho.gestion.repositories;

import com.despacho.gestion.models.Notificacion;
import com.despacho.gestion.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    // Notificaciones no leídas del usuario
    List<Notificacion> findByUsuarioDestinatarioAndLeidaFalse(Usuario usuario);

    // Todas las notificaciones del usuario
    List<Notificacion> findByUsuarioDestinatarioOrderByCreatedAtDesc(
            Usuario usuario);
}