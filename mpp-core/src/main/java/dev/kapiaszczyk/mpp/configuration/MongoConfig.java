package dev.kapiaszczyk.mpp.configuration;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for MongoDB.
 */
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${authentication.database}")
    private String authenticationDatabase;

    @Bean
    public GridFSBucket gridFSBucket() {
        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s", username, password, host, port, database, authenticationDatabase);
        MongoDatabase database = MongoClients.create(connectionString).getDatabase("mpp");
        return GridFSBuckets.create(database);
    }

}