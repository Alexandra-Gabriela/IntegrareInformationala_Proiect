package org.datasource.springdata.views;

public record PRODUCTS_VIEW(
        String productId,      // ID-ul real din Postgres
        String categoryName,   // Categoria produsului
        Double price,          // Pretul de vanzare
        Double weight          // Greutatea produsului
){}