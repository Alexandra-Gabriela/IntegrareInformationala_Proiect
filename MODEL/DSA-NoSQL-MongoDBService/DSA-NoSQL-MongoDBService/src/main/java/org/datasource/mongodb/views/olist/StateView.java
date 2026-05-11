package org.datasource.mongodb.views.olist;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class StateView {
    @BsonProperty("state_name") // Potrivire cu Compass
    @JsonProperty("stateName")
    private String stateName;

    private List<CityView> cities;
}