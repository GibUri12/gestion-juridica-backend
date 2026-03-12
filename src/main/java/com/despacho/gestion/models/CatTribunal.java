package com.despacho.gestion.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cat_tribunales")
@Data
public class CatTribunal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String clave;

    @Column(name = "nombre_completo", nullable = false, length = 250)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTribunal tipo;

    @Column(nullable = false)
    private Boolean activo = true;
}