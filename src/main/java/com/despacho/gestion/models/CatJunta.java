package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cat_juntas")
@Data
public class CatJunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;
}
