package org.spark.service.rest;

import org.spark.service.exception.RESTSQLWorkflowException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.logging.Logger;

@RestController
@RequestMapping("/_sqlrest")
public class RESTEnabledSQLService {
    private static Logger logger = Logger.getLogger(RESTEnabledSQLService.class.getName());

    @Value("${sparksql.rest.enabled:true}")
    private Boolean sparkRestEnabled = true;

    private SQLViewWorkflow sqlViewWorkflow;

    // MODIFICARE: Valori default pentru a preveni eroarea de configurare la startup
    private static String serverPort = "9990";
    private static String serverServletContextPath = "/DSA-SparkSQL-Service";

    @Value("${server.port:9990}")
    public void setServerPort(String port) { serverPort = port; }

    @Value("${server.servlet.context-path:/DSA-SparkSQL-Service}")
    public void setServerServletContextPath(String path) { serverServletContextPath = path; }

    public RESTEnabledSQLService(SQLViewWorkflow workflow) {
        this.sqlViewWorkflow = workflow;
    }

    public static String createJSONViewFromREST(String viewName, String restDataServiceHttpURL) {
        if (viewName == null || viewName.isEmpty()) throw new RuntimeException("REST Error: viewName is NULL");

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            String restDataEndpoint = String.format("http://localhost:%s%s/_sqlrest/create-json-view-from-rest?view_name=%s",
                    serverPort, serverServletContextPath, viewName);

            logger.info(">>> APELARE REST: " + restDataEndpoint);

            ResponseEntity<String> response = restTemplate.exchange(
                    restDataEndpoint, HttpMethod.POST, new HttpEntity<>(restDataServiceHttpURL, headers), String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RESTSQLWorkflowException("REST Error: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    public SQLResponse executePostQuery(@RequestBody String SQLQuery) {
        return sqlViewWorkflow.executeSQLQuery(SQLQuery);
    }

    @PostMapping("/create-json-view-from-rest")
    public SQLViewDefinition createJsonViewFromREST(@RequestParam("view_name") String viewName, @RequestBody String url) {
        return sqlViewWorkflow.createJsonViewFromREST(viewName, url);
    }
}