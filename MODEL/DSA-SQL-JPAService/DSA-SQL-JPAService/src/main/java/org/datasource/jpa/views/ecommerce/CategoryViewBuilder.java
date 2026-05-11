package org.datasource.jpa.views.ecommerce;

import org.datasource.jpa.JPADataSourceConnector;
import org.springframework.stereotype.Service;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryViewBuilder {
    private String JPQL_SELECT = "SELECT NEW org.datasource.jpa.views.ecommerce.CategoryView(c.categoryId, c.categoryName) FROM CategoryView c";
    private List<CategoryView> categoryViewList = new ArrayList<>();
    private JPADataSourceConnector dataSourceConnector;

    public CategoryViewBuilder(JPADataSourceConnector dataSourceConnector) {
        this.dataSourceConnector = dataSourceConnector;
    }

    public List<CategoryView> getCategoryViewList() {
        return categoryViewList;
    }

    public CategoryViewBuilder build() {
        EntityManager em = dataSourceConnector.getEntityManager();
        this.categoryViewList = em.createQuery(JPQL_SELECT, CategoryView.class).getResultList();
        return this;
    }
}