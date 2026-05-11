package org.datasource.jpa.views.ecommerce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.io.Serializable;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name="products", schema="ecommerce")
public class ProductView implements Serializable {
	@Id
	@Column(name="product_id")
	private String productId;

	@Column(name="category_id")
	private Long categoryId;

	@Column(name="product_weight_g")
	private Double weight;
}