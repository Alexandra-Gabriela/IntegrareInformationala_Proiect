BEGIN
  DBMS_NETWORK_ACL_ADMIN.append_host_ace(
    host => '127.0.0.1',
    lower_port => 3000,
    upper_port => 3000,
    ace => xs$ace_type(privilege_list => xs$name_list('http'),
                       principal_name => 'SYS', 
                       principal_type => xs_acl.ptype_db));

  DBMS_NETWORK_ACL_ADMIN.append_host_ace(
    host => '127.0.0.1',
    lower_port => 8081,
    upper_port => 8081,
    ace => xs$ace_type(privilege_list => xs$name_list('http'),
                       principal_name => 'SYS', 
                       principal_type => xs_acl.ptype_db));
END;
/


SELECT status FROM all_objects WHERE owner = 'SYS' AND object_name = 'V_ULTIMATE_OLIST_REPORT';


GRANT SELECT ON SYS.V_OLIST_ORDERS_ACCESS TO FDBO;
GRANT SELECT ON SYS.products_view TO FDBO;
GRANT SELECT ON SYS.categories_view TO FDBO;
GRANT SELECT ON SYS.olist_sellers_view_mongodb TO FDBO;
GRANT SELECT ON SYS.olist_regions_view_mongodb TO FDBO;

GRANT EXECUTE ON SYS.get_rest_data TO FDBO;
GRANT EXECUTE ON SYS.get_restheart_data_media TO FDBO;


GRANT SELECT ON SYS.PRODUCTS_VIEW TO FDBO;
GRANT SELECT ON SYS.V_OLIST_ORDERS_ACCESS TO FDBO;
GRANT SELECT ON SYS.OLIST_SELLERS_VIEW_MONGODB TO FDBO;
GRANT SELECT ON SYS.V_ULTIMATE_OLIST_REPORT TO FDBO;


CREATE OR REPLACE VIEW FDBO.PRODUCTS_VIEW AS SELECT * FROM SYS.PRODUCTS_VIEW;
CREATE OR REPLACE VIEW FDBO.V_OLIST_ORDERS_ACCESS AS SELECT * FROM SYS.V_OLIST_ORDERS_ACCESS;
CREATE OR REPLACE VIEW FDBO.OLIST_SELLERS_VIEW_MONGODB AS SELECT * FROM SYS.OLIST_SELLERS_VIEW_MONGODB;
CREATE OR REPLACE VIEW FDBO.V_ULTIMATE_OLIST_REPORT AS SELECT * FROM SYS.V_ULTIMATE_OLIST_REPORT;

CREATE OR REPLACE VIEW FDBO.V_ULTIMATE_OLIST_REPORT AS SELECT * FROM SYS.V_ULTIMATE_OLIST_REPORT;

COMMIT;

BEGIN
  ORDS.ENABLE_SCHEMA(
    p_enabled => TRUE,
    p_schema => 'FDBO',
    p_url_mapping_type => 'BASE_PATH',
    p_url_mapping_pattern => 'fdbo',
    p_auto_rest_auth => FALSE
  );

  ORDS.ENABLE_OBJECT(
    p_enabled => TRUE,
    p_schema => 'FDBO',
    p_object => 'V_ULTIMATE_OLIST_REPORT',
    p_object_type => 'VIEW',
    p_object_alias => 'ultimate_report',
    p_auto_rest_auth => FALSE
  );
  COMMIT;
END;
/


ALTER USER ORDS_PUBLIC_USER IDENTIFIED BY 1234 ACCOUNT UNLOCK;

BEGIN
    ORDS.ENABLE_OBJECT(p_enabled => TRUE, 
                       p_schema => 'FDBO', 
                       p_object => 'V_ULTIMATE_OLIST_REPORT', 
                       p_object_type => 'VIEW', 
                       p_object_alias => 'ultimate_report', 
                       p_auto_rest_auth => FALSE);
    COMMIT;
END;
/

-------------------------endpoint-uri
-------------------------------------------------------------------------

BEGIN
    ORDS.ENABLE_SCHEMA(
        p_enabled => FALSE,
        p_schema  => 'FDBO'
    );
    
    ORDS.drop_rest_for_schema('FDBO');
    
    COMMIT;
END;
/

BEGIN
    ORDS.ENABLE_SCHEMA(p_enabled => TRUE,
                       p_schema => 'FDBO',
                       p_url_mapping_type => 'BASE_PATH',
                       p_url_mapping_pattern => 'fdbo',
                       p_auto_rest_auth => FALSE);
    COMMIT;
END;
/


----http://localhost:8080/ords/fdbo/metadata-catalog/
----http://localhost:8080/ords/fdbo/ultimate_report/

BEGIN
    -- Endpoint 1: Raportul Complet 
    -- URL: http://localhost:8080/ords/fdbo/ultimate_report/
    ORDS.ENABLE_OBJECT(p_enabled => TRUE,
                       p_schema => 'FDBO',
                       p_object => 'V_ULTIMATE_OLIST_REPORT',
                       p_object_type => 'VIEW',
                       p_object_alias => 'ultimate_report',
                       p_auto_rest_auth => FALSE);

    -- Endpoint 2: Sursa de Date CSV (Orders)
    -- URL: http://localhost:8080/ords/fdbo/orders_csv/
    ORDS.ENABLE_OBJECT(p_enabled => TRUE,
                       p_schema => 'FDBO',
                       p_object => 'V_OLIST_ORDERS_ACCESS',
                       p_object_type => 'VIEW',
                       p_object_alias => 'orders_csv',
                       p_auto_rest_auth => FALSE);

    -- Endpoint 3: Sursa de Date Postgres (Products)
    -- URL: http://localhost:8080/ords/fdbo/products_postgres/
    ORDS.ENABLE_OBJECT(p_enabled => TRUE,
                       p_schema => 'FDBO',
                       p_object => 'PRODUCTS_VIEW',
                       p_object_type => 'VIEW',
                       p_object_alias => 'products_postgres',
                       p_auto_rest_auth => FALSE);

    -- Endpoint 4: Sursa de Date MongoDB (Sellers/Locations)
    -- URL: http://localhost:8080/ords/fdbo/sellers_mongo/
    ORDS.ENABLE_OBJECT(p_enabled => TRUE,
                       p_schema => 'FDBO',
                       p_object => 'OLIST_SELLERS_VIEW_MONGODB',
                       p_object_type => 'VIEW',
                       p_object_alias => 'sellers_mongo',
                       p_auto_rest_auth => FALSE);

    COMMIT;
END;
/
---------------------------------

--http://localhost:8080/ords/fdbo/analiza_regiuni/
CREATE OR REPLACE VIEW FDBO.OLIST_REGIONS_VIEW_MONGODB AS 
SELECT * FROM SYS.OLIST_REGIONS_VIEW_MONGODB;

CREATE OR REPLACE VIEW FDBO.V_REGIONAL_SALES_REPORT AS
SELECT 
    reg.region_name AS regiune_mongo,
    COUNT(ord.V_ORDER_ID) AS numar_comenzi,
    SUM(ord.V_TOTAL_AMOUNT) AS vanzari_totale_csv,
    AVG(p.product_weight_g) AS greutate_medie_produs_postgres
FROM FDBO.V_OLIST_ORDERS_ACCESS ord
JOIN FDBO.PRODUCTS_VIEW p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
LEFT JOIN FDBO.OLIST_SELLERS_VIEW_MONGODB s ON 1=1 -- sau join pe seller_id dacă există
LEFT JOIN FDBO.OLIST_REGIONS_VIEW_MONGODB reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
GROUP BY reg.region_name;


BEGIN
    ORDS.ENABLE_OBJECT(
        p_enabled      => TRUE,
        p_schema       => 'FDBO',
        p_object       => 'V_REGIONAL_SALES_REPORT',
        p_object_type  => 'VIEW',
        p_object_alias => 'analiza_regiuni',
        p_auto_rest_auth => FALSE
    );
    COMMIT;
END;
/

------------------------------------------------------
------------------------------------------------------

GRANT EXECUTE ON ORDS_METADATA.ORDS TO FDBO;
GRANT SELECT ON FDBO.V_PAYMENT_BY_REGION TO ORDS_METADATA;
GRANT SELECT ON FDBO.V_SELLER_PERFORMANCE TO ORDS_METADATA;

--http://localhost:8080/ords/fdbo/plati/
CREATE OR REPLACE VIEW FDBO.V_PAYMENT_BY_REGION AS
SELECT 
    reg.region_name         AS macro_regiune,
    ord.V_PAYMENT_METHOD    AS metoda_plata,
    COUNT(*)                AS numar_utilizari,
    ROUND(AVG(ord.V_TOTAL_AMOUNT), 2) AS valoare_medie_tranzactie
FROM FDBO.V_OLIST_ORDERS_ACCESS ord
LEFT JOIN FDBO.OLIST_SELLERS_VIEW_MONGODB s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
LEFT JOIN FDBO.OLIST_REGIONS_VIEW_MONGODB reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
WHERE reg.region_name IS NOT NULL
GROUP BY reg.region_name, ord.V_PAYMENT_METHOD
ORDER BY reg.region_name, numar_utilizari DESC;

--http://localhost:8080/ords/fdbo/hibrid/performanta_vanzatori
CREATE OR REPLACE VIEW FDBO.V_SELLER_PERFORMANCE AS
SELECT 
    s.seller_city           AS oras_vanzator,
    p.category_id           AS id_categorie_postgres,
    COUNT(ord.V_ORDER_ID)   AS total_comenzi,
    SUM(ord.V_TOTAL_AMOUNT) AS total_incasari_csv
FROM FDBO.V_OLIST_ORDERS_ACCESS ord
JOIN FDBO.PRODUCTS_VIEW p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN FDBO.OLIST_SELLERS_VIEW_MONGODB s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
GROUP BY s.seller_city, p.category_id
HAVING COUNT(ord.V_ORDER_ID) > 5
ORDER BY total_incasari_csv DESC;

COMMIT;

BEGIN
    ORDS.ENABLE_OBJECT(
        p_enabled      => TRUE,
        p_schema       => 'FDBO',
        p_object       => 'V_PAYMENT_BY_REGION',
        p_object_type  => 'VIEW',
        p_object_alias => 'plati',
        p_auto_rest_auth => FALSE
    );
    COMMIT;
END;
/

BEGIN
    ORDS.ENABLE_SCHEMA(
        p_enabled             => TRUE,
        p_schema              => 'FDBO',
        p_url_mapping_type    => 'BASE_PATH',
        p_url_mapping_pattern => 'fdbo',
        p_auto_rest_auth      => FALSE
    );

    ORDS.ENABLE_OBJECT(
        p_enabled      => TRUE,
        p_schema       => 'FDBO',
        p_object       => 'V_SELLER_PERFORMANCE',
        p_object_type  => 'VIEW',
        p_object_alias => 'performanta',
        p_auto_rest_auth => FALSE
    );

    COMMIT;
END;
/

--http://localhost:8080/ords/fdbo/performanta/


GRANT CREATE VIEW, RESOURCE, CONNECT TO FDBO;

GRANT SELECT ON SYS.V_OLIST_ORDERS_ACCESS TO FDBO;
GRANT SELECT ON SYS.PRODUCTS_VIEW TO FDBO;
GRANT SELECT ON SYS.CATEGORIES_VIEW TO FDBO;
GRANT SELECT ON SYS.OLIST_SELLERS_VIEW_MONGODB TO FDBO;
GRANT SELECT ON SYS.OLIST_REGIONS_VIEW_MONGODB TO FDBO;

COMMIT;



BEGIN
    EXECUTE IMMEDIATE 'DROP VIEW VW_PARETO_ORASE';
    EXECUTE IMMEDIATE 'DROP VIEW VW_DOMINANTA_CATEGORII';
    EXECUTE IMMEDIATE 'DROP VIEW VW_EFICIENTA_LOGISTICA';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/


-- Analiza Pareto
CREATE OR REPLACE VIEW VW_PARETO_ORASE AS
WITH CitySales AS (
    SELECT s.seller_city, reg.region_name, SUM(ord.V_TOTAL_AMOUNT) AS venit_oras
    FROM SYS.V_OLIST_ORDERS_ACCESS ord
    JOIN SYS.OLIST_SELLERS_VIEW_MONGODB s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
    JOIN SYS.OLIST_REGIONS_VIEW_MONGODB reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
    GROUP BY s.seller_city, reg.region_name
),
RunningStats AS (
    SELECT region_name AS macro_regiune, seller_city, venit_oras,
           SUM(venit_oras) OVER (PARTITION BY region_name ORDER BY venit_oras DESC) AS cumulative_sales,
           SUM(venit_oras) OVER (PARTITION BY region_name) AS total_regiune
    FROM CitySales
)
SELECT macro_regiune, seller_city, venit_oras,
       ROUND((cumulative_sales / NULLIF(total_regiune, 0)) * 100, 2) AS procent_cumulat
FROM RunningStats;

-- Dominanța Categoriilor
CREATE OR REPLACE VIEW VW_DOMINANTA_CATEGORII AS
SELECT reg.region_name AS regiune, c.product_category_name AS categorie,
       SUM(ord.V_TOTAL_AMOUNT) AS vanzari_categorie,
       ROUND(100 * RATIO_TO_REPORT(SUM(ord.V_TOTAL_AMOUNT)) OVER (PARTITION BY reg.region_name), 2) AS cota_piata
FROM SYS.V_OLIST_ORDERS_ACCESS ord
JOIN SYS.PRODUCTS_VIEW p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN SYS.CATEGORIES_VIEW c ON p.category_id = c.category_id
JOIN SYS.OLIST_SELLERS_VIEW_MONGODB s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
JOIN SYS.OLIST_REGIONS_VIEW_MONGODB reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
GROUP BY reg.region_name, c.product_category_name;

-- Eficiența Logistică
CREATE OR REPLACE VIEW VW_EFICIENTA_LOGISTICA AS
SELECT reg.state_name AS stat_brazilia,
       ROUND(SUM(ord.V_TOTAL_AMOUNT) / NULLIF(SUM(p.product_weight_g / 1000), 0), 2) AS venit_per_kg
FROM SYS.V_OLIST_ORDERS_ACCESS ord
JOIN SYS.PRODUCTS_VIEW p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN SYS.OLIST_SELLERS_VIEW_MONGODB s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
JOIN SYS.OLIST_REGIONS_VIEW_MONGODB reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
GROUP BY reg.state_name
HAVING SUM(p.product_weight_g) > 0;


CREATE OR REPLACE VIEW FDBO.VW_PARETO_ORASE AS 
SELECT * FROM SYS.V_REST_PARETO_ORASE;

SELECT owner, object_name, status 
FROM all_objects 
WHERE object_name = 'VW_PARETO_ORASE' AND owner = 'FDBO';

BEGIN
    ORDS.ENABLE_SCHEMA(
        p_enabled             => TRUE,
        p_schema              => 'FDBO',
        p_url_mapping_pattern => 'fdbo'
    );

    ORDS.ENABLE_OBJECT(
        p_enabled      => TRUE,
        p_schema       => 'FDBO',           
        p_object       => 'VW_PARETO_ORASE', 
        p_object_type  => 'VIEW',
        p_object_alias => 'pareto-analiza'
    );

    COMMIT;
END;
/

-- 1. Dominanța Categoriilor
CREATE OR REPLACE VIEW FDBO.VW_DOMINANTA_CATEGORII AS 
SELECT * FROM SYS.V_REST_DOMINANTA_CATEGORII;

-- 2. Eficiența Logistică
CREATE OR REPLACE VIEW FDBO.VW_EFICIENTA_LOGISTICA AS 
SELECT * FROM SYS.V_REST_EFICIENTA_LOGISTICA;


BEGIN
    -- Activăm fiecare obiect nou creat în FDBO
    
    -- URL: .../fdbo/categorii-regiuni/
    ORDS.ENABLE_OBJECT(
        p_enabled      => TRUE, 
        p_schema       => 'FDBO', 
        p_object       => 'VW_DOMINANTA_CATEGORII', 
        p_object_type  => 'VIEW',
        p_object_alias => 'categorii-regiuni'
    );

    -- URL: .../fdbo/eficienta-logistica/
    ORDS.ENABLE_OBJECT(
        p_enabled      => TRUE, 
        p_schema       => 'FDBO', 
        p_object       => 'VW_EFICIENTA_LOGISTICA', 
        p_object_type  => 'VIEW',
        p_object_alias => 'eficienta-logistica'
    );

    COMMIT;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER SESSION SET CURRENT_SCHEMA = FDBO';

    ORDS.ENABLE_SCHEMA(
        p_enabled             => TRUE,
        p_schema              => 'FDBO',
        p_url_mapping_pattern => 'fdbo',
        p_auto_rest_auth      => FALSE
    );

    ORDS_METADATA.OAUTH.CREATE_CLIENT(
        p_name            => 'analyst_app',
        p_grant_type      => 'client_credentials',
        p_owner           => 'FDBO', 
        p_description     => 'Client analize Olist',
        p_support_email   => 'admin@olist.ro',
        p_privilege_names => 'PRIV_HIBRID_REPORTS'
    );

    COMMIT;
    
    EXECUTE IMMEDIATE 'ALTER SESSION SET CURRENT_SCHEMA = SYS';
    DBMS_OUTPUT.PUT_LINE('SUCCES! Clientul a fost creat.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Eroare: ' || SQLERRM);
        EXECUTE IMMEDIATE 'ALTER SESSION SET CURRENT_SCHEMA = SYS';
END;
/


BEGIN
    EXECUTE IMMEDIATE 'ALTER SESSION SET CURRENT_SCHEMA = FDBO';

    ORDS.ENABLE_SCHEMA(
        p_enabled             => TRUE,
        p_schema              => 'FDBO',
        p_url_mapping_pattern => 'fdbo',
        p_auto_rest_auth      => FALSE
    );
    
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Schema FDBO a fost resetata. Incearca acum in browser.');
END;
/
