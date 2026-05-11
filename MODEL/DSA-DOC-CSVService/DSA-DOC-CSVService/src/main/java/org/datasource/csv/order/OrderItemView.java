package org.datasource.csv.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor(force = true)
public class OrderItemView {
	private String orderId;
	private String productId;
	private String sellerId;
	private Double price;
	private Double freightValue;
}