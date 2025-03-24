package dev.kapiaszczyk.mpp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.kapiaszczyk.mpp.constants.Constants.*;

@Component
public class TaggingResponseListener {

    @Autowired
    private PhotoService photoService;

    private static final Logger logger = LoggerFactory.getLogger(TaggingResponseListener.class);

    @RabbitListener(queues = RABBIT_RESPONSE_QUEUE)
    public void handlePhotoTaggedResponse(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> response = objectMapper.readValue(message, new TypeReference<>() {
            });

            String photoId = response.get(TAGGING_RESPONSE_PHOTO_KEY).trim();
            String tag = response.get(TAGGING_RESPONSE_TAG_KEY).trim();

            if (photoId.isEmpty() || tag.isEmpty()) {
                logger.error("Invalid response from tagging service: {}", message);
                return;
            }

            photoService.tagPhoto(photoId, tag);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse tagging response: {}", message, e);
        }
    }

}
