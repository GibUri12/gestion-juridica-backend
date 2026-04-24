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

                Usuario admin = new Usuario();
                admin.setNombreCompleto("Gibran Administrador");
                admin.setUsername("Gibran");
                admin.setEmail("gibran@despacho.com");
                admin.setPassword(encoder.encode("2312Conejo"));
                admin.setRol(Role.ADMINISTRADOR);
                repository.save(admin);

                Usuario itManager = new Usuario();
                itManager.setNombreCompleto("IT Manager");
                itManager.setUsername("it_manager");
                itManager.setEmail("it@despacho.com");
                itManager.setPassword(encoder.encode("it123"));
                itManager.setRol(Role.IT_MANAGER);
                repository.save(itManager);

                Usuario aguilar = new Usuario();
                aguilar.setNombreCompleto("Aguilar");
                aguilar.setUsername("Aguilar");
                aguilar.setEmail("aguilar@despacho.com");
                aguilar.setPassword(encoder.encode("passwordAguilar"));
                aguilar.setRol(Role.ADMINISTRADOR);
                repository.save(aguilar);

                Usuario grimeldo = new Usuario();
                grimeldo.setNombreCompleto("Grimeldo");
                grimeldo.setUsername("Grimeldo");
                grimeldo.setEmail("grimeldo@despacho.com");
                grimeldo.setPassword(encoder.encode("passwordGrimeldo"));
                grimeldo.setRol(Role.ADMINISTRADOR);
                repository.save(grimeldo);

                // --- ABOGADOS (Nuevos registros) ---
                Usuario abogado1 = new Usuario();
                abogado1.setNombreCompleto("Lic. Hector Herrera");
                abogado1.setUsername("hector_h");
                abogado1.setEmail("hector.h@despacho.com");
                abogado1.setPassword(encoder.encode("abogado2026"));
                abogado1.setRol(Role.ABOGADO); // <--- Asegúrate de que Role.ABOGADO exista en tu Enum
                repository.save(abogado1);

                Usuario abogado2 = new Usuario();
                abogado2.setNombreCompleto("Lic. Axel Aguilar");
                abogado2.setUsername("axel_a");
                abogado2.setEmail("axel.a@despacho.com");
                abogado2.setPassword(encoder.encode("abogado123"));
                abogado2.setRol(Role.ABOGADO);
                repository.save(abogado2);

                System.out.println("✅ Base de Datos inicializada correctamente.");
            }
        };
    }
}