package org.spark.service;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class SparkSQLService {
    private static Logger logger = Logger.getLogger(SparkSQLService.class.getName());
    private SparkSession spark;

    public SparkSession getSpark() {
        return spark;
    }

    public SparkSQLService() {
        startThriftServer2();
        // PASUL CRITIC: Rulăm scriptul de integrare imediat ce Spark este gata
        executeSqlScript("scripts/SparkSQL_OLAP_Multidimensional_Analytical.sql");
    }

    private void startThriftServer2(){
        logger.info(">>> HiveThriftServer2 Starting ....");
        this.spark = SparkSession.builder()
                .master("local[*]")
                .config("spark.ui.port", "8081")
                .appName("SparkSQL-REST.Server")
                .enableHiveSupport()
                .config("hive.server2.thrift.port", "10000")
                .getOrCreate();

        HiveThriftServer2.startWithContext(spark.sqlContext());
        logger.info(">>> HiveThriftServer2 started successfully!");
    }

    private void executeSqlScript(String scriptPath) {
        logger.info(">>> Running initialization script: " + scriptPath);
        try {
            ClassPathResource resource = new ClassPathResource(scriptPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            // Citim fișierul și eliminăm comentariile pentru a nu bloca Spark
            String scriptContent = reader.lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining(" "));

            // Împărțim în comenzi individuale după ';'
            String[] queries = scriptContent.split(";");

            for (String query : queries) {
                String cleanQuery = query.trim();
                if (!cleanQuery.isEmpty()) {
                    try {
                        logger.info(">>> Executing Spark SQL Query...");
                        this.spark.sql(cleanQuery);
                    } catch (Exception e) {
                        // Unele drop-uri pot eșua dacă view-ul nu există, e normal
                        logger.warning(">>> Query warning (skipping): " + e.getMessage());
                    }
                }
            }
            logger.info(">>> Spark SQL initialization completed.");
        } catch (Exception e) {
            logger.severe(">>> CRITICAL ERROR: Failed to execute SQL script: " + e.getMessage());
        }
    }
}