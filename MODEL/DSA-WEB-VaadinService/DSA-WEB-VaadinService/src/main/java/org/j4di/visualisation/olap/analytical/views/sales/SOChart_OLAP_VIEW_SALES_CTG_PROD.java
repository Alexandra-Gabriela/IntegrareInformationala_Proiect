package org.j4di.visualisation.olap.analytical.views.sales;

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

@PageTitle("Vanzari Categorii Olist (SOChart)")
@Route(value = "SOChart_OLAP_VIEW_SALES_CTG_PROD", layout = MainView.class)
public class SOChart_OLAP_VIEW_SALES_CTG_PROD extends VerticalLayout {
    private static final Logger logger = Logger.getLogger(SOChart_OLAP_VIEW_SALES_CTG_PROD.class.getName());

    // Sursa de date Spark SQL
    private final String restDataServiceURL = "http://localhost:9990/DSA-SparkSQL-Service/_sqlrest/query";
    private final String sqlQuery = """
                        SELECT categoryName, SUM(price) as sales_amount 
                        FROM VW_INTEGRATED_OLIST 
                        GROUP BY categoryName
                        ORDER BY sales_amount DESC
                        LIMIT 10
                        """;

    private List<OLAP_VIEW_SALES_CTG_PROD> dataList = new ArrayList<>();

    // Modelele de date specifice SOCharts
    private Data dataSet = new Data();
    private CategoryData categoryDataSet = new CategoryData();

    private Grid<OLAP_VIEW_SALES_CTG_PROD> dataGrid = new Grid<>(OLAP_VIEW_SALES_CTG_PROD.class);
    private SOChart soChart = new SOChart();

    public SOChart_OLAP_VIEW_SALES_CTG_PROD() {
        initDataSet();
        initDataGridChart();

        // Configurare layout
        soChart.setSize("700px", "500px");
        dataGrid.setHeight("400px");
        dataGrid.setWidth("500px");

        add(new HorizontalLayout(
                soChart,
                new VerticalLayout(new H3("Distributie Tabelara"), dataGrid))
        );
    }

    private void initDataSet() {
        SQLResponse<OLAP_VIEW_SALES_CTG_PROD> response = this.getRESTData();
        if (response != null && response.response() != null) {
            this.dataList = response.response();
            for (OLAP_VIEW_SALES_CTG_PROD data : dataList) {
                // Mapare pentru SOCharts: categoryName() si sales_amount()
                this.categoryDataSet.add(data.categoryName());
                this.dataSet.add(data.sales_amount());
            }
        }
    }

    private void initDataGridChart() {
        dataGrid.setItems(dataList);
        dataGrid.setColumns("categoryName", "sales_amount");

        // Creare PieChart (Diagrama Placinta)
        PieChart pieChart = new PieChart(categoryDataSet, dataSet);
        pieChart.setName("Cote Piata Categorii");

        // Configurare etichete interactive (procentaj)
        Label label = pieChart.getLabel(true);
        label.setFormatter("{b}: {d}%"); // {b} nume, {d} procent

        // Adaugare Legenda
        Legend legend = new Legend();
        Position p = new Position();
        p.setBottom(Size.pixels(10));
        legend.setPosition(p);

        soChart.add(pieChart, legend);
    }

    private SQLResponse<OLAP_VIEW_SALES_CTG_PROD> getRESTData() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setBasicAuth("spark", "sql");

        try {
            return restTemplate.exchange(
                    restDataServiceURL,
                    HttpMethod.POST,
                    new HttpEntity<>(sqlQuery, headers),
                    new ParameterizedTypeReference<SQLResponse<OLAP_VIEW_SALES_CTG_PROD>>() {}
            ).getBody();
        } catch (Exception e) {
            logger.severe("Eroare la preluarea datelor pentru SOChart: " + e.getMessage());
            return null;
        }
    }
}