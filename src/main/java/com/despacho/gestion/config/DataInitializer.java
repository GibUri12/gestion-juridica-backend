package com.despacho.gestion.config;

import com.despacho.gestion.models.Role;
import com.despacho.gestion.models.Usuario;
import com.despacho.gestion.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                // 1. Usuario Administrador Principal
                Usuario admin = new Usuario();
                admin.setUsername("Gibran");
                admin.setPassword(encoder.encode("2312Conejo")); // Cambia esto después
                admin.setRole(Role.ROLE_ADMIN);
                repository.save(admin);

                // 2. Usuario IT Manager
                Usuario itManager = new Usuario();
                itManager.setUsername("it_manager");
                itManager.setPassword(encoder.encode("it123"));
                itManager.setRole(Role.ROLE_IT_MANAGER);
                repository.save(itManager);

                // 3. Usuario Aguilar (Abogado)
                Usuario aguilar = new Usuario();
                aguilar.setUsername("Aguilar");
                aguilar.setPassword(encoder.encode("passwordAguilar")); // Pon la que gustes
                aguilar.setRole(Role.ROLE_ADMIN);
                repository.save(aguilar);

                // 4. Usuario Grimeldo (Abogado)
                Usuario grimeldo = new Usuario();
                grimeldo.setUsername("Grimeldo");
                grimeldo.setPassword(encoder.encode("passwordGrimeldo")); // Pon la que gustes
                grimeldo.setRole(Role.ROLE_ADMIN);
                repository.save(grimeldo);

                System.out.println("✅ Base de Datos inicializada: Admin, IT, Aguilar y Grimeldo creados.");
            }
        };
    }
}