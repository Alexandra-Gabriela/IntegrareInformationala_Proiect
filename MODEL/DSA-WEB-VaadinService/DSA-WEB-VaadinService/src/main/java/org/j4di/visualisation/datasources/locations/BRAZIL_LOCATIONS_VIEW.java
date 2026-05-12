package org.j4di.visualisation.datasources.locations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record adaptat pentru locațiile geografice din MongoDB.
 */
public record BRAZIL_LOCATIONS_VIEW(
        @JsonProperty("cityName") String cityName,
        @JsonProperty("stateName") String stateName
) { }