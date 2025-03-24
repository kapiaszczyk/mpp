package dev.kapiaszczyk.mpp.responses;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;

/**
 * Represents response containing photo file and its metadata.
 */
public class PhotoDownloadResponse {
    GridFSDownloadStream file;
    GridFSFile metadata;

    public PhotoDownloadResponse(GridFSDownloadStream file, GridFSFile metadata) {
        this.file = file;
        this.metadata = metadata;
    }

    public GridFSDownloadStream getFile() {
        return file;
    }

    public GridFSFile getMetadata() {
        return metadata;
    }
}
