DROP VIEW IF EXISTS OLAP_FACTS_SALES_AMOUNT;
DROP VIEW IF EXISTS VW_CUSTS_LOCATIONS;
DROP VIEW IF EXISTS PRODUCTS_VIEW;
DROP VIEW IF EXISTS ORDER_ITEMS_PREPARED;
DROP VIEW IF EXISTS VW_INTEGRATED_OLIST;

-- 1. Produse (Postgres)
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST', 'PRODUCTS_JSON_VIEW', 'http://developer:iis@localhost:8091/DSA_SQL_JPAService/rest/ecommerce/ProductView');
CREATE OR REPLACE VIEW PRODUCTS_VIEW AS
SELECT v.productId as productId, v.categoryId as categoryId, CAST(v.weight AS DOUBLE) as weight
FROM PRODUCTS_JSON_VIEW LATERAL VIEW explode(array) AS v;

-- 2. Locații (Mongo) - REPARAT (Adăugăm un customerId fictiv pentru a nu crăpa graficele care numără clienți)
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST', 'LOCATIONS_JSON_VIEW', 'http://developer:iis@localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/CityView');
CREATE OR REPLACE VIEW VW_CUSTS_LOCATIONS AS
SELECT v.city as cityName, v.stateName as stateName, v.city as customerid
FROM LOCATIONS_JSON_VIEW LATERAL VIEW explode(array) AS v;

-- 3. Facts (CSV + Postgres)
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST', 'ORDER_ITEMS_JSON_VIEW', 'http://developer:iis@localhost:8097/DSA-DOC-CSVService/rest/olist/OrderItems');

CREATE OR REPLACE VIEW ORDER_ITEMS_PREPARED AS
SELECT v.productId as productId, CAST(v.price AS DOUBLE) as price
FROM ORDER_ITEMS_JSON_VIEW LATERAL VIEW explode(array) AS v;

-- Cream ambele vederi (alias) pentru a satisface toate componentele Vaadin
CREATE OR REPLACE VIEW OLAP_FACTS_SALES_AMOUNT AS
SELECT o.productId, o.price as sales_amount, p.weight, CAST(CURRENT_DATE() AS STRING) as invoiceDate
FROM ORDER_ITEMS_PREPARED o
         JOIN PRODUCTS_VIEW p ON o.productId = p.productId;

-- FIX: Cream și VW_INTEGRATED_OLIST pentru graficele care o caută
CREATE OR REPLACE VIEW VW_INTEGRATED_OLIST AS
SELECT * FROM OLAP_FACTS_SALES_AMOUNT;

-- 4. ACTIVARE REST
ALTER VIEW PRODUCTS_VIEW SET TBLPROPERTIES('AUTOREST' = 'PRODUCTS_VIEW');
ALTER VIEW VW_CUSTS_LOCATIONS SET TBLPROPERTIES('AUTOREST' = 'VW_CUSTS_LOCATIONS');
ALTER VIEW OLAP_FACTS_SALES_AMOUNT SET TBLPROPERTIES('AUTOREST' = 'OLAP_FACTS_SALES_AMOUNT');
ALTER VIEW VW_INTEGRATED_OLIST SET TBLPROPERTIES('AUTOREST' = 'VW_INTEGRATED_OLIST');