package org.datasource.jpa.views.ecommerce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.io.Serializable;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name="categories", schema="ecommerce")
public class CategoryView implements Serializable {
    @Id
    @Column(name="category_id")
    private Long categoryId;

    @Column(name="product_category_name")
    private String categoryName;
}