package org.datasource.springdata.views;

public interface ProductDetailsProjection {
    String getProductId();
    String getCategoryName();
    Double getWeight();
}