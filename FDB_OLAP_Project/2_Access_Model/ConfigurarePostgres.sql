CREATE ROLE ecommerce_admin WITH
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    INHERIT
    NOREPLICATION
    CONNECTION LIMIT -1
    PASSWORD '1234';

CREATE SCHEMA ecommerce AUTHORIZATION ecommerce_admin;

SET search_path TO ecommerce;

DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS category_translations;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    category_id NUMERIC(5)
        CONSTRAINT pk_categories PRIMARY KEY
        CONSTRAINT ck_category_id CHECK (category_id > 0),
    product_category_name VARCHAR(100)
        CONSTRAINT nn_category_name NOT NULL
);

CREATE TABLE category_translations (
    category_id NUMERIC(5)
        CONSTRAINT pk_translations PRIMARY KEY
        REFERENCES categories(category_id),
    category_name_en VARCHAR(100)
        CONSTRAINT nn_category_name_en NOT NULL
);

CREATE TABLE products (
    product_id CHAR(32)
        CONSTRAINT pk_products PRIMARY KEY,
    category_id NUMERIC(5)
        REFERENCES categories(category_id),
    product_name_lenght NUMERIC(5),
    product_description_lenght NUMERIC(10),
    product_photos_qty NUMERIC(3),
    product_weight_g NUMERIC(10,2)
        CONSTRAINT ck_weight CHECK (product_weight_g > 0),
    product_length_cm NUMERIC(10,2),
    product_height_cm NUMERIC(10,2),
    product_width_cm NUMERIC(10,2)
);


SELECT 
    p.product_id, 
    c.product_category_name AS nume_ro_br, 
    t.category_name_en AS nume_en,
    p.product_weight_g AS greutate
FROM products p
INNER JOIN categories c ON p.category_id = c.category_id
INNER JOIN category_translations t ON c.category_id = t.category_id;

SELECT category_id, category_name_en FROM category_translations;



ALTER TABLE ecommerce.products ALTER COLUMN category_id TYPE NUMERIC;
ALTER TABLE ecommerce.products ALTER COLUMN product_name_lenght TYPE NUMERIC;
ALTER TABLE ecommerce.products ALTER COLUMN product_description_lenght TYPE NUMERIC;
ALTER TABLE ecommerce.products ALTER COLUMN product_photos_qty TYPE NUMERIC;
ALTER TABLE ecommerce.products DROP CONSTRAINT IF EXISTS ck_weight;
ALTER TABLE ecommerce.products ADD CONSTRAINT ck_weight CHECK (product_weight_g >= 0);


select * from categories


GRANT USAGE ON SCHEMA ecommerce TO ecommerce_admin;

GRANT SELECT ON ALL TABLES IN SCHEMA ecommerce TO ecommerce_admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA ecommerce GRANT SELECT ON TABLES TO ecommerce_admin;

GRANT SELECT ON ALL TABLES IN SCHEMA information_schema TO ecommerce_admin;
GRANT SELECT ON ALL TABLES IN SCHEMA pg_catalog TO ecommerce_admin;