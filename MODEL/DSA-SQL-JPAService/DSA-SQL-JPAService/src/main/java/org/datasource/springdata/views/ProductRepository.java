package org.datasource.springdata.views;

import org.datasource.jpa.views.ecommerce.ProductView; // Import corect conform pozei
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductView, String> {

    @Query(nativeQuery = true, value = """
            SELECT p.product_id as productId, 
                   c.product_category_name as categoryName, 
                   p.product_weight_g as weight
            FROM products p
            INNER JOIN categories c ON p.category_id = c.category_id
            """)
    List<ProductDetailsProjection> getProductFullDetails();
}