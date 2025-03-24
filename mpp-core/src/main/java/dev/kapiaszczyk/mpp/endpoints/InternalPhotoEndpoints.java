package dev.kapiaszczyk.mpp.endpoints;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;

/**
 * Endpoint for photo operations that are only available internally.
 */
@Hidden
public interface InternalPhotoEndpoints {
    ResponseEntity<?> downloadPhotoInternal(String photoId);
}
