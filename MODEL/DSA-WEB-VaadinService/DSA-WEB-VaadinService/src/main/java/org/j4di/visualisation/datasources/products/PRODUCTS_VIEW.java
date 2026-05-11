package org.j4di.visualisation.datasources.products;

public record PRODUCTS_VIEW(
        String productId,      // ID-ul real din Postgres
        String categoryName,   // Categoria produsului
        Double price,          // Prețul de vânzare
        Double weight          // Greutatea produsului
){}