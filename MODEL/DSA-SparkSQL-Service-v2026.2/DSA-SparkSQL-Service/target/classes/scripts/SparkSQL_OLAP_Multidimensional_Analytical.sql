--------------------------------------------------------------------------------
-- 1. CURĂȚARE TOTALĂ (Resetăm metadatele corupte)
--------------------------------------------------------------------------------
DROP VIEW IF EXISTS OLAP_FACTS_SALES_AMOUNT;
DROP VIEW IF EXISTS VW_CUSTS_LOCATIONS;
DROP VIEW IF EXISTS PRODUCTS_VIEW;
DROP VIEW IF EXISTS ORDER_ITEMS_PREPARED;

--------------------------------------------------------------------------------
-- 2. IMPORT PRODUSE (PostgreSQL -> Spark)
--------------------------------------------------------------------------------
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'PRODUCTS_JSON_VIEW',
                   'http://developer:iis@localhost:8091/DSA_SQL_JPAService/rest/ecommerce/ProductView');

CREATE OR REPLACE VIEW PRODUCTS_VIEW AS
SELECT v.productId as productId, CAST(v.weight AS DOUBLE) as weight
FROM PRODUCTS_JSON_VIEW LATERAL VIEW explode(array) AS v;

--------------------------------------------------------------------------------
-- 3. IMPORT LOCAȚII (MongoDB -> Spark)
--------------------------------------------------------------------------------
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'LOCATIONS_JSON_VIEW',
                   'http://developer:iis@localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/CityView');

CREATE OR REPLACE VIEW VW_CUSTS_LOCATIONS AS
SELECT v.city as cityName, v.stateName as stateName
FROM LOCATIONS_JSON_VIEW LATERAL VIEW explode(array) AS v;

--------------------------------------------------------------------------------
-- 4. IMPORT ȘI INTEGRARE VÂNZĂRI (CSV + Postgres JOIN)
--------------------------------------------------------------------------------
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'ORDER_ITEMS_JSON_VIEW',
                   'http://developer:iis@localhost:8097/DSA-DOC-CSVService/rest/olist/OrderItems');

-- Pas intermediar: Aplatizăm JSON-ul CSV înainte de JOIN pentru a evita erorile de sintaxă
CREATE OR REPLACE VIEW ORDER_ITEMS_PREPARED AS
SELECT v.product_id as productId, CAST(v.price AS DOUBLE) as sales_amount
FROM ORDER_ITEMS_JSON_VIEW LATERAL VIEW explode(array) AS v;

-- Vederea finală pe care o citește Vaadin
CREATE OR REPLACE VIEW OLAP_FACTS_SALES_AMOUNT AS
SELECT
    o.productId,
    o.sales_amount,
    p.weight,
    CAST(CURRENT_DATE() AS STRING) as invoiceDate
FROM ORDER_ITEMS_PREPARED o
         JOIN PRODUCTS_VIEW p ON o.productId = p.productId;

--------------------------------------------------------------------------------
-- 5. ACTIVARE RUTE REST (Esențial pentru vizualizarea în interfață)
--------------------------------------------------------------------------------
ALTER VIEW PRODUCTS_VIEW SET TBLPROPERTIES('AUTOREST' = 'PRODUCTS_VIEW');
ALTER VIEW VW_CUSTS_LOCATIONS SET TBLPROPERTIES('AUTOREST' = 'VW_CUSTS_LOCATIONS');
ALTER VIEW OLAP_FACTS_SALES_AMOUNT SET TBLPROPERTIES('AUTOREST' = 'OLAP_FACTS_SALES_AMOUNT');

-- Confirmare finală în log-ul Spark
SELECT 'PIPELINE SQL INITIALIZATA' as status;