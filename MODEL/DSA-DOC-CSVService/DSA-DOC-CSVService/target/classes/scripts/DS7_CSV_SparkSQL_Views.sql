-- 1. Creare View JSON
SELECT java_method(
               'org.spark.service.rest.RESTEnabledSQLService',
               'createJSONViewFromREST',
               'ORDER_ITEMS_JSON_VIEW',
               'http://developer:iis@localhost:8094/DSA-DOC-CSVService/rest/csv/order/items');

-- 2. Creare View SQL
CREATE OR REPLACE VIEW ORDER_ITEMS_VIEW AS
SELECT v.*
FROM ORDER_ITEMS_JSON_VIEW as json_view LATERAL VIEW explode(json_view.array) AS v;

-- 3. Activare REST
ALTER VIEW ORDER_ITEMS_VIEW SET TBLPROPERTIES('AUTOREST' = 'ORDER_ITEMS_VIEW');