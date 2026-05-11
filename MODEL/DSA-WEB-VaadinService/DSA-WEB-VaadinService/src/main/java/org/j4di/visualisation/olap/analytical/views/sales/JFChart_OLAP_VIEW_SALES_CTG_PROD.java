package org.j4di.visualisation.olap.analytical.views.sales;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.j4di.visualisation.MainView;
import org.j4di.visualisation.olap.analytical.views.sales.OLAP_VIEW_SALES_CTG_PROD;
import org.j4di.visualisation.rest.SQLResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
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

@PageTitle("Vanzari pe Categorii Olist")
@Route(value = "JFChart_OLAP_VIEW_SALES_CTG_PROD", layout = MainView.class)
public class JFChart_OLAP_VIEW_SALES_CTG_PROD extends VerticalLayout {

    // Declarare logger pentru a evita eroarea "Cannot resolve symbol"
    private static final Logger logger = Logger.getLogger(JFChart_OLAP_VIEW_SALES_CTG_PROD.class.getName());

    // Configurare conexiune Spark SQL
    private final String restDataServiceURL = "http://localhost:9990/DSA-SparkSQL-Service/_sqlrest/query";
    private final String sqlQuery = """
                        SELECT categoryName, SUM(price) as sales_amount 
                        FROM VW_INTEGRATED_OLIST 
                        GROUP BY categoryName
                        ORDER BY sales_amount DESC
                        LIMIT 10
                        """;

    private List<OLAP_VIEW_SALES_CTG_PROD> dataList = new ArrayList<>();
    private DefaultPieDataset dataSet = new DefaultPieDataset();
    private Grid<OLAP_VIEW_SALES_CTG_PROD> dataGrid = new Grid<>(OLAP_VIEW_SALES_CTG_PROD.class);
    private JFreeChart chart;
    private JFreeChartWrapper chartWrapper;

    public JFChart_OLAP_VIEW_SALES_CTG_PROD() {
        initDataSet();
        initDataGridChart();

        chartWrapper = new JFreeChartWrapper(chart);
        chartWrapper.setHeight("450px");
        chartWrapper.setWidth("650px");
        dataGrid.setHeight("400px");
        dataGrid.setWidth("500px");

        add(new HorizontalLayout(
                chartWrapper,
                new VerticalLayout(new H3("Date Analitice"), dataGrid))
        );
    }

    private void initDataSet() {
        SQLResponse<OLAP_VIEW_SALES_CTG_PROD> sqlResponse = this.getRESTData();
        if (sqlResponse != null && sqlResponse.response() != null) {
            this.dataList = sqlResponse.response();
            for (OLAP_VIEW_SALES_CTG_PROD data : dataList) {
                // REPARARE: Accesarea campului din record se face cu paranteze ()
                this.dataSet.setValue(data.categoryName(), data.sales_amount());
            }
        }
        logger.info("DEBUG: Am incarcat " + dataList.size() + " categorii.");
    }

    private void initDataGridChart() {
        dataGrid.setItems(dataList);
        dataGrid.setColumns("categoryName", "sales_amount");

        chart = ChartFactory.createPieChart("Top 10 Categorii Olist",
                this.dataSet, false, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        // Afiseaza Numele categoriei si Procentul
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} : {2}"));
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
            logger.severe("Eroare la apelul REST Spark: " + e.getMessage());
            return null;
        }
    }
}