----------------------------------------------------------------------------------
-- 1. ORDER ITEMS (Sursa: Port 8097)
----------------------------------------------------------------------------------
-- Descarcă JSON-ul brut
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'ORDER_ITEMS_JSON_VIEW',
                   'http://developer:iis@localhost:8097/DSA-DOC-CSVService/rest/olist/OrderItems');

-- „Explodează” JSON-ul într-un tabel SQL real
CREATE OR REPLACE VIEW ORDER_ITEMS_VIEW AS
SELECT v.* FROM ORDER_ITEMS_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

-- Permite Vaadin să vadă tabelul
ALTER VIEW ORDER_ITEMS_VIEW SET TBLPROPERTIES('AUTOREST' = 'ORDER_ITEMS_VIEW');


----------------------------------------------------------------------------------
-- 2. ORDER PAYMENTS (Sursa: Port 8097)
----------------------------------------------------------------------------------
SELECT java_method('org.spark.service.rest.RESTEnabledSQLService', 'createJSONViewFromREST',
                   'ORDER_PAYMENTS_JSON_VIEW',
                   'http://developer:iis@localhost:8097/DSA-DOC-CSVService/rest/olist/OrderPayments');

CREATE OR REPLACE VIEW ORDER_PAYMENTS_VIEW AS
SELECT v.* FROM ORDER_PAYMENTS_JSON_VIEW as j LATERAL VIEW explode(j.array) AS v;

ALTER VIEW ORDER_PAYMENTS_VIEW SET TBLPROPERTIES('AUTOREST' = 'ORDER_PAYMENTS_VIEW');