package org.j4di.visualisation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

// Importuri Date Brute
import org.j4di.visualisation.datasources.locations.DataGrid_BRAZIL_LOCATIONS;
import org.j4di.visualisation.datasources.products.DataGrid_PRODUCTS_VIEW;

// Importuri Integrare
import org.j4di.visualisation.olap.fact.views.sales.DataGrid_OLAP_FACTS_SALES_AMOUNT;

// Importuri Analize
import org.j4di.visualisation.olap.analytical.views.sales.JFChart_OLAP_VIEW_SALES_CTG_PROD;
import org.j4di.visualisation.olap.analytical.views.sales.SOChart_OLAP_VIEW_SALES_CTG_PROD;
import org.j4di.visualisation.olap.analytical.views.customers.JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS;
import org.j4di.visualisation.olap.analytical.views.customers.SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS;
import org.j4di.visualisation.olap.fact.views.sales.JFChart_LOGISTICS_EFFICIENCY;

@Route("")
public class MainView extends AppLayout implements BeforeEnterObserver {

    public MainView() {
        // Antet (Header)
        HorizontalLayout header = createHeader();
        addToNavbar(header);

        // Conținut central implicit
        H1 welcomeMsg = new H1("Olist Ecommerce - Sistem Integrat de Analiză");
        welcomeMsg.getStyle().set("margin", "40px");
        setContent(welcomeMsg);

        // Meniul lateral (Drawer)
        getElement().getStyle().set("--vaadin-app-layout-drawer-width", "360px");
        addToDrawer(createAccordion());
    }

    private HorizontalLayout createHeader() {
        H1 title = new H1("DSA Olist Dashboard");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.setPadding(true);
        return header;
    }

    private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> viewClass) {
        Icon icon = viewIcon.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs)");

        RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(viewClass);

        return new Tab(link);
    }

    private Accordion createAccordion() {
        Accordion accordion = new Accordion();

        // --- NIVEL 1: DATE BRUTE ---
        Tabs tabsDataSources = new Tabs();
        tabsDataSources.setOrientation(Tabs.Orientation.VERTICAL);
        tabsDataSources.add(createTab(VaadinIcon.DATABASE, "Produse (Postgres)", DataGrid_PRODUCTS_VIEW.class));
        tabsDataSources.add(createTab(VaadinIcon.MAP_MARKER, "Locații (Mongo)", DataGrid_BRAZIL_LOCATIONS.class));
        accordion.add("1. Date Brute", tabsDataSources);

        // --- NIVEL 2: INTEGRARE ---
        Tabs tabsIntegration = new Tabs();
        tabsIntegration.setOrientation(Tabs.Orientation.VERTICAL);
        tabsIntegration.add(createTab(VaadinIcon.LIST_SELECT, "Fact: Vânzări Olist", DataGrid_OLAP_FACTS_SALES_AMOUNT.class));
        accordion.add("2. Integrare", tabsIntegration);

        // --- NIVEL 3: ANALIZE ---
        Tabs tabsAnalytics = new Tabs();
        tabsAnalytics.setOrientation(Tabs.Orientation.VERTICAL);
        tabsAnalytics.add(createTab(VaadinIcon.CHART, "Categorii (JFC)", JFChart_OLAP_VIEW_SALES_CTG_PROD.class));
//        tabsAnalytics.add(createTab(VaadinIcon.PIE_CHART, "Categorii (SOC)", SOChart_OLAP_VIEW_SALES_CTG_PROD.class));
        tabsAnalytics.add(createTab(VaadinIcon.USER_CHECK, "Clienți/Stat (JFC)", JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS.class));
        tabsAnalytics.add(createTab(VaadinIcon.GLOBE_WIRE, "Clienți/Stat (SOC)", SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS.class));
        tabsAnalytics.add(createTab(VaadinIcon.TRUCK, "Eficiență Logistică", JFChart_LOGISTICS_EFFICIENCY.class));
        accordion.add("3. Analize și Dashboard", tabsAnalytics);

        return accordion;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {}
}