package org.j4di.visualisation.olap.analytical.views.sales;

public record OLAP_VIEW_SALES_CTG_PROD (
        String categoryName, // Numele categoriei din Postgres
        Double sales_amount  // Suma preturilor din CSV
){}