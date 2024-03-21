package com.example.sadjoum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_table")
public record Order(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "order_id")
        Long id,

        @Column(name = "order_type")
        String type,

        @Column(name = "order_quantity")
        int qte,

        @Column(name = "order_unit_price")
        int prixUnitaire,

        @Column(name = "order_total_price")
        int prixTotal
) { }

