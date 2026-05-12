----------------------------------------------------------------------------------
-- 1. ANALIZA PARETO (Sursa: Web Service - Port 8096)
----------------------------------------------------------------------------------
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'PARETO_JSON_VIEW',
                   'http://developer:iis@localhost:8096/DSA-WEB-RESTService/rest/ecommerce-analytics/pareto');

CREATE OR REPLACE VIEW VW_PARETO_ANALYTICS AS
SELECT v.* FROM PARETO_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

ALTER VIEW VW_PARETO_ANALYTICS SET TBLPROPERTIES('AUTOREST' = 'VW_PARETO_ANALYTICS');

----------------------------------------------------------------------------------
-- 2. EFICIENȚĂ LOGISTICĂ (Sursa: Web Service - Port 8096)
----------------------------------------------------------------------------------
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'EFFICIENCY_JSON_VIEW',
                   'http://developer:iis@localhost:8096/DSA-WEB-RESTService/rest/ecommerce-analytics/efficiency');

CREATE OR REPLACE VIEW VW_LOGISTICS_EFFICIENCY AS
SELECT v.* FROM EFFICIENCY_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

ALTER VIEW VW_LOGISTICS_EFFICIENCY SET TBLPROPERTIES('AUTOREST' = 'VW_LOGISTICS_EFFICIENCY');