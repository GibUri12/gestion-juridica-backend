package com.despacho.gestion.controllers;
import com.despacho.gestion.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private com.despacho.gestion.repositories.UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            String token    = jwtUtils.generateToken(auth.getName());
            String username = auth.getName();
            String role     = auth.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            // Obtener el id desde el repositorio usando el username autenticado
            Long userId = usuarioRepository.findByUsername(username)
                    .map(u -> u.getId())
                    .orElse(null);

            return ResponseEntity.ok(new JwtResponse(token, username, role, userId));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }
    }
}

@Data 
class LoginRequest { 
    private String username; 
    private String password; 
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class JwtResponse { 
    private String token; 
    private String username;
    private String role;
    private Long   id;
}