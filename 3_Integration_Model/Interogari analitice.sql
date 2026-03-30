--Analiza "Pareto" și Segmentarea Clienților (60/20/20)
--calculează contribuția procentuală a fiecărui oraș (din MongoDB) la vânzările totale (CSV) și clasifică orașele în categorii de importanță, folosind un total rulat pe fereastră variabilă
WITH CitySales AS (
    SELECT 
        s.seller_city,
        reg.region_name,
        SUM(ord.V_TOTAL_AMOUNT) AS venit_oras
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
    JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
    GROUP BY s.seller_city, reg.region_name
),
RunningStats AS (
    SELECT 
        region_name AS macro_regiune,
        seller_city,
        venit_oras,
        SUM(venit_oras) OVER (PARTITION BY region_name ORDER BY venit_oras DESC) AS cumulative_sales,
        SUM(venit_oras) OVER (PARTITION BY region_name) AS total_regiune
    FROM CitySales
)
SELECT 
    macro_regiune,
    seller_city,
    venit_oras,
    ROUND((cumulative_sales / NULLIF(total_regiune, 0)) * 100, 2) || '%' AS procent_cumulat_regiune,
    CASE 
        WHEN (cumulative_sales / NULLIF(total_regiune, 0)) <= 0.60 THEN 'TOP A (60% Venit)'
        WHEN (cumulative_sales / NULLIF(total_regiune, 0)) <= 0.85 THEN 'MEDIUM B (25% Venit)'
        ELSE 'LOW C (Restul)'
    END AS segmentare_prioritate
FROM RunningStats
ORDER BY macro_regiune, venit_oras DESC;


--------------------

SELECT * FROM V_ULTIMATE_OLIST_REPORT WHERE ROWNUM <= 10;


--2 Analiza Dominanței Categoriilor pe Macro-Regiuni Braziliene
SELECT 
    reg.region_name AS regiune,
    c.product_category_name AS categorie,
    SUM(ord.V_TOTAL_AMOUNT) AS vanzari_categorie,
    -- RATIO_TO_REPORT: Calculeaza ponderea categoriei in totalul regiunii
    ROUND(100 * RATIO_TO_REPORT(SUM(ord.V_TOTAL_AMOUNT)) OVER (PARTITION BY reg.region_name), 2) || '%' AS cota_piata_regiune,
    -- RANK: Clasamentul categoriei in regiune
    RANK() OVER (PARTITION BY reg.region_name ORDER BY SUM(ord.V_TOTAL_AMOUNT) DESC) AS pozitie_top
FROM V_OLIST_ORDERS_ACCESS ord
JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN categories_view c ON p.category_id = c.category_id
JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
GROUP BY reg.region_name, c.product_category_name
ORDER BY regiune, vanzari_categorie DESC;


--3 Raport de Eficiență Logistică: Venit per Kilogram per Stat"
SELECT 
    reg.state_name AS stat_brazilia,
    ROUND(AVG(p.product_weight_g), 2) AS greutate_medie_g,
    ROUND(SUM(ord.V_TOTAL_AMOUNT) / NULLIF(SUM(p.product_weight_g / 1000), 0), 2) AS venit_per_kg,
    -- Analiza ferestrei: Media venitului pe kg la nivel de intreaga tara pentru comparatie
    ROUND(AVG(SUM(ord.V_TOTAL_AMOUNT) / NULLIF(SUM(p.product_weight_g / 1000), 0)) OVER (), 2) AS medie_nationala_venit_kg
FROM V_OLIST_ORDERS_ACCESS ord
JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
GROUP BY reg.state_name
HAVING SUM(p.product_weight_g) > 0
ORDER BY venit_per_kg DESC;

---4. Segmentarea Tranzacțiilor: Impactul Metodei de Plată asupra Valorii Comenzii

SELECT 
    c.product_category_name AS categorie,
    ord.V_PAYMENT_METHOD AS metoda_plata,
    ord.V_TOTAL_AMOUNT AS valoare_comanda,
    -- PERCENT_RANK: Ne spune in ce percentila se afla comanda fata de restul din categoria sa
    ROUND(PERCENT_RANK() OVER (PARTITION BY c.product_category_name ORDER BY ord.V_TOTAL_AMOUNT), 2) AS percentila_valoare,
    -- NTILE: Imparte comenzile in 4 grupuri egale (Quartile)
    NTILE(4) OVER (PARTITION BY c.product_category_name ORDER BY ord.V_TOTAL_AMOUNT) AS quartila_profit
FROM V_OLIST_ORDERS_ACCESS ord
JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN categories_view c ON p.category_id = c.category_id
WHERE ord.V_TOTAL_AMOUNT > 0
ORDER BY categorie, valoare_comanda DESC;

---5. Consolidarea Vânzărilor pe Niveluri: Regiune > Stat > Categorie
SELECT 
    NVL(reg.region_name, '{TOATE REGIUNILE}') AS regiune,
    NVL(reg.state_name, '{TOATE STATELE}') AS stat,
    NVL(c.product_category_name, '{TOATE CATEGORIILE}') AS categorie,
    COUNT(*) AS nr_comenzi,
    SUM(ord.V_TOTAL_AMOUNT) AS total_venit
FROM V_OLIST_ORDERS_ACCESS ord
JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN categories_view c ON p.category_id = c.category_id
JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
GROUP BY GROUPING SETS (
    (reg.region_name, reg.state_name, c.product_category_name),
    (reg.region_name, reg.state_name),
    (reg.region_name),
    ()
)
ORDER BY reg.region_name, reg.state_name, total_venit DESC;


---6. Identificarea Outlierilor: Detectarea Produselor cu Prețuri Anormale în Categorie
WITH CategoryStats AS (
    SELECT 
        c.product_category_name AS categorie,
        p.product_id,
        ord.V_TOTAL_AMOUNT AS pret,
        -- Calculam media si deviatia standard pe categorie (Window Functions)
        AVG(ord.V_TOTAL_AMOUNT) OVER (PARTITION BY c.product_category_name) AS medie_cat,
        STDDEV(ord.V_TOTAL_AMOUNT) OVER (PARTITION BY c.product_category_name) AS stddev_cat
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
    JOIN categories_view c ON p.category_id = c.category_id
)
SELECT 
    categorie,
    product_id,
    pret,
    ROUND((pret - medie_cat) / NULLIF(stddev_cat, 0), 2) AS z_score,
    CASE 
        WHEN ABS((pret - medie_cat) / NULLIF(stddev_cat, 0)) > 2 THEN 'ANOMALIE/OUTLIER'
        ELSE 'NORMAL'
    END AS status_pret
FROM CategoryStats
WHERE stddev_cat > 0
ORDER BY ABS(z_score) DESC;

--7. Corelația Categoriilor: Ce Categorii tind să fie cumpărate în aceeași Regiune?
WITH OrderMap AS (
    SELECT 
        ord.V_ORDER_ID,
        c.product_category_name AS categorie,
        reg.region_name AS regiune
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
    JOIN categories_view c ON p.category_id = c.category_id
    JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
    JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
)
SELECT 
    t1.regiune,
    t1.categorie AS categoria_A,
    t2.categorie AS categoria_B,
    COUNT(*) AS frecventa_asociere,
    RANK() OVER (PARTITION BY t1.regiune ORDER BY COUNT(*) DESC) AS top_asociere
FROM OrderMap t1
JOIN OrderMap t2 ON t1.V_ORDER_ID = t2.V_ORDER_ID AND t1.categorie < t2.categorie
GROUP BY t1.regiune, t1.categorie, t2.categorie
ORDER BY t1.regiune, frecventa_asociere DESC;

--8 Analiza Pareto: Identificarea Statelor care Generează 80% din Venituri"
WITH StateSales AS (
    SELECT 
        reg.state_name,
        SUM(ord.V_TOTAL_AMOUNT) AS venit_stat,
        ROUND(AVG(p.product_weight_g), 2) AS greutate_medie_kg
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
    JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
    JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
    GROUP BY reg.state_name
),
RunningTotals AS (
    SELECT 
        state_name,
        venit_stat,
        SUM(venit_stat) OVER (ORDER BY venit_stat DESC) AS venit_cumulat,
        SUM(venit_stat) OVER () AS venit_total_tara
    FROM StateSales
)
SELECT 
    state_name,
    venit_stat,
    ROUND((venit_cumulat / venit_total_tara) * 100, 2) AS procent_cumulat,
    CASE 
        WHEN (venit_cumulat / venit_total_tara) <= 0.80 THEN 'Motor Economic (Top 80%)'
        ELSE 'Restul Pietei'
    END AS segmentare_pareto
FROM RunningTotals
ORDER BY venit_stat DESC;

--9 Identificarea Outlierilor: Detectarea Produselor cu Prețuri Anormale în Categorie
WITH CategoryStats AS (
    SELECT 
        c.product_category_name AS categorie,
        p.product_id,
        ord.V_TOTAL_AMOUNT AS pret,
        -- Calculam media si deviatia standard pe categorie (Window Functions)
        AVG(ord.V_TOTAL_AMOUNT) OVER (PARTITION BY c.product_category_name) AS medie_cat,
        STDDEV(ord.V_TOTAL_AMOUNT) OVER (PARTITION BY c.product_category_name) AS stddev_cat
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
    JOIN categories_view c ON p.category_id = c.category_id
)
SELECT 
    categorie,
    product_id,
    pret,
    ROUND((pret - medie_cat) / NULLIF(stddev_cat, 0), 2) AS z_score,
    CASE 
        WHEN ABS((pret - medie_cat) / NULLIF(stddev_cat, 0)) > 2 THEN 'ANOMALIE/OUTLIER'
        ELSE 'NORMAL'
    END AS status_pret
FROM CategoryStats
WHERE stddev_cat > 0
ORDER BY ABS(z_score) DESC;

--10. Corelația Categoriilor: Ce Categorii tind să fie cumpărate în aceeași Regiune?
WITH OrderMap AS (
    SELECT 
        ord.V_ORDER_ID,
        c.product_category_name AS categorie,
        reg.region_name AS regiune
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
    JOIN categories_view c ON p.category_id = c.category_id
    JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
    JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
)
SELECT 
    t1.regiune,
    t1.categorie AS categoria_A,
    t2.categorie AS categoria_B,
    COUNT(*) AS frecventa_asociere,
    RANK() OVER (PARTITION BY t1.regiune ORDER BY COUNT(*) DESC) AS top_asociere
FROM OrderMap t1
JOIN OrderMap t2 ON t1.V_ORDER_ID = t2.V_ORDER_ID AND t1.categorie < t2.categorie
GROUP BY t1.regiune, t1.categorie, t2.categorie
ORDER BY t1.regiune, frecventa_asociere DESC;

--11.Analiza Pareto: Identificarea Statelor care Generează 80% din Venituri
WITH StateSales AS (
    SELECT 
        reg.state_name,
        SUM(ord.V_TOTAL_AMOUNT) AS venit_stat,
        ROUND(AVG(p.product_weight_g), 2) AS greutate_medie_kg
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
    JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
    JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
    GROUP BY reg.state_name
),
RunningTotals AS (
    SELECT 
        state_name,
        venit_stat,
        SUM(venit_stat) OVER (ORDER BY venit_stat DESC) AS venit_cumulat,
        SUM(venit_stat) OVER () AS venit_total_tara
    FROM StateSales
)
SELECT 
    state_name,
    venit_stat,
    ROUND((venit_cumulat / venit_total_tara) * 100, 2) AS procent_cumulat,
    CASE 
        WHEN (venit_cumulat / venit_total_tara) <= 0.80 THEN 'Motor Economic (Top 80%)'
        ELSE 'Restul Pietei'
    END AS segmentare_pareto
FROM RunningTotals
ORDER BY venit_stat DESC;

--12 Monitorizarea Dinamicii Vânzărilor: Detectarea Categoriilor cu Risc de Epuizare
WITH CategoryDailySales AS (
    SELECT 
        reg.region_name,
        c.product_category_name,
        TRUNC(CURRENT_DATE) as data_analiza, -- Simulam data curenta
        SUM(ord.V_TOTAL_AMOUNT) as vanzari_zi
    FROM V_OLIST_ORDERS_ACCESS ord
    JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
    JOIN categories_view c ON p.category_id = c.category_id
    JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
    JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
    GROUP BY reg.region_name, c.product_category_name
)
SELECT 
    region_name,
    product_category_name,
    vanzari_zi,
    AVG(vanzari_zi) OVER (PARTITION BY region_name) as medie_regiune,
    ROUND(vanzari_zi / NULLIF(AVG(vanzari_zi) OVER (PARTITION BY region_name), 0), 2) as indice_accelerare,
    CASE 
        WHEN vanzari_zi > AVG(vanzari_zi) OVER (PARTITION BY region_name) * 1.5 THEN 'CERERE EXPLOZIVA - RECOMANDARE STOC'
        WHEN vanzari_zi < AVG(vanzari_zi) OVER (PARTITION BY region_name) * 0.5 THEN 'CERERE SCAZUTA - PROMOTIE NECESARA'
        ELSE 'FLUX NORMAL'
    END AS strategie_logistica
FROM CategoryDailySales
ORDER BY indice_accelerare DESC;

--13 Ierarhia Valorii: Identificarea Statelor care 'Cără' Regiunea în Spate
SELECT 
    reg.region_name,
    reg.state_name,
    SUM(ord.V_TOTAL_AMOUNT) as venit_stat,
    ROUND(100 * SUM(ord.V_TOTAL_AMOUNT) / 
        SUM(SUM(ord.V_TOTAL_AMOUNT)) OVER (PARTITION BY reg.region_name), 2) || '%' as contributie_in_regiune,
    ROUND(100 * SUM(ord.V_TOTAL_AMOUNT) / 
        SUM(SUM(ord.V_TOTAL_AMOUNT)) OVER (), 2) || '%' as contributie_nationala,
    DENSE_RANK() OVER (ORDER BY SUM(ord.V_TOTAL_AMOUNT) DESC) as pozitie_nationala
FROM V_OLIST_ORDERS_ACCESS ord
JOIN products_view p ON TRIM(ord.V_PRODUCT_ID) = TRIM(p.product_id)
JOIN olist_sellers_view_mongodb s ON TRIM(ord.V_SELLER_ID) = TRIM(s.seller_id)
JOIN olist_regions_view_mongodb reg ON LOWER(TRIM(s.seller_city)) = LOWER(TRIM(reg.city_name))
GROUP BY reg.region_name, reg.state_name
ORDER BY venit_stat DESC;