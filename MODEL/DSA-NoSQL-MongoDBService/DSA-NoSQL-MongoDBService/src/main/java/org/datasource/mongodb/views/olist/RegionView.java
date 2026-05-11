package org.datasource.mongodb.views.olist;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RegionView {
    @BsonProperty("region") // Cheia exactă din Compass
    @JsonProperty("regionName")
    private String regionName;

    private List<StateView> states;
}