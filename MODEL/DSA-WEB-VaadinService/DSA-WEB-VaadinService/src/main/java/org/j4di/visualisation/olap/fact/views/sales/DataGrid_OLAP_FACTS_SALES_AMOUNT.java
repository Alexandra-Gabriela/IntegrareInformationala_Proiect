package org.j4di.visualisation.olap.fact.views.sales;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.j4di.visualisation.MainView;
import org.j4di.visualisation.olap.analytical.views.sales.OLAP_FACTS_SALES_AMOUNT;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@PageTitle("Olist Fact Sales Amount")
@Route(value = "DataGrid_OLAP_FACTS_SALES_AMOUNT", layout = MainView.class)
public class DataGrid_OLAP_FACTS_SALES_AMOUNT extends VerticalLayout {
    private static final Logger logger = Logger.getLogger(DataGrid_OLAP_FACTS_SALES_AMOUNT.class.getName());

    // URL catre view-ul Spark SQL care face join intre CSV si Postgres
    private final String restDataServiceURL = "http://localhost:9990/DSA-SparkSQL-Service/rest/view/OLAP_FACTS_SALES_AMOUNT";

    private List<OLAP_FACTS_SALES_AMOUNT> dataList = new ArrayList<>();
    private Grid<OLAP_FACTS_SALES_AMOUNT> dataGrid = new Grid<>(OLAP_FACTS_SALES_AMOUNT.class);

    public DataGrid_OLAP_FACTS_SALES_AMOUNT() {
        initDataSet();
        initDataGrid();

        add(new VerticalLayout(new H3("Fact Table: Olist Sales Integration"), dataGrid));
    }

    private void initDataSet() {
        this.dataList = getRESTData();
    }

    private void initDataGrid() {
        dataGrid.setItems(dataList);

        // ELIMINĂ "orderId" de aici pentru a opri eroarea de mapare
        dataGrid.setColumns("productId", "weight", "sales_amount", "invoiceDate");

        // Opțional: Dacă vrei neapărat să afișezi ceva în loc de ID, poți adăuga o coloană calculată manual
        // dataGrid.addColumn(item -> "N/A").setHeader("Order ID");
    }

    private List<OLAP_FACTS_SALES_AMOUNT> getRESTData() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setBasicAuth("spark", "sql");

        try {
            return restTemplate.exchange(
                    this.restDataServiceURL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<OLAP_FACTS_SALES_AMOUNT>>() {}
            ).getBody();
        } catch (Exception e) {
            logger.severe("Eroare la preluarea faptelor din Spark: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}