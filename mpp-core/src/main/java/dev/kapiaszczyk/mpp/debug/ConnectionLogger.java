package dev.kapiaszczyk.mpp.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs connection URIs for MongoDB and RabbitMQ on application startup.
 */
@Component
public class ConnectionLogger implements CommandLineRunner, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionLogger.class);

    private Environment environment;

    @Override
    public void run(String... args) throws Exception {
        logMongoDBConnection();
        logRabbitMQConnection();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private void logMongoDBConnection() {
        String port = environment.getProperty("spring.data.mongodb.port");
        String host = environment.getProperty("spring.data.mongodb.host");
        String username = environment.getProperty("spring.data.mongodb.username");
        String password = "*****";
        String database = environment.getProperty("spring.data.mongodb.database");
        logger.info("MongoDB connection URI: {}", String.format("mongodb://%s:%s@%s:%s/%s", username, password, host, port, database));
    }

    private void logRabbitMQConnection() {
        String port = environment.getProperty("spring.rabbitmq.port");
        String host = environment.getProperty("spring.rabbitmq.host");
        String username = environment.getProperty("spring.rabbitmq.username");
        String password = "*****";
        String virtualHost = environment.getProperty("spring.rabbitmq.virtual-host");
        logger.info("RabbitMQ connection URI: {}", String.format("amqp://%s:%s@%s:%s/%s", username, password, host, port, virtualHost));
    }

}
