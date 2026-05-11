package org.j4di.visualisation.datasources.products;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.j4di.visualisation.MainView;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Olist Products Data")
@Route(value = "DataGrid_PRODUCTS_VIEW", layout = MainView.class)
public class DataGrid_PRODUCTS_VIEW extends VerticalLayout {

    // CORECȚIE: Portul este 8081 (nu 9990) și View-ul este PRODUCTS_VIEW (nu PRODUCTS_SPARK)
    private final String restDataServiceURL = "http://localhost:8081/DSA-SparkSQL-Service/rest/view/PRODUCTS_VIEW";

    private List<PRODUCTS_VIEW> dataList = new ArrayList<>();
    private Grid<PRODUCTS_VIEW> dataGrid = new Grid<>(PRODUCTS_VIEW.class);

    public DataGrid_PRODUCTS_VIEW() {
        initDataSet();
        initDataGrid();

        // Estetică: Adăugăm padding și aliniere
        setSpacing(true);
        setPadding(true);

        add(new H3("Sursă Date: Spark SQL (Postgres Products Integration)"), dataGrid);
    }

    private void initDataSet() {
        try {
            this.dataList = getRESTData();
        } catch (Exception e) {
            // Logare în consolă pentru depanare dacă Spark este oprit
            System.err.println("EROARE: Nu s-au putut prelua datele din Spark: " + e.getMessage());
            this.dataList = new ArrayList<>();
        }
    }

    private void initDataGrid(){
        dataGrid.setItems(dataList);
        // Mapăm coloanele conform câmpurilor din clasa PRODUCTS_VIEW.java
        // Asigură-te că aceste nume de câmpuri există în Record/Clasa PRODUCTS_VIEW
        dataGrid.setColumns("productId", "categoryName", "weight");
    }

    private List<PRODUCTS_VIEW> getRESTData(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        // Credențiale setate în BasicConfiguration din Spark Service
        headers.setBasicAuth("developer", "iis");

        return restTemplate.exchange(
                this.restDataServiceURL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<PRODUCTS_VIEW>>() {}
        ).getBody();
    }
}