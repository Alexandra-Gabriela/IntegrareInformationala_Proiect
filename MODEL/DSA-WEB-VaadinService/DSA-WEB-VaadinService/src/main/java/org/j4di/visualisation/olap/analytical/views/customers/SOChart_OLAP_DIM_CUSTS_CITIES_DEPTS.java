package org.j4di.visualisation.olap.analytical.views.customers;

import com.storedobject.chart.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.j4di.visualisation.MainView;
import org.j4di.visualisation.rest.SQLResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@PageTitle("Analiza Geografica Clienti (SOChart)")
@Route(value = "SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS", layout = MainView.class)
public class SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS extends VerticalLayout {
    private static final Logger logger = Logger.getLogger(SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS.class.getName());

    private final String restDataServiceURL = "http://localhost:9990/DSA-SparkSQL-Service/_sqlrest/query";

    // Query care numara clientii pe fiecare stat
    private final String sqlQuery = """
                        SELECT statename, COUNT(customerid) as client_count 
                        FROM VW_CUSTS_LOCATIONS 
                        GROUP BY statename 
                        ORDER BY client_count DESC
                        """;

    // Folosim un record local sau cel definit de tine pentru a mapa rezultatul numaratorii
    public record CustomerStat(String statename, Long client_count) {}

    private List<CustomerStat> dataList = new ArrayList<>();
    private Data dataSet = new Data();
    private CategoryData categoryDataSet = new CategoryData();

    private SOChart soChart = new SOChart();
    private Grid<CustomerStat> dataGrid = new Grid<>(CustomerStat.class);

    public SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS() {
        initDataSet();
        initVisuals();

        soChart.setSize("700px", "500px");
        dataGrid.setWidth("400px");

        add(new H3("Distributia Clientilor pe State (Brazilia)"),
                new HorizontalLayout(soChart, dataGrid));
    }

    private void initDataSet() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setBasicAuth("spark", "sql");

        try {
            SQLResponse<CustomerStat> response = restTemplate.exchange(
                    restDataServiceURL,
                    HttpMethod.POST,
                    new HttpEntity<>(sqlQuery, headers),
                    new ParameterizedTypeReference<SQLResponse<CustomerStat>>() {}
            ).getBody();

            if (response != null && response.response() != null) {
                this.dataList = response.response();
                for (CustomerStat data : dataList) {
                    categoryDataSet.add(data.statename());
                    dataSet.add(data.client_count());
                }
            }
        } catch (Exception e) {
            logger.severe("Eroare la preluarea datelor clienti: " + e.getMessage());
        }
    }

    private void initVisuals() {
        dataGrid.setItems(dataList);

        BarChart barChart = new BarChart(categoryDataSet, dataSet);
        barChart.setName("Numar Clienti");

        XAxis xAxis = new XAxis(DataType.CATEGORY);
        YAxis yAxis = new YAxis(DataType.NUMBER);

        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        barChart.plotOn(rc);

        soChart.add(rc, barChart);
    }
}