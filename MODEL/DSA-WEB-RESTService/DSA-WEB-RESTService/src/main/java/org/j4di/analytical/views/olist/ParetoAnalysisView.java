package org.j4di.analytical.views.olist;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Immutable
@Table(name = "VW_PARETO_ORASE") // Numele View-ului creat de tine în Spark
public class ParetoAnalysisView {
    @Id
    private String sellerCity;
    private String macroRegiune;
    private Double venitOras;
    private Double procentCumulat;
}