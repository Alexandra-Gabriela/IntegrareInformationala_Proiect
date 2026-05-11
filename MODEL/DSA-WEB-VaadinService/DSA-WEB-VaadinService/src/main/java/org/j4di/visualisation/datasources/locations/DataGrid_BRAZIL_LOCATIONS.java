package org.j4di.visualisation.datasources.locations;

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

@PageTitle("Brazil Geography Data")
@Route(value = "DataGrid_BRAZIL_LOCATIONS", layout = MainView.class)
public class DataGrid_BRAZIL_LOCATIONS extends VerticalLayout {

    // CORECȚIE: Numele view-ului din scriptul tău SQL de integrare
    private final String restDataServiceURL = "http://localhost:9990/DSA-SparkSQL-Service/rest/view/VW_CUSTS_LOCATIONS";

    private List<BRAZIL_LOCATIONS_VIEW> dataList = new ArrayList<>();
    private Grid<BRAZIL_LOCATIONS_VIEW> dataGrid = new Grid<>(BRAZIL_LOCATIONS_VIEW.class);

    public DataGrid_BRAZIL_LOCATIONS() {
        initDataSet();
        initDataGrid();
        add(new VerticalLayout(new H3("Sursă Date: Spark SQL (MongoDB Locations)"), dataGrid));
    }

    private void initDataSet() {
        try {
            this.dataList = getRESTData();
        } catch (Exception e) {
            System.err.println("Eroare colectare date locații: " + e.getMessage());
            this.dataList = new ArrayList<>();
        }
    }

    private void initDataGrid(){
        dataGrid.setItems(dataList);
        dataGrid.setColumns("cityName", "stateName");
    }

    private List<BRAZIL_LOCATIONS_VIEW> getRESTData(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        // CORECȚIE: Credențiale din application.properties-ul de la Spark
        headers.setBasicAuth("spark", "sql");

        return restTemplate.exchange(
                this.restDataServiceURL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<BRAZIL_LOCATIONS_VIEW>>() {}
        ).getBody();
    }
}