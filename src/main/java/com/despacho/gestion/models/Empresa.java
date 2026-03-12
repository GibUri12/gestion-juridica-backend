package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empresas")
@Data
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 400)
    private String nombreCompleto;

    @Column(nullable = false)
    private Boolean activo = true;
}