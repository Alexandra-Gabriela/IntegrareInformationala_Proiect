package org.spark.service;

import jakarta.annotation.PostConstruct;
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

    private void startThriftServer2() {
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

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                logger.info(">>> ASTEPTARE INITIALIZARE SISTEM (10 sec)...");
                Thread.sleep(10000);
                executeSqlScript("scripts/SparkSQL_OLAP_Multidimensional_Analytical.sql");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void executeSqlScript(String path) {
        try {
            org.springframework.core.io.ClassPathResource res = new org.springframework.core.io.ClassPathResource(path);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(res.getInputStream()));
            String content = reader.lines().filter(line -> !line.trim().startsWith("--")).collect(java.util.stream.Collectors.joining(" "));

            for (String sql : content.split(";")) {
                if (!sql.trim().isEmpty()) {
                    logger.info(">>> EXECUTARE: " + sql.trim());
                    // CRITIC: .collect() forțează Spark să facă apelurile HTTP către JPA/Mongo
                    this.spark.sql(sql.trim()).collect();
                }
            }
        } catch (Exception e) {
            logger.severe("EROARE SCRIPT: " + e.getMessage());
        }
    }
}
