----------------------------------------------------------------------------------
-- 1. PRODUCTS VIEW (Sursă: PostgreSQL port 8091)
----------------------------------------------------------------------------------
-- Descarcă JSON-ul brut pentru produse
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'PRODUCTS_JSON_VIEW',
                   'http://developer:iis@localhost:8091/DSA_SQL_JPAService/rest/ecommerce/ProductView');

-- Transformă JSON-ul în tabel SQL (Explode)
-- Câmpurile (productId, categoryId, weight) corespund cu ProductView.java
CREATE OR REPLACE VIEW PRODUCTS_VIEW AS
SELECT v.* FROM PRODUCTS_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

-- Activează accesul REST
ALTER VIEW PRODUCTS_VIEW SET TBLPROPERTIES('AUTOREST' = 'PRODUCTS_VIEW');

----------------------------------------------------------------------------------
-- 2. CATEGORIES VIEW (Sursă: PostgreSQL port 8091)
----------------------------------------------------------------------------------
-- Descarcă JSON-ul brut pentru categorii
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'CATEGORIES_JSON_VIEW',
                   'http://developer:iis@localhost:8091/DSA_SQL_JPAService/rest/ecommerce/CategoryView');

-- Transformă JSON-ul în tabel SQL
-- Câmpurile (categoryId, categoryName) corespund cu CategoryView.java
CREATE OR REPLACE VIEW CATEGORIES_VIEW AS
SELECT v.* FROM CATEGORIES_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

ALTER VIEW CATEGORIES_VIEW SET TBLPROPERTIES('AUTOREST' = 'CATEGORIES_VIEW');