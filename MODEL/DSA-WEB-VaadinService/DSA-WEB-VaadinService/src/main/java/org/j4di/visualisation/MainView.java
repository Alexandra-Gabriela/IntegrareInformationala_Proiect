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

// 1. Data Sources (Conform imaginii tale)
import org.j4di.visualisation.datasources.locations.DataGrid_BRAZIL_LOCATIONS;
import org.j4di.visualisation.datasources.products.DataGrid_PRODUCTS_VIEW;

// 2. Multidimensional Model (Conform imaginii tale)
// ATENTIE: In poza ta apare folderul 'olap.analytical.views.customers', nu 'dim.views.customers'
import org.j4di.visualisation.olap.analytical.views.customers.JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS;
import org.j4di.visualisation.olap.fact.views.sales.DataGrid_OLAP_FACTS_SALES_AMOUNT;

// 3. Analytics & Charts (Conform pachetelor din poza ta)
import org.j4di.visualisation.olap.analytical.views.sales.JFChart_OLAP_VIEW_SALES_CTG_PROD;
import org.j4di.visualisation.olap.analytical.views.sales.SOChart_OLAP_VIEW_SALES_CTG_PROD;
import org.j4di.visualisation.olap.analytical.views.customers.SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS;
import org.j4di.visualisation.olap.fact.views.sales.JFChart_LOGISTICS_EFFICIENCY;

@Route
public class MainView extends AppLayout implements BeforeEnterObserver {

    public MainView() {
        HorizontalLayout header = createHeader();
        addToNavbar(header);
        setContent(new H1("Olist Ecommerce - Sistem Integrat"));
        getElement().getStyle().set("--vaadin-app-layout-drawer-width", "360px");
        addToDrawer(createAccordion());
    }

    private HorizontalLayout createHeader() {
        H1 title = new H1("DSA Olist Dashboard");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
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

        // --- NIVEL 1: DATA SOURCES ---
        Tabs tabsDataSources = new Tabs();
        tabsDataSources.add(createTab(VaadinIcon.DATABASE, "Produse (Postgres)", DataGrid_PRODUCTS_VIEW.class));
        tabsDataSources.add(createTab(VaadinIcon.MAP_MARKER, "Locatii (Mongo)", DataGrid_BRAZIL_LOCATIONS.class));
        tabsDataSources.setOrientation(Tabs.Orientation.VERTICAL);
        accordion.add("1. Date Brute", tabsDataSources);

        // --- NIVEL 2: INTEGRATION ---
        Tabs tabsMultidimensional = new Tabs();
        tabsMultidimensional.add(createTab(VaadinIcon.LIST_SELECT, "Fact: Vanzari Olist", DataGrid_OLAP_FACTS_SALES_AMOUNT.class));
        tabsMultidimensional.setOrientation(Tabs.Orientation.VERTICAL);
        accordion.add("2. Integrare", tabsMultidimensional);

        // --- NIVEL 3: ANALYTICS ---
        Tabs tabsAnalytics = new Tabs();
        tabsAnalytics.add(createTab(VaadinIcon.CHART, "Categorii (JFC)", JFChart_OLAP_VIEW_SALES_CTG_PROD.class));
        tabsAnalytics.add(createTab(VaadinIcon.PIE_CHART, "Categorii (SOC)", SOChart_OLAP_VIEW_SALES_CTG_PROD.class));
        tabsAnalytics.add(createTab(VaadinIcon.USER_CHECK, "Clienti/Stat (JFC)", JFChart_OLAP_DIM_CUSTS_CITIES_DEPTS.class));
        tabsAnalytics.add(createTab(VaadinIcon.GLOBE_WIRE, "Clienti/Stat (SOC)", SOChart_OLAP_DIM_CUSTS_CITIES_DEPTS.class));
        tabsAnalytics.add(createTab(VaadinIcon.TRUCK, "Eficienta Logistica", JFChart_LOGISTICS_EFFICIENCY.class));

        tabsAnalytics.setOrientation(Tabs.Orientation.VERTICAL);
        accordion.add("3. Analize si Dashboard", tabsAnalytics);

        return accordion;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {}
}