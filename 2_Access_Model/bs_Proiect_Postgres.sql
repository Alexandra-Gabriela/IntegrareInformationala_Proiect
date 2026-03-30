BEGIN
  DBMS_NETWORK_ACL_ADMIN.append_host_ace(
    host => '127.0.0.1',
    lower_port => 3000,
    upper_port => 3000,
    ace => xs$ace_type(privilege_list => xs$name_list('http'),
                       principal_name => 'FDBO', -- Înlocuiește cu userul tău de Oracle (ex: FDBO, HR, etc.)
                       principal_type => xs_acl.ptype_db));
END;
/


CREATE OR REPLACE FUNCTION get_rest_data(p_url IN VARCHAR2) RETURN CLOB IS
  req   utl_http.req;
  res   utl_http.resp;
  buffer VARCHAR2(32767);
  clob_data CLOB;
BEGIN
  dbms_lob.createtemporary(clob_data, FALSE);
  
  req := utl_http.begin_request(p_url, 'GET', 'HTTP/1.1');
  utl_http.set_header(req, 'Accept', 'application/json');
  utl_http.set_header(req, 'User-Agent', 'Mozilla/4.0');

  res := utl_http.get_response(req);

  BEGIN
    LOOP
      utl_http.read_text(res, buffer);
      dbms_lob.writeappend(clob_data, length(buffer), buffer);
    END LOOP;
  EXCEPTION
    WHEN utl_http.end_of_body THEN
      utl_http.end_response(res);
  END;

  RETURN clob_data;
EXCEPTION
  WHEN OTHERS THEN
    IF res.private_hndl IS NOT NULL THEN
      utl_http.end_response(res);
    END IF;
    RAISE;
END;
/




-- View pentru CATEGORIES
CREATE OR REPLACE VIEW categories_view AS
SELECT
    category_id, 
    product_category_name
FROM JSON_TABLE( get_rest_data('http://127.0.0.1:3000/categories'), '$[*]'
    COLUMNS (
        category_id           NUMBER(5)     PATH '$."category_id"',
        product_category_name VARCHAR2(100) PATH '$."product_category_name"'
    )
);


-- View pentru CATEGORY_TRANSLATIONS
CREATE OR REPLACE VIEW category_translations_view AS
SELECT
    category_id, 
    category_name_en
FROM JSON_TABLE( get_rest_data('http://127.0.0.1:3000/category_translations'), '$[*]'
    COLUMNS (
        category_id      NUMBER(5)     PATH '$."category_id"',
        category_name_en VARCHAR2(100) PATH '$."category_name_en"'
    )
);


-- View pentru PRODUCTS
CREATE OR REPLACE VIEW products_view AS
SELECT
    product_id,
    category_id,
    product_weight_g,
    product_length_cm,
    product_height_cm,
    product_width_cm
FROM JSON_TABLE( get_rest_data('http://127.0.0.1:3000/products'), '$[*]'
    COLUMNS (
        product_id        VARCHAR2(32)  PATH '$."product_id"',
        category_id       NUMBER(5)     PATH '$."category_id"',
        product_weight_g  NUMBER(10,2)  PATH '$."product_weight_g"',
        product_length_cm NUMBER(10,2)  PATH '$."product_length_cm"',
        product_height_cm NUMBER(10,2)  PATH '$."product_height_cm"',
        product_width_cm  NUMBER(10,2)  PATH '$."product_width_cm"'
    )
);



SELECT * FROM categories_view;

SELECT 
    p.product_id, 
    c.product_category_name AS nume_br, 
    t.category_name_en AS nume_en,
    p.product_weight_g
FROM products_view p
JOIN categories_view c ON p.category_id = c.category_id
JOIN category_translations_view t ON c.category_id = t.category_id;







