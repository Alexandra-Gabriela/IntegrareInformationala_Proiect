package org.j4di; // CRITIC: Adaugă această linie

import org.j4di.analytical.views.OLAP_VIEW_SALES_DEP_CIT_CUST;
import org.j4di.analytical.views.OLAP_VIEW_SALES_DEP_CIT_CUST_Repository;
import org.j4di.analytical.views.olist.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/ecommerce-analytics")
public class RESTViewService {

	@Autowired private ParetoRepository paretoRepo;
	@Autowired private LogisticRepository logisticRepo;
	@Autowired private CategoryDominanceRepository categoryRepo;
	@Autowired private OLAP_VIEW_SALES_DEP_CIT_CUST_Repository analyticalRepo;

	@GetMapping("/final-report")
	public List<OLAP_VIEW_SALES_DEP_CIT_CUST> getFinalReport() {
		return analyticalRepo.get_OLAP_VIEW_SALES_DEP_CIT_CUST();
	}

	@GetMapping("/pareto")
	public List<ParetoAnalysisView> getPareto() {
		return paretoRepo.getParetoReport();
	}

	@GetMapping("/efficiency")
	public List<LogisticEfficiencyView> getEfficiency() {
		return logisticRepo.getReport();
	}

	@GetMapping("/market-share")
	public List<CategoryDominanceView> getMarketShare() {
		return categoryRepo.getReport();
	}
}