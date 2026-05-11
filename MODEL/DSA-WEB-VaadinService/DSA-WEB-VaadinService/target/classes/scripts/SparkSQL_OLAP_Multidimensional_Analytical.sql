-- 1. Mapare Produse (Postgres) - Port 8091
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST', 'PRODUCTS_VIEW', 'http://localhost:8091/DSA_SQL_JPAService/rest/ecommerce/ProductView');

-- 2. Mapare Locații (MongoDB) - Port 8093
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST', 'VW_CUSTS_LOCATIONS', 'http://localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/CityView');

-- Adaugă aceste linii înainte de CREATE
DROP VIEW IF EXISTS VW_INTEGRATED_OLIST;

CREATE OR REPLACE VIEW VW_INTEGRATED_OLIST AS
SELECT
    productId,
    CAST(weight AS DOUBLE) as weight,
    CAST(price AS DOUBLE) as price,
    categoryName
FROM PRODUCTS_VIEW
WHERE productId IS NOT NULL;

ALTER VIEW VW_INTEGRATED_OLIST SET TBLPROPERTIES('AUTOREST' = 'VW_INTEGRATED_OLIST');
-- 3. CREAREA VIZUALIZĂRII INTEGRATE (LIPSA DIN SCRIPTUL TĂU)
-- Aceasta este vizualizarea căutată de JFChart_LOGISTICS_EFFICIENCY
CREATE OR REPLACE VIEW VW_INTEGRATED_OLIST AS
SELECT
    productId,
    weight,
    price,
    categoryName
FROM PRODUCTS_VIEW
WHERE productId IS NOT NULL;

-- 4. ACTIVARE REST PENTRU TOATE SURSELE (Fără acestea vei primi erori 500/404 în Vaadin)
ALTER VIEW PRODUCTS_VIEW SET TBLPROPERTIES('AUTOREST' = 'PRODUCTS_VIEW');
ALTER VIEW VW_CUSTS_LOCATIONS SET TBLPROPERTIES('AUTOREST' = 'VW_CUSTS_LOCATIONS');
ALTER VIEW VW_INTEGRATED_OLIST SET TBLPROPERTIES('AUTOREST' = 'VW_INTEGRATED_OLIST');