package org.j4di.analytical.views.olist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryDominanceRepository extends JpaRepository<CategoryDominanceView, String> {
    @Query("SELECT v FROM CategoryDominanceView v")
    List<CategoryDominanceView> getReport();
}