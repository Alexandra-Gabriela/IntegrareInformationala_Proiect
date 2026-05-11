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
public class OrderItemsCSVViewBuilder {
    private List<OrderItemView> viewList = new ArrayList<>();
    private CSVResourceFileDataSourceConnector dataSourceConnector;

    public OrderItemsCSVViewBuilder(CSVResourceFileDataSourceConnector dataSourceConnector) {
        this.dataSourceConnector = dataSourceConnector;
    }

    public List<OrderItemView> getViewList() { return viewList; }

    public OrderItemsCSVViewBuilder build() throws Exception {
        File csvFile = dataSourceConnector.getCSVFile("datasource/olist_order_items_dataset.csv");
        Reader in = new FileReader(csvFile);

        // Configurăm formatul CSV cu header-ul exact din fișierul tău Olist
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',');
        Iterable<CSVRecord> records = format.parse(in);

        viewList = new ArrayList<>();
        for (CSVRecord record : records) {
            this.viewList.add(new OrderItemView(
                    record.get("order_id"),
                    record.get("product_id"),
                    record.get("seller_id"),
                    Double.parseDouble(record.get("price")),
                    Double.parseDouble(record.get("freight_value"))
            ));
        }
        return this;
    }
}