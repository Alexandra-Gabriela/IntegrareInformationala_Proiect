package org.j4di.analytical.views.olist;

import com.google.errorprone.annotations.Immutable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Immutable
@Table(name = "VW_EFICIENTA_LOGISTICA")
public class LogisticEfficiencyView {
    @Id
    private String statBrazilia;
    private Double venitPerKg;
}