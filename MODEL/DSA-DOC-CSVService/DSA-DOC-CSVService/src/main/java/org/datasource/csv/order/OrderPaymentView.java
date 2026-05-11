package org.datasource.csv.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor(force = true)
public class OrderPaymentView {
    private String orderId;
    private String paymentType;
    private Double paymentValue;
    private Integer paymentInstallments;
}