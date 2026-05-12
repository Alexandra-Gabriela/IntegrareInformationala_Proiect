-- 1. Creare View JSON
SELECT java_method(
               'org.spark.service.rest.RESTEnabledSQLService',
               'createJSONViewFromREST',
               'LOCATIONS_JSON_VIEW',
               'http://developer:iis@localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/CityView');

-- 2. Creare View SQL
CREATE OR REPLACE VIEW VW_CUSTS_LOCATIONS AS
SELECT v.*
FROM LOCATIONS_JSON_VIEW as json_view LATERAL VIEW explode(json_view.array) AS v;

-- 3. Activare REST
ALTER VIEW VW_CUSTS_LOCATIONS SET TBLPROPERTIES('AUTOREST' = 'VW_CUSTS_LOCATIONS');