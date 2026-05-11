package org.datasource.mongodb.views.olist;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.datasource.mongodb.MongoDataSourceConnector;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class OlistRegionViewBuilder {
    private List<RegionView> regionsViewList;
    private List<CityView> citiesViewList;
    private MongoDataSourceConnector dataSourceConnector;

    public OlistRegionViewBuilder(MongoDataSourceConnector dataSourceConnector) {
        this.dataSourceConnector = dataSourceConnector;
    }

    public List<CityView> getCitiesViewList() {
        return citiesViewList;
    }

    public OlistRegionViewBuilder build() throws Exception {
        return this.select().map();
    }


    private OlistRegionViewBuilder select() throws Exception {
        MongoDatabase db = dataSourceConnector.getMongoDatabase();
        // Citim colecția "Regions" (Numele corect din imaginea ta)
        MongoCollection<RegionView> collection = db.getCollection("Regions", RegionView.class);

        this.regionsViewList = new ArrayList<>();
        collection.find().into(this.regionsViewList); // "Aspirăm" toate cele 5+ documente din imagine
        return this;
    }

    private OlistRegionViewBuilder map() {
        this.citiesViewList = new ArrayList<>();
        if (this.regionsViewList != null) {
            for (RegionView region : regionsViewList) {
                if (region.getStates() != null) {
                    for (StateView state : region.getStates()) {
                        if (state.getCities() != null) {
                            for (CityView city : state.getCities()) {
                                // Mapăm numele statului (ex: Parana) în fiecare oraș pentru Spark
                                city.setStateName(state.getStateName());
                                this.citiesViewList.add(city);
                            }
                        }
                    }
                }
            }
        }
        return this;
    }
}