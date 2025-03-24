package dev.kapiaszczyk.mpp.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.kapiaszczyk.mpp.constants.Constants.RABBIT_REQUEST_QUEUE;
import static dev.kapiaszczyk.mpp.constants.Constants.RABBIT_RESPONSE_QUEUE;

/**
 * Configuration class for RabbitMQ.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue photosToBeTaggedQueue() {
        return new Queue(RABBIT_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue taggingResponseQueue() {
        return new Queue(RABBIT_RESPONSE_QUEUE, true);
    }

}
