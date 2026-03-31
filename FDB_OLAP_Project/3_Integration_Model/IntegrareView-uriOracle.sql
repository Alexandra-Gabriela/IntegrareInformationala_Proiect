-- VIEW HIBRID: PRODUSE (Postgres) + LOCATII (MongoDB)
CREATE OR REPLACE VIEW product_location_report_view AS
SELECT 
    p.product_id,
    c.product_category_name,
    p.product_weight_g,
    s.seller_id,
    s.seller_city,
    s.lat,
    s.lng,
    r.region_name
FROM products_view p 
JOIN categories_view c 
  ON p.category_id = c.category_id      
LEFT JOIN olist_sellers_view_mongodb s 
  ON p.product_id IS NOT NULL            
LEFT JOIN olist_regions_view_mongodb r 
  ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(r.city_name)); 


SELECT 
    p.product_id, 
    c.product_category_name AS categorie, 
    r.region_name AS regiune_mongo, 
    r.state_name
FROM products_view p
JOIN categories_view c ON p.category_id = c.category_id
JOIN olist_regions_view_mongodb r ON 1=1
WHERE ROWNUM <= 10;


-- VIEW FINAL: INTEGRARE MULTI-SURSA (ECOMMERCE TOTAL)
CREATE OR REPLACE VIEW V_ULTIMATE_OLIST_REPORT AS
SELECT 
    ord.V_ORDER_ID,
    ord.V_PAYMENT_METHOD,
    ord.V_TOTAL_AMOUNT,
    
    p.product_id,
    c.product_category_name AS nume_categorie_pt,
    p.product_weight_g,
    
    s.seller_id AS mongo_seller_id,
    s.seller_city,
    reg.state_name,
    reg.region_name AS macro_regiune
FROM V_OLIST_ORDERS_ACCESS ord                                
JOIN products_view p 
    ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)       
JOIN categories_view c 
    ON p.category_id = c.category_id       
LEFT JOIN olist_sellers_view_mongodb s 
    ON 1=1 
LEFT JOIN olist_regions_view_mongodb reg 
    ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name));


SELECT * FROM V_ULTIMATE_OLIST_REPORT WHERE ROWNUM <= 10;


SELECT 'V_OLIST_ORDERS_ACCESS' AS obiect, COUNT(*) AS exista FROM user_views WHERE view_name = 'V_OLIST_ORDERS_ACCESS'
UNION ALL
SELECT 'PRODUCTS_VIEW', COUNT(*) FROM user_views WHERE view_name = 'PRODUCTS_VIEW'
UNION ALL
SELECT 'CATEGORIES_VIEW', COUNT(*) FROM user_views WHERE view_name = 'CATEGORIES_VIEW'
UNION ALL
SELECT 'OLIST_SELLERS_VIEW_MONGODB', COUNT(*) FROM user_views WHERE view_name = 'OLIST_SELLERS_VIEW_MONGODB'
UNION ALL
SELECT 'OLIST_REGIONS_VIEW_MONGODB', COUNT(*) FROM user_views WHERE view_name = 'OLIST_REGIONS_VIEW_MONGODB';


-- 1. Tabel: Order Items

DROP TABLE EXT_ORDER_ITEMS;
CREATE TABLE EXT_ORDER_ITEMS (
    order_id            VARCHAR2(50),
    order_item_id       NUMBER,
    product_id          VARCHAR2(50),
    seller_id           VARCHAR2(50),
    shipping_limit_date VARCHAR2(30),
    price               NUMBER(15,2),
    freight_value       NUMBER(15,2)
)
ORGANIZATION EXTERNAL (
    TYPE ORACLE_LOADER
    DEFAULT DIRECTORY EXT_FILE_DS
    ACCESS PARAMETERS (
        RECORDS DELIMITED BY 0x'0A'   
        CHARACTERSET AL32UTF8         
        SKIP 1                       
        FIELDS TERMINATED BY ',' 
        OPTIONALLY ENCLOSED BY '"'
        LRTRIM                        
        MISSING FIELD VALUES ARE NULL
    )
    LOCATION ('olist_order_items_dataset.csv')
);

-- 2. Tabel: Order Payments
DROP TABLE EXT_ORDER_PAYMENTS;
CREATE TABLE EXT_ORDER_PAYMENTS (
    order_id               VARCHAR2(50),
    payment_sequential     NUMBER,
    payment_type           VARCHAR2(30),
    payment_installments   NUMBER,
    payment_value          NUMBER(15,2)
)
ORGANIZATION EXTERNAL (
    TYPE ORACLE_LOADER
    DEFAULT DIRECTORY EXT_FILE_DS
    ACCESS PARAMETERS (
        RECORDS DELIMITED BY 0x'0A'
        CHARACTERSET AL32UTF8
        SKIP 1
        FIELDS TERMINATED BY ',' 
        OPTIONALLY ENCLOSED BY '"'
        LRTRIM
        MISSING FIELD VALUES ARE NULL
    )
    LOCATION ('olist_order_payments_dataset.csv')
);


-- 3. View de Acces (V_OLIST_ORDERS_ACCESS)
CREATE OR REPLACE VIEW V_OLIST_ORDERS_ACCESS AS
SELECT 
    i.order_id               AS V_ORDER_ID,
    i.product_id             AS V_PRODUCT_ID,
    i.seller_id              AS V_SELLER_ID,
    i.price                  AS V_UNIT_PRICE,
    p.payment_type           AS V_PAYMENT_METHOD,
    p.payment_value          AS V_TOTAL_AMOUNT,
    CASE 
        WHEN i.price > 0 AND p.payment_value = 0 THEN 'YES' 
        ELSE 'NO' 
    END                      AS V_IS_FULLY_DISCOUNTED
FROM EXT_ORDER_ITEMS i
LEFT JOIN EXT_ORDER_PAYMENTS p ON i.order_id = p.order_id;




CREATE OR REPLACE VIEW V_ULTIMATE_OLIST_REPORT AS
SELECT 
    ord.V_ORDER_ID,
    ord.V_PAYMENT_METHOD,
    p.product_id,
    c.product_category_name AS nume_categorie_pt,
    s.seller_city,
    reg.region_name AS macro_regiune
FROM V_OLIST_ORDERS_ACCESS ord                                
JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)       
JOIN categories_view c ON p.category_id = c.category_id       
LEFT JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
LEFT JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name));


SELECT * FROM V_OLIST_ORDERS_ACCESS



-----------------------------------VIew final de integrare

CREATE OR REPLACE VIEW V_ULTIMATE_OLIST_REPORT AS
SELECT 
    ord.V_ORDER_ID        AS comanda_id,
    ord.V_PAYMENT_METHOD  AS metoda_plata,
    ord.V_TOTAL_AMOUNT    AS valoare_totala,
    
    p.product_id          AS produs_id,
    c.product_category_name AS categorie_portugheza,
    p.product_weight_g    AS greutate_g,
    
    s.seller_id           AS vanzator_id,
    s.seller_city         AS oras_vanzator,
    reg.state_name        AS stat_brazilia,
    reg.region_name       AS macro_regiune
FROM V_OLIST_ORDERS_ACCESS ord                                
JOIN products_view p 
    ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)       
JOIN categories_view c 
    ON p.category_id = c.category_id       
LEFT JOIN olist_sellers_view_mongodb s 
    ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
LEFT JOIN olist_regions_view_mongodb reg 
    ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name));

SELECT macro_regiune, SUM(valoare_totala) as total_vanzari, COUNT(comanda_id) as nr_comenzi
FROM V_ULTIMATE_OLIST_REPORT
WHERE macro_regiune IS NOT NULL
GROUP BY macro_regiune
ORDER BY total_vanzari DESC;

SELECT stat_brazilia, AVG(greutate_g) as greutate_medie
FROM V_ULTIMATE_OLIST_REPORT
WHERE stat_brazilia IS NOT NULL
GROUP BY stat_brazilia
ORDER BY greutate_medie DESC;
