package com.example.sadjoum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_table")
public class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "order_id")
        Long id;

        @Column(name = "order_type")
        String type;

        @Column(name = "order_quantity")
        int qte;

        @Column(name = "order_unit_price")
        int prixUnitaire;

        @Column(name = "order_total_price")
        int prixTotal;

        public Order() {
        }

        public Order(Long id, String type, int qte, int prixUnitaire, int prixTotal) {
                this.id = id;
                this.type = type;
                this.qte = qte;
                this.prixUnitaire = prixUnitaire;
                this.prixTotal = prixTotal;
        }

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getType() {
                return type;
        }

        public void setType(String type) {
                this.type = type;
        }

        public int getQte() {
                return qte;
        }

        public void setQte(int qte) {
                this.qte = qte;
        }

        public int getPrixUnitaire() {
                return prixUnitaire;
        }

        public void setPrixUnitaire(int prixUnitaire) {
                this.prixUnitaire = prixUnitaire;
        }

        public int getPrixTotal() {
                return prixTotal;
        }

        public void setPrixTotal(int prixTotal) {
                this.prixTotal = prixTotal;
        }

        @Override
        public String toString() {
                return "Order{" +
                        "id=" + id +
                        ", type='" + type + '\'' +
                        ", qte=" + qte +
                        ", prixUnitaire=" + prixUnitaire +
                        ", prixTotal=" + prixTotal +
                        '}';
        }
}
