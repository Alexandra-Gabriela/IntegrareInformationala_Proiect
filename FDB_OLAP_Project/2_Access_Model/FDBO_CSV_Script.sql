
CREATE OR REPLACE DIRECTORY EXT_FILE_DS AS 'C:\olist_csv';

-- Tabel: Order Items
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
        READSIZE 20971520             
        SKIP 1                       
        FIELDS TERMINATED BY ',' 
        OPTIONALLY ENCLOSED BY '"'
        LRTRIM                        
        MISSING FIELD VALUES ARE NULL
    )
    LOCATION ('olist_order_items_dataset.csv')
)
REJECT LIMIT UNLIMITED;

-- Tabel: Order Payments
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
        READSIZE 20971520
        SKIP 1
        FIELDS TERMINATED BY ',' 
        OPTIONALLY ENCLOSED BY '"'
        LRTRIM
        MISSING FIELD VALUES ARE NULL
    )
    LOCATION ('olist_order_payments_dataset.csv')
)
REJECT LIMIT UNLIMITED;

CREATE OR REPLACE VIEW V_OLIST_ORDERS_ACCESS AS
SELECT 
    i.order_id              AS V_ORDER_ID,
    i.product_id            AS V_PRODUCT_ID,
    i.price                 AS V_UNIT_PRICE,
    p.payment_type          AS V_PAYMENT_METHOD,
    p.payment_value         AS V_TOTAL_AMOUNT,
    CASE 
        WHEN i.price > 0 AND p.payment_value = 0 THEN 'YES' 
        ELSE 'NO' 
    END                     AS V_IS_FULLY_DISCOUNTED
FROM EXT_ORDER_ITEMS i
LEFT JOIN EXT_ORDER_PAYMENTS p ON i.order_id = p.order_id;


SELECT * FROM V_OLIST_ORDERS_ACCESS WHERE ROWNUM <= 10;