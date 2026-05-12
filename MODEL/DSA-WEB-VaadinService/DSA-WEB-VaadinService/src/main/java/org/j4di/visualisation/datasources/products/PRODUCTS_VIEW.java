package org.j4di.visualisation.datasources.products;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PRODUCTS_VIEW(
        @JsonProperty("productId")    String productId,
        @JsonProperty("categoryId")   Long categoryId,
        @JsonProperty("categoryName") String categoryName, // Adaugă acest câmp
        @JsonProperty("weight")       Double weight
) {}