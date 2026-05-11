package org.datasource;

import org.datasource.jpa.views.ecommerce.CategoryView;
import org.datasource.jpa.views.ecommerce.CategoryViewBuilder;
import org.datasource.jpa.views.ecommerce.ProductView;
import org.datasource.jpa.views.ecommerce.ProductViewBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

/* Noile URL-uri pentru Spark SQL:
    http://localhost:8091/DSA_SQL_JPAService/rest/ecommerce/ProductView
    http://localhost:8091/DSA_SQL_JPAService/rest/ecommerce/CategoryView
*/
@RestController
@RequestMapping("/ecommerce") // Schimbăm calea să se potrivească cu schema ta
public class RESTViewServiceJPA {
	private static Logger logger = Logger.getLogger(RESTViewServiceJPA.class.getName());

	@Autowired private ProductViewBuilder productViewBuilder;
	@Autowired private CategoryViewBuilder categoryViewBuilder;

	@RequestMapping(value = "/ping", method = RequestMethod.GET,
			produces = {MediaType.TEXT_PLAIN_VALUE})
	@ResponseBody
	public String pingDataSource() {
		logger.info(">>>> DSA-SQL-JPAService:: RESTViewService is Up!");
		return "Ping response from DSA-SQL-JPAService (Ecommerce context)!";
	}

	// Endpoint pentru Produse (din Postgres)
	@RequestMapping(value = "/ProductView", method = RequestMethod.GET,
			produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public List<ProductView> get_ProductView() {
		// Aici folosim builder-ul adaptat pentru Postgres
		return this.productViewBuilder.build().getProductViewList();
	}

	// Endpoint pentru Categorii (din Postgres)
	@RequestMapping(value = "/CategoryView", method = RequestMethod.GET,
			produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public List<CategoryView> get_CategoryView() {
		return this.categoryViewBuilder.build().getCategoryViewList();
	}
}