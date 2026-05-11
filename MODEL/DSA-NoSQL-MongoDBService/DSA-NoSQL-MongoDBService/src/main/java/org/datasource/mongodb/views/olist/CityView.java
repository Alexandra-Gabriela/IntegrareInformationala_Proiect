package org.datasource.mongodb.views.olist;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import lombok.Data;

@Data
public class CityView {
    @BsonProperty("city_name") // Cum se numește în Compass
    @JsonProperty("city")      // Cum vrei să apară în Browser
    private String cityName;

    @BsonProperty("avg_lat")   // În Compass ai avg_lat, nu postalCode
    @JsonProperty("zip_code_prefix")
    private Double cityZipCodePrefix;

    private String stateName; // Populat manual de Builder
}