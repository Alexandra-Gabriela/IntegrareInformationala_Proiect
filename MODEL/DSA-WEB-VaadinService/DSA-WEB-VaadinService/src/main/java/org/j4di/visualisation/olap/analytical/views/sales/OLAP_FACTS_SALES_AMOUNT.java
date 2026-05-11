package org.j4di.visualisation.olap.analytical.views.sales;

import java.time.LocalDate;

public record OLAP_FACTS_SALES_AMOUNT(
        String productId,
        Double weight,
        Double sales_amount,
        LocalDate invoiceDate
){}