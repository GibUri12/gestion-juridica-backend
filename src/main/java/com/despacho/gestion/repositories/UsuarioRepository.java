package com.despacho.gestion.repositories;
import com.despacho.gestion.models.Role;
import com.despacho.gestion.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    List<Usuario> findByRolAndActivoTrue(Role rol);
}

