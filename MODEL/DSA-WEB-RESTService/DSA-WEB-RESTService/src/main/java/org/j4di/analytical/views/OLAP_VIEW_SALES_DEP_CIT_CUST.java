package org.j4di.analytical.views;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Immutable
@Table(name = "V_ANALYTICS_OLIST_FINAL") // Numele View-ului final din Spark
public class OLAP_VIEW_SALES_DEP_CIT_CUST {
    @Id
    private String orderId; // Cheie primară pentru JPA
    private String customerCity; // Din MongoDB
    private String productCategory; // Din Postgres
    private Double price; // Din CSV
}