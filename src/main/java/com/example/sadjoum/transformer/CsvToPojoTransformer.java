package com.example.sadjoum.transformer;

import com.example.sadjoum.model.Order;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.stereotype.Component;

@Component
public class CsvToPojoTransformer implements GenericTransformer<String, Order> {

    @Override
    public Order transform(String csvData) {
        // Parse CSV and create Order record
        // You may use a CSV parsing library like OpenCSV or manually split the string
        // For simplicity, let's assume a CSV format: type,qte,prixUnitaire,prixTotal
        String[] parts = csvData.split(",");
        String type = parts[0].trim();
        int qte = Integer.parseInt(parts[1].trim());
        int prixUnitaire = Integer.parseInt(parts[2].trim());
        int prixTotal = qte * prixUnitaire;

        return new Order(1L, type, qte, prixUnitaire, prixTotal);
    }
}
