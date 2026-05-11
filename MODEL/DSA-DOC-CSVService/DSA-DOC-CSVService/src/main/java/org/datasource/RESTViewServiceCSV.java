package org.datasource;

import org.datasource.csv.order.OrderItemView;
import org.datasource.csv.order.OrderItemsCSVViewBuilder;
import org.datasource.csv.order.OrderPaymentView;
import org.datasource.csv.order.OrderPaymentsCSVViewBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

/* Noile URL-uri de acces pentru datele tale Olist:
    http://localhost:8097/DSA-DOC-CSVService/rest/olist/OrderItems
    http://localhost:8097/DSA-DOC-CSVService/rest/olist/OrderPayments
*/
@RestController @RequestMapping("/olist") // Schimbăm prefixul în /olist
public class RESTViewServiceCSV {
	private static Logger logger = Logger.getLogger(RESTViewServiceCSV.class.getName());

	// Injectăm cele două buildere noi pe care le-am construit anterior
	@Autowired private OrderItemsCSVViewBuilder orderItemsBuilder;
	@Autowired private OrderPaymentsCSVViewBuilder orderPaymentsBuilder;

	// Endpoint pentru Produsele din Comenzi (olist_order_items_dataset.csv)
	@RequestMapping(value = "/OrderItems", method = RequestMethod.GET,
			produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public List<OrderItemView> get_OrderItems() throws Exception {
		logger.info("Solicitare date: Order Items CSV");
		// Urmează logica cerută de prof: dacă lista e goală, o construim, altfel o dăm din cache
		if (this.orderItemsBuilder.getViewList().isEmpty()) {
			return this.orderItemsBuilder.build().getViewList();
		}
		return this.orderItemsBuilder.getViewList();
	}

	// Endpoint pentru Plăți (olist_order_payments_dataset.csv)
	@RequestMapping(value = "/OrderPayments", method = RequestMethod.GET,
			produces = {MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	public List<OrderPaymentView> get_OrderPayments() throws Exception {
		logger.info("Solicitare date: Order Payments CSV");
		if (this.orderPaymentsBuilder.getViewList().isEmpty()) {
			return this.orderPaymentsBuilder.build().getViewList();
		}
		return this.orderPaymentsBuilder.getViewList();
	}
}