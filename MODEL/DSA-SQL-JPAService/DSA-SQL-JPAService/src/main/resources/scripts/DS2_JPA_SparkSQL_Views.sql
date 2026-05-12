-- 1. Creare View JSON (Preluare date brute)
SELECT java_method(
               'org.spark.service.rest.RESTEnabledSQLService',
               'createJSONViewFromREST',
               'PRODUCTS_JSON_VIEW',
               'http://developer:iis@localhost:8091/DSA_SQL_JPAService/rest/ecommerce/ProductView');

-- 2. Creare View SQL (Transformare în tabel)
CREATE OR REPLACE VIEW PRODUCTS_VIEW AS
SELECT v.*
FROM PRODUCTS_JSON_VIEW as json_view LATERAL VIEW explode(json_view.array) AS v;

-- 3. Activare REST
ALTER VIEW PRODUCTS_VIEW SET TBLPROPERTIES('AUTOREST' = 'PRODUCTS_VIEW');