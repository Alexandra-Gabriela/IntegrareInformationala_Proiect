----------------------------------------------------------------------------------
-- 1. CITY & REGIONS VIEW (Sursa: MongoDB port 8093)
----------------------------------------------------------------------------------
-- Descarcă JSON-ul brut pentru orașe/regiuni
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'CITY_JSON_VIEW',
                   'http://developer:iis@localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/CityView');

-- Transformă JSON-ul în tabel SQL (Explode)
-- Spark va folosi maparea din CityView.java (city, zip_code_prefix, stateName)
CREATE OR REPLACE VIEW VW_CUSTS_LOCATIONS AS
SELECT v.* FROM CITY_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

-- Activează accesul REST pentru Dashboard-ul Vaadin
ALTER VIEW VW_CUSTS_LOCATIONS SET TBLPROPERTIES('AUTOREST' = 'VW_CUSTS_LOCATIONS');


----------------------------------------------------------------------------------
-- 2. SELLER LOCATIONS VIEW (Sursa: MongoDB port 8093)
----------------------------------------------------------------------------------
-- Descarcă JSON-ul brut pentru vânzători
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'SELLERS_JSON_VIEW',
                   'http://developer:iis@localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/SellerView');

-- Transformă JSON-ul în tabel SQL
-- Va include campurile din SellerLocationView.java (sellerId, sellerCity, location)
CREATE OR REPLACE VIEW VW_SELLERS_LOCATIONS AS
SELECT v.* FROM SELLERS_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

ALTER VIEW VW_SELLERS_LOCATIONS SET TBLPROPERTIES('AUTOREST' = 'VW_SELLERS_LOCATIONS');