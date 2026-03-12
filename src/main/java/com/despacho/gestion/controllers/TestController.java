package com.despacho.gestion.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/api/test")
    public String test() {
        return "OK";
    }
    
    @GetMapping("/api/test/auth")
    public String testAuth(Authentication authentication) {
        return "Usuario autenticado: " + authentication.getName();
    }
}
