package org.datasource.csv.order;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.datasource.csv.CSVResourceFileDataSourceConnector;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderPaymentsCSVViewBuilder {
    private List<OrderPaymentView> viewList = new ArrayList<>();
    private CSVResourceFileDataSourceConnector dataSourceConnector;

    public OrderPaymentsCSVViewBuilder(CSVResourceFileDataSourceConnector dataSourceConnector) {
        this.dataSourceConnector = dataSourceConnector;
    }

    public List<OrderPaymentView> getViewList() { return viewList; }

    public OrderPaymentsCSVViewBuilder build() throws Exception {
        // Linia 28 din OrderPaymentsCSVViewBuilder.java
        File csvFile = dataSourceConnector.getCSVFile("datasource/olist_order_payments_dataset.csv");
        Reader in = new FileReader(csvFile);

        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',');
        Iterable<CSVRecord> records = format.parse(in);

        viewList = new ArrayList<>();
        for (CSVRecord record : records) {
            this.viewList.add(new OrderPaymentView(
                    record.get("order_id"),
                    record.get("payment_type"),
                    Double.parseDouble(record.get("payment_value")),
                    Integer.parseInt(record.get("payment_installments"))
            ));
        }
        return this;
    }
}