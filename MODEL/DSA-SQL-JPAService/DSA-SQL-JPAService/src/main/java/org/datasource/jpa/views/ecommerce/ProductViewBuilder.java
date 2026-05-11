package org.datasource.jpa.views.ecommerce;

import org.datasource.jpa.JPADataSourceConnector;
import org.springframework.stereotype.Service;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductViewBuilder {
    private String JPQL_SELECT = "SELECT NEW org.datasource.jpa.views.ecommerce.ProductView(p.productId, p.categoryId, p.weight) FROM ProductView p";
    private List<ProductView> productViewList = new ArrayList<>();
    private JPADataSourceConnector dataSourceConnector;

    public ProductViewBuilder(JPADataSourceConnector dataSourceConnector) {
        this.dataSourceConnector = dataSourceConnector;
    }

    public List<ProductView> getProductViewList() {
        return productViewList;
    }

    public ProductViewBuilder build() {
        EntityManager em = dataSourceConnector.getEntityManager();
        this.productViewList = em.createQuery(JPQL_SELECT, ProductView.class).getResultList();
        return this;
    }
}