package org.j4di.visualisation.rest;

import com.jayway.jsonpath.JsonPath;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/*
Visualisation Data Service to call REST.APIs
 */
public class QueryRESTDataClient {
    private static Logger logger = Logger.getLogger(QueryRESTDataClient.class.getName());
    // REST.API query methods: simple String typed responses
    public static String getRESTDataDocument(String httpURL){
        RestTemplate restTemplate = new RestTemplate();
        //
        logger.info("DEBUG: getRESTDataDocument ...");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        // Manage credentials
        String[] credentials = parseCredentials(httpURL);
        if (credentials != null) {
            String username = credentials[0];
            String password = credentials[1];
            if (username != null && password != null)
            headers.setBasicAuth(username, password);
            httpURL = httpURL.replace(credentials[0] + ":" + credentials[1] + "@", "");
        }
        //
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                httpURL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        String viewList = responseEntity.getBody();
        return viewList;
    }

    public static String getAutoRESTDataDocument(String httpURL, String sqlQuery){
        RestTemplate restTemplate = new RestTemplate();
        //
        logger.info("DEBUG: getRESTDataDocument ...");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        // Manage credentials
        String[] credentials = parseCredentials(httpURL);
        if (credentials != null) {
            String username = credentials[0];
            String password = credentials[1];
            if (username != null && password != null)
                headers.setBasicAuth(username, password);
            httpURL = httpURL.replace(credentials[0] + ":" + credentials[1] + "@", "");
        }
        //
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                httpURL,
                HttpMethod.POST,
                new HttpEntity<>(sqlQuery, headers),
                String.class
        );

        String viewList = responseEntity.getBody();
        return viewList;
    }

    public static String[] parseCredentials(String urlString) {
        try {
            URI uri = new URI(urlString);
            // Extract the user-info part (username:password)
            String userInfo = uri.getUserInfo();

            if (userInfo != null && userInfo.contains(":")) {
                String[] credentials = userInfo.split(":", 2);
                String username = credentials[0];
                String password = credentials[1];
                //
                logger.info("Username: " + username);
                logger.info("Password: " + password);
                //
                return credentials;
            } else {
                logger.info("No credentials found in the URL.");
            }
        } catch (URISyntaxException e) {
            logger.info("Invalid URL format: " + e.getMessage());
            throw new RuntimeException("Invalid URL format: " + e.getMessage());
        }
        return null;
    }

    // REST.API query methods: data typed responses
    // http://localhost:9990/DSA-SparkSQL-Service/_sqlrest/query
    // SELECT * FROM OLAP_VIEW_SALES_DEP_CIT_CUST
    // https://www.baeldung.com/spring-parameterized-type-reference
    public static <T> SQLResponse<T> getRESTData(String urlString, String restData){
        RestTemplate restTemplate = new RestTemplate();
        //
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        // Manage credentials
        headers.setBasicAuth("spark", "sql");
        //
        SQLResponse<T> responseEntity = restTemplate.exchange(
                urlString,
                HttpMethod.POST,
                new HttpEntity<>(restData, headers),
                new ParameterizedTypeReference<SQLResponse<T>>() {}
        ).getBody();
        //
        logger.info("DEBUG getRESTData: responseEntity: " + responseEntity);
        //
        return responseEntity;
    }

    // JSONPath: $.response
    // https://github.com/json-path/JsonPath
    public static <T> List<T> getRESTDataList(String urlString, String restData, String jsonPath){
        RestTemplate restTemplate = new RestTemplate();
        //
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        // Manage credentials
        headers.setBasicAuth("spark", "sql");
        //
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                urlString,
                HttpMethod.POST,
                new HttpEntity<>(restData, headers),
                String.class
        );
        //
        String viewList = responseEntity.getBody();
        logger.info("DEBUG: viewList: " + viewList);
        //T[] dataArray = JsonPath.parse(viewList).read(jsonPath, T[].class);
        T[] dataArray = JsonPath.parse(viewList).read(jsonPath);
        //
        logger.info("DEBUG getRESTDataList: dataArray: " + dataArray);
        //
        return Arrays.asList(dataArray);
    }
}
/*
https://www.baeldung.com/spark-framework-rest-api
*/