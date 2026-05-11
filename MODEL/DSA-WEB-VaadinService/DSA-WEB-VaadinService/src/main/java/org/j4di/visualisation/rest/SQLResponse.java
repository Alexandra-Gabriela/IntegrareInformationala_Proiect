package org.j4di.visualisation.rest;

import java.util.List;

// Record Type to restore the response of REST Enabled Service of SparkSQL
public record SQLResponse<T>(
        String query,
        List<T> response
) {}