
-- 28_AM_JSON_MongoDB_View_Olist_Final

DROP VIEW olist_sellers_view_mongodb;
DROP VIEW olist_regions_view_mongodb;
DROP VIEW olist_full_hierarchy_view_mongodb;

-- 1. FUNCTIA DE CONECTARE
CREATE OR REPLACE FUNCTION get_restheart_data_media(pURL VARCHAR2, pUserPass VARCHAR2) 
RETURN clob IS
  l_req    UTL_HTTP.req;
  l_resp   UTL_HTTP.resp;
  l_buffer clob; 
begin
  l_req  := UTL_HTTP.begin_request(pURL);
  UTL_HTTP.set_header(l_req, 'Authorization', 'Basic ' || 
    UTL_RAW.cast_to_varchar2(UTL_ENCODE.base64_encode(UTL_I18N.string_to_raw(pUserPass, 'AL32UTF8')))); 
  l_resp := UTL_HTTP.get_response(l_req);
  UTL_HTTP.READ_TEXT(l_resp, l_buffer);
  UTL_HTTP.end_response(l_resp);
  return l_buffer;
end;
/


-- 2. TESTARE CONEXIUNE
SELECT get_restheart_data_media('http://127.0.0.1:8081/mds/Locations', 'admin:secret') as test_json from dual;


-- 3. VIEW PENTRU SELLERS (Colecția Locations)
CREATE OR REPLACE VIEW olist_sellers_view_mongodb AS
with json as
    (select get_restheart_data_media('http://127.0.0.1:8081/mds/Locations', 'admin:secret') doc from dual)
SELECT seller_id, seller_city, lat, lng
FROM JSON_TABLE( (select doc from json) , '$[*]'  
    COLUMNS ( 
        seller_id    VARCHAR2(50)  PATH '$.seller_id',
        seller_city  VARCHAR2(100) PATH '$.seller_city',
        nested PATH '$.location' 
            COLUMNS (
                lat  NUMBER PATH '$.lat',
                lng  NUMBER PATH '$.lng'
            )
    )  
);

-- 4. VIEW PENTRU REGIUNI (Colecția Regions)
-- Modelul ierarhic: Region -> States[*] -> Cities[*]
CREATE OR REPLACE VIEW olist_regions_view_mongodb AS
with json as
    (select get_restheart_data_media('http://127.0.0.1:8081/mds/Regions', 'admin:secret') doc from dual)
SELECT region_name, state_name, city_name
FROM JSON_TABLE( (select doc from json) , '$[*]'  
    COLUMNS ( 
        region_name PATH '$.region',
        nested PATH '$.states[*]' 
            COLUMNS (
                state_name VARCHAR2(50) PATH '$.state_name',
                nested PATH '$.cities[*]' 
                    COLUMNS (
                        city_name VARCHAR2(100) PATH '$.city_name'
                    )
            )
    )  
);


-- 5. VIEW FINAL
CREATE OR REPLACE VIEW olist_full_hierarchy_view_mongodb AS
SELECT 
    s.seller_id, 
    s.seller_city, 
    r.state_name, 
    r.region_name as macro_region,
    s.lat,
    s.lng
FROM olist_sellers_view_mongodb s
JOIN olist_regions_view_mongodb r ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(r.city_name));

-- 6. AFISARE REZULTATE
SELECT * FROM olist_full_hierarchy_view_mongodb WHERE ROWNUM <= 20;