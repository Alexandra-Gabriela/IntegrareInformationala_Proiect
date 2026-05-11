package org.j4di.visualisation.olap.fact.views.sales;

import java.time.LocalDate;

public record OLAP_FACTS_SALES_AMOUNT(
        String orderId,        // ID Comanda din CSV
        String productId,      // ID Produs din Postgres
        LocalDate invoiceDate, // Data din CSV
        Double sales_amount    // Pretul din CSV
){}
