package org.datasource.mongodb.views.olist;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.datasource.mongodb.MongoDataSourceConnector;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class OlistSellerLocationViewBuilder {
    private List<SellerLocationView> sellerLocations;
    private MongoDataSourceConnector dataSourceConnector;

    public OlistSellerLocationViewBuilder(MongoDataSourceConnector dataSourceConnector) {
        this.dataSourceConnector = dataSourceConnector;
    }

    public List<SellerLocationView> getSellerLocations() { return sellerLocations; }

    public OlistSellerLocationViewBuilder build() throws Exception {
        MongoDatabase db = dataSourceConnector.getMongoDatabase();
        // Citim din colectia "Locations" definita in scriptul tau SQL
        MongoCollection<SellerLocationView> collection =
                db.getCollection("Locations", SellerLocationView.class);

        this.sellerLocations = new ArrayList<>();
        collection.find().into(this.sellerLocations);
        return this;
    }
}