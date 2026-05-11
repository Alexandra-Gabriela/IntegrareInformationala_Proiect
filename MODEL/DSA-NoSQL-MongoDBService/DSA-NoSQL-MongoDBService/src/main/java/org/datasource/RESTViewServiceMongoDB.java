package org.datasource;

import org.datasource.mongodb.views.olist.CityView;
import org.datasource.mongodb.views.olist.RegionView;
import org.datasource.mongodb.views.olist.SellerLocationView;
import org.datasource.mongodb.views.olist.OlistRegionViewBuilder;
import org.datasource.mongodb.views.olist.OlistSellerLocationViewBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/brazil") // ELIMINĂM "/rest" de aici, deoarece există deja în context-path
public class RESTViewServiceMongoDB {
	private static final Logger logger = Logger.getLogger(RESTViewServiceMongoDB.class.getName());

	@Autowired
	private OlistRegionViewBuilder regionsBuilder;

	@Autowired
	private OlistSellerLocationViewBuilder sellersBuilder;

	@GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
	public String pingDataSource() {
		logger.info(">>>> RESTViewServiceMongoDB is Up!");
		return "Ping response from RESTViewServiceMongoDB (Olist Context)!";
	}

	@GetMapping(value = "/CityView", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CityView> get_CityView() throws Exception {
		logger.info(">>>> Se livreaza datele CityView pentru Spark");
		return this.regionsBuilder.build().getCitiesViewList();
	}

	@GetMapping(value = "/SellerView", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SellerLocationView> get_SellerView() throws Exception {
		logger.info(">>>> Se livreaza datele SellerLocationView pentru Spark");
		return this.sellersBuilder.build().getSellerLocations();
	}
}