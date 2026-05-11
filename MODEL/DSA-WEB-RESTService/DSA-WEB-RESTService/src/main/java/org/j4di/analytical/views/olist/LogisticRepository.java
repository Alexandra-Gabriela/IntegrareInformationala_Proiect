package org.j4di.analytical.views.olist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LogisticRepository extends JpaRepository<LogisticEfficiencyView, String> {
    @Query("SELECT v FROM LogisticEfficiencyView v")
    List<LogisticEfficiencyView> getReport();
}

