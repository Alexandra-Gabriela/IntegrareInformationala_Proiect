package org.j4di.visualisation.olap.analytical.views.customers;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.j4di.visualisation.MainView;
import org.j4di.visualisation.rest.SQLResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.vaadin.addon.JFreeChartWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@PageTitle("Distributie Clienti (JFChart)")
@Route(value = "JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS", layout = MainView.class)
public class JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS extends VerticalLayout {
    private static final Logger logger = Logger.getLogger(JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS.class.getName());

    // Sursa de date Spark SQL
    private final String restDataServiceURL = "http://localhost:9990/DSA-SparkSQL-Service/_sqlrest/query";
    private final String sqlQuery = """
                        SELECT statename, COUNT(customerid) as client_count 
                        FROM VW_CUSTS_LOCATIONS 
                        GROUP BY statename 
                        ORDER BY client_count DESC
                        """;

    // Record local pentru maparea rezultatului
    public record CustomerStat(String statename, Long client_count) {}

    private List<CustomerStat> dataList = new ArrayList<>();
    private DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

    private Grid<CustomerStat> dataGrid = new Grid<>(CustomerStat.class);
    private JFreeChart chart;
    private JFreeChartWrapper chartWrapper;

    public JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS() {
        initDataSet();
        initDataGridChart();

        chartWrapper = new JFreeChartWrapper(chart);
        chartWrapper.setHeight("500px");
        chartWrapper.setWidth("700px");
        dataGrid.setHeight("400px");
        dataGrid.setWidth("400px");

        add(new HorizontalLayout(
                chartWrapper,
                new VerticalLayout(new H3("Statistici Clienti"), dataGrid))
        );
    }

    private void initDataSet() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setBasicAuth("spark", "sql");

        try {
            SQLResponse<CustomerStat> responseEntity = restTemplate.exchange(
                    restDataServiceURL,
                    HttpMethod.POST,
                    new HttpEntity<>(sqlQuery, headers),
                    new ParameterizedTypeReference<SQLResponse<CustomerStat>>() {}
            ).getBody();

            if (responseEntity != null && responseEntity.response() != null) {
                this.dataList = responseEntity.response();
                for (CustomerStat data : dataList) {
                    // Adaugam valorile in setul de date JFreeChart
                    this.dataSet.addValue(data.client_count(), "Clienti", data.statename());
                }
            }
        } catch (Exception e) {
            logger.severe("Eroare la preluarea datelor JFChart: " + e.getMessage());
        }
    }

    private void initDataGridChart() {
        dataGrid.setItems(dataList);
        dataGrid.setColumns("statename", "client_count");

        // Creare Bar Chart
        chart = ChartFactory.createBarChart(
                "Clienti pe State Brazilia",
                "STATE",
                "Numar Clienti",
                this.dataSet,
                PlotOrientation.VERTICAL,
                false, true, false
        );
    }
}