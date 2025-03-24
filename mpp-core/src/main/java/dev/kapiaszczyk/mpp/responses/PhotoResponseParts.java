package dev.kapiaszczyk.mpp.responses;

import org.springframework.core.io.InputStreamResource;

/**
 * Represents parts of a photo response in a ResponseEntity.
 */
public class PhotoResponseParts {

    private final InputStreamResource resource;
    private final String filename;
    private final String contentType;
    private final long size;

    public PhotoResponseParts(InputStreamResource resource, String filename, String contentType, long size) {
        this.resource = resource;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }

    public InputStreamResource getResource() {
        return resource;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

}
