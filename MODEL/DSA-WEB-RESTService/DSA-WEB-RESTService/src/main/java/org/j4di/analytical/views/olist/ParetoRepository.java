package org.j4di.analytical.views.olist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ParetoRepository extends JpaRepository<ParetoAnalysisView, String> {
    @Query("SELECT v FROM ParetoAnalysisView v")
    List<ParetoAnalysisView> getParetoReport();
}