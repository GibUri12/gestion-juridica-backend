package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cat_tipos_audiencia")
@Data
public class CatTipoAudiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String abreviatura;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;
}