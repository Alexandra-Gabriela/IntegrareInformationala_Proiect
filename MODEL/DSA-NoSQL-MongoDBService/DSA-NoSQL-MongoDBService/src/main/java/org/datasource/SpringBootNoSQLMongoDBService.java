package org.datasource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import java.util.logging.Logger;

@SpringBootApplication(
		exclude={DataSourceAutoConfiguration.class},
		scanBasePackages = "org.datasource" // CRITIC: Scanează tot, inclusiv sub-pachetul .mongodb
)
public class SpringBootNoSQLMongoDBService extends SpringBootServletInitializer {
	private static final Logger logger = Logger.getLogger(SpringBootNoSQLMongoDBService.class.getName());

	public static void main(String[] args) {
		logger.info("Loading ... SpringBootNoSQLMongoDBService (Olist Data) ... JSON");
		SpringApplication.run(SpringBootNoSQLMongoDBService.class, args);
	}
}

//testing
//http://localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/CityView
//http://localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/ping
//http://localhost:8093/DSA-NoSQL-MongoDBService/rest/brazil/SellerView
