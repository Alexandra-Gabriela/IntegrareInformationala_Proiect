package org.j4di.visualisation.olap.analytical.views.customers;

/**
 * Dimensiune care uneste datele despre clienti cu locatiile din MongoDB.
 */
public record OLAP_DIM_CUSTS_CITIES_DEPTS(
        String customerid,      // String pentru ID-urile Olist
        String customername,
        String cityname,        // Din Mongo
        String statename        // Din Mongo
){}