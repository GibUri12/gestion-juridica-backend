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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );
            
            // Generamos el token
            String token = jwtUtils.generateToken(auth.getName());
            
            // Extraemos el nombre de usuario
            String username = auth.getName();
            
            // Extraemos el rol (Spring Security los guarda en getAuthorities)
            // Tomamos el primero, ya que en tu caso cada usuario tiene un solo rol
            String role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            // Devolvemos la respuesta completa
            return ResponseEntity.ok(new JwtResponse(token, username, role));
            
        } catch (Exception e) {
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
}