package org.j4di.visualisation.olap.fact.views.sales;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.j4di.visualisation.MainView;
import org.j4di.visualisation.olap.analytical.views.sales.OLAP_FACTS_SALES_AMOUNT;
import org.j4di.visualisation.rest.SQLResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.vaadin.addon.JFreeChartWrapper;

@PageTitle("Eficienta Logistica (JFChart)")
@Route(value = "JFChart_LOGISTICS_EFFICIENCY", layout = MainView.class)
public class JFChart_LOGISTICS_EFFICIENCY extends VerticalLayout {
    private final String restDataServiceURL = "http://localhost:9990/DSA-SparkSQL-Service/_sqlrest/query";
    private final String sqlQuery = """
                        SELECT productId, weight, price as sales_amount 
                        FROM VW_INTEGRATED_OLIST 
                        WHERE weight > 0 
                        LIMIT 50
                        """;

    private DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

    public JFChart_LOGISTICS_EFFICIENCY() {
        initDataSet();
        JFreeChart chart = ChartFactory.createBarChart(
                "Venit per Produs/Greutate", "Produs", "Suma ($)",
                dataSet, PlotOrientation.VERTICAL, false, true, false
        );
        add(new JFreeChartWrapper(chart));
    }

    private void initDataSet() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("spark", "sql");

        SQLResponse<OLAP_FACTS_SALES_AMOUNT> response = restTemplate.exchange(
                restDataServiceURL, HttpMethod.POST, new HttpEntity<>(sqlQuery, headers),
                new ParameterizedTypeReference<SQLResponse<OLAP_FACTS_SALES_AMOUNT>>() {}
        ).getBody();

        if (response != null && response.response() != null) {
            for (OLAP_FACTS_SALES_AMOUNT data : response.response()) {
                // Afisam raportul pret/greutate
                this.dataSet.addValue(data.sales_amount() / data.weight(), "Venit/Kg", data.productId().substring(0, 5));
            }
        }
    }
}