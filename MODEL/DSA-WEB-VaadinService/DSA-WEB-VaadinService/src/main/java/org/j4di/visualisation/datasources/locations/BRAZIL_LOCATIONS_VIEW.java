package org.j4di.visualisation.datasources.locations;

/**
 * Record adaptat pentru locațiile geografice din MongoDB.
 */
public record BRAZIL_LOCATIONS_VIEW(
        String cityName,
        String stateName
) { }