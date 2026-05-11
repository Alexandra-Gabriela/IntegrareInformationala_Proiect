package org.datasource.mongodb.views.olist;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import lombok.Data;

@Data
public class SellerLocationView {
    @BsonProperty("seller_id")
    @JsonProperty("sellerId")
    private String sellerId;

    @BsonProperty("seller_city")
    private String sellerCity;

    private LocationInner location; // Obiectul care conține coordonatele

    @Data
    public static class LocationInner {
        private Double lat;
        private Double lng;
    }
}