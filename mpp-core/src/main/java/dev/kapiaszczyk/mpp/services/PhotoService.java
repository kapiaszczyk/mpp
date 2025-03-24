package dev.kapiaszczyk.mpp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import dev.kapiaszczyk.mpp.constants.Constants;
import dev.kapiaszczyk.mpp.models.api.PhotoGroupedByAlbum;
import dev.kapiaszczyk.mpp.models.database.Album;
import dev.kapiaszczyk.mpp.models.database.PhotoMetadata;
import dev.kapiaszczyk.mpp.repositories.PhotoRepository;
import dev.kapiaszczyk.mpp.responses.PhotoDownloadResponse;
import dev.kapiaszczyk.mpp.util.Either;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static dev.kapiaszczyk.mpp.constants.Constants.RABBIT_REQUEST_QUEUE;
import static dev.kapiaszczyk.mpp.errors.GenericErrors.FILE_NOT_FOUND;

@Service
public class PhotoService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);
    @Autowired
    private final GridFSBucket gridFSBucket;
    @Autowired
    private final MongoTemplate mongoTemplate;
    @Autowired
    private final PhotoRepository photoRepository;
    @Autowired
    private final RabbitTemplate rabbitTemplate;

    public PhotoService(GridFSBucket gridFSBucket, MongoTemplate mongoTemplate, PhotoRepository photoRepository, RabbitTemplate rabbitTemplate) {
        this.gridFSBucket = gridFSBucket;
        this.mongoTemplate = mongoTemplate;
        this.photoRepository = photoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Upload a photo to the database and sends a message to RabbitMQ
     *
     * @param file          the photo file
     * @param userId        the ID of the user uploading the photo
     * @param targetAlbumId the ID of the album the photo should be uploaded to
     * @return the ID of the uploaded photo
     * @throws RuntimeException if an error occurs while uploading the photo
     */
    public String uploadPhoto(MultipartFile file, String userId, String targetAlbumId) throws Exception {
        PhotoMetadata metadata = extractDataFromFile(file);

        try (InputStream inputStream = file.getInputStream();
             InputStream thumbnailStream = file.getInputStream()) {

            // Upload photo to GridFS
            ObjectId fileId = uploadFileToGridFS(metadata, inputStream, userId);
            logger.info("Uploaded photo with ID: {}", fileId);

            // Generate and upload thumbnail
            ObjectId thumbnailId = uploadThumbnailToGridFS(metadata, thumbnailStream, userId);
            logger.info("Uploaded thumbnail with ID: {}", thumbnailId);

            // Save metadata
            metadata.setUserId(userId);
            metadata.setAlbumId(targetAlbumId);
            metadata.setGridFsId(fileId.toString());
            metadata.setThumbnailId(thumbnailId.toString());
            mongoTemplate.save(metadata);

            // Send message to RabbitMQ
            sendMessageToQueue(metadata.getId());
            logger.info("Sent message to RabbitMQ with photo ID: {}", metadata.getId());

            incrementPhotoCount(targetAlbumId);

            return metadata.getId();
        } catch (IOException | MongoException e) {
            logger.error("Error uploading photo: {}", e.getMessage(), e);
            throw new IOException("Failed to upload photo", e);
        }
    }

    /**
     * Uploads a file to GridFS and returns its ID.
     */
    private ObjectId uploadFileToGridFS(PhotoMetadata metadata, InputStream inputStream, String userId) {
        GridFSUploadOptions options = new GridFSUploadOptions()
                .metadata(new org.bson.Document("userId", userId)
                        .append("contentType", metadata.getContentType()));

        return gridFSBucket.uploadFromStream(metadata.getFilename(), inputStream, options);
    }

    /**
     * Generates and uploads a thumbnail to GridFS.
     */
    private ObjectId uploadThumbnailToGridFS(PhotoMetadata metadata, InputStream thumbnailStream, String userId) throws IOException {
        ByteArrayOutputStream thumbnailOutput = generateThumbnail(thumbnailStream);
        return gridFSBucket.uploadFromStream("thumbnail_" + metadata.getFilename(),
                new ByteArrayInputStream(thumbnailOutput.toByteArray()),
                new GridFSUploadOptions().metadata(new org.bson.Document("userId", userId)));
    }

    /**
     * Sends a message to RabbitMQ with the given photo ID.
     */
    private void sendMessageToQueue(String photoId) {
        rabbitTemplate.convertAndSend(RABBIT_REQUEST_QUEUE, createJsonMessage(photoId));
    }

    private void incrementPhotoCount(String albumId) {
        // do nothing
    }

    /**
     * Download a photo from the database
     *
     * @param gridFsId the ID of the photo in GridFS
     * @return response containing the photo download stream and the GridFS file
     * @throws FileNotFoundException if the photo is not found
     */
    public PhotoDownloadResponse downloadPhoto(String gridFsId) throws FileNotFoundException {
        if (gridFsId == null || gridFsId.isBlank()) {
            logger.error("Invalid Photo ID: {}", gridFsId);
            throw new IllegalArgumentException("Photo ID cannot be null or empty");
        }
        try {
            ObjectId objectId = new ObjectId(gridFsId);
            GridFSDownloadStream fileStream = gridFSBucket.openDownloadStream(objectId);
            return new PhotoDownloadResponse(fileStream, gridFSBucket.find(new BasicDBObject("_id", objectId)).first());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid ObjectId format for Photo ID: {}", gridFsId, e);
            throw new IllegalArgumentException("Invalid Photo ID format", e);
        } catch (MongoGridFSException e) {
            logger.error("File with ID {} not found in GridFS", gridFsId, e);
            throw new FileNotFoundException("Photo not found");
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving photo with ID: {}", gridFsId, e);
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }

    /**
     * Download a thumbnail from the database
     *
     * @param thumbnailId the ID of the thumbnail in GridFS
     * @return response containing the thumbnail download stream and the GridFS file
     * @throws FileNotFoundException if the thumbnail is not found
     */
    public PhotoDownloadResponse downloadThumbnail(String thumbnailId) throws FileNotFoundException {
        if (thumbnailId == null || thumbnailId.isBlank()) {
            logger.error("Invalid Thumbnail ID: {}", thumbnailId);
            throw new IllegalArgumentException("Thumbnail ID cannot be null or empty");
        }
        try {
            ObjectId objectId = new ObjectId(thumbnailId);
            GridFSDownloadStream fileStream = gridFSBucket.openDownloadStream(objectId);
            GridFSFile gridFSFile = gridFSBucket.find(new BasicDBObject("_id", objectId)).first();
            return new PhotoDownloadResponse(fileStream, gridFSFile);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid ObjectId format for Thumbnail ID: {}", thumbnailId, e);
            throw new IllegalArgumentException("Invalid Thumbnail ID format", e);
        } catch (MongoGridFSException e) {
            logger.error("File with ID {} not found in GridFS", thumbnailId, e);
            throw new FileNotFoundException("Thumbnail not found");
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving thumbnail with ID: {}", thumbnailId, e);
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }

    /**
     * Retrieve the metadata of all photos in a given album
     *
     * @param photoId the ID of the photo
     * @param albumId the ID of the album
     */
    public void movePhotoToAlbum(String photoId, String albumId) {
        PhotoMetadata metadata = photoRepository.findById(photoId).orElseThrow(() -> new IllegalArgumentException(FILE_NOT_FOUND));
        metadata.setAlbumId(albumId);
        mongoTemplate.save(metadata);
    }

    /**
     * Retrieve the metadata of all photos in a given album
     *
     * @param albumId the ID of the album
     * @return a list of photo metadata
     */
    public List<PhotoMetadata> getAllPhotoMetadataInAlbum(String albumId) {
        return mongoTemplate.find(new Query(Criteria.where("albumId").is(albumId)), PhotoMetadata.class);
    }

    /**
     * Retrieve the metadata of a photo by its ID
     *
     * @param photoId the ID of the photo
     * @return the photo metadata
     */
    public Optional<PhotoMetadata> getPhotoById(String photoId) {
        return photoRepository.findById(photoId);
    }

    /**
     * Move photos from one album to another
     *
     * @param albumId       the ID of the album
     * @param targetAlbumId the ID of the target album
     */
    public void movePhotosToAlbum(String albumId, String targetAlbumId) {
        mongoTemplate.updateMulti(new Query(Criteria.where("albumId").is(albumId)),
                new org.springframework.data.mongodb.core.query.Update().set("albumIdalbumId", targetAlbumId), PhotoMetadata.class);
    }

    /**
     * Retrieve all tags in user's photos
     *
     * @param userId the ID of the user
     * @return a list of tags
     */
    public List<String> getAllTagsForUser(String userId) {
        List<PhotoMetadata> photos = mongoTemplate.find(new Query(Criteria.where("userId").is(userId)), PhotoMetadata.class);
        return photos.stream().map(PhotoMetadata::getTags).toList().stream().distinct().flatMap(Collection::stream).distinct().collect(Collectors.toList());
    }

    /**
     * Retrieve all photo metadata with a given tag in user's photos
     *
     * @param userId the ID of the user
     * @param tag    the tag
     * @return a list of photo metadata
     */
    public List<PhotoMetadata> getPhotosWithTag(String userId, String tag) {
        return mongoTemplate.find(new Query(Criteria.where("userId").is(userId).and("tags").is(tag)), PhotoMetadata.class);
    }

    /**
     * Retrieve all photo metadata with a given tag in a given album
     *
     * @param userId the ID of the user
     * @param tag    the tag
     * @param album  the album
     * @return a list of photo metadata
     */
    public List<PhotoMetadata> getPhotosWithTagInAlbum(String userId, String tag, Album album) {
        return mongoTemplate.find(new Query(Criteria.where("userId").is(userId).and("tags").is(tag).and("albumId").is(album.getId())), PhotoMetadata.class);
    }

    /**
     * Retrieve all tags in a given album for a user
     *
     * @param userId the ID of the user
     * @param album  the album
     * @return a list of tags
     */
    public Either<String, List<String>> getAllTagsForUserInAlbum(String userId, Album album) {
        List<PhotoMetadata> photos = mongoTemplate.find(new Query(Criteria.where("userId").is(userId).and("albumId").is(album.getId())), PhotoMetadata.class);
        return Either.ofRight(photos.stream().map(PhotoMetadata::getTags).toList().stream().distinct().flatMap(Collection::stream).distinct().collect(Collectors.toList()));
    }

    /**
     * Duplicate a photo to another album
     *
     * @param photoId       the ID of the photo
     * @param targetAlbumId the ID of the target album
     * @param userId        the ID of the user
     * @return the photo metadata
     */
    public String duplicatePhoto(String photoId, String targetAlbumId, String userId) {
        // Fetch the metadata of the original photo
        PhotoMetadata originalMetadata = mongoTemplate.findById(photoId, PhotoMetadata.class);

        // Create a copy of the metadata with new albumId, owner, and upload date
        // The same GridFS ID is used to reference the same file for efficiency
        PhotoMetadata newMetadata = duplicateMetadata(originalMetadata)
                .userId(userId)
                .albumId(targetAlbumId);

        mongoTemplate.save(newMetadata);

        return newMetadata.getId();
    }

    /**
     * Check if a file is valid
     *
     * @param file the file
     * @return true if the file is valid, false otherwise
     */
    public boolean isFileValid(MultipartFile file) {
        return !file.isEmpty() && isPhotoFormatSupported(Objects.requireNonNull(file.getOriginalFilename())) && file.getSize() <= 26214400;
    }

    /**
     * Delete all photos in an album
     *
     * @param albumId the ID of the album
     */
    public void deleteAllPhotosInAlbum(String albumId) {
        // Since photo duplication is supported, we cannot just delete the files from GridFS
        List<PhotoMetadata> photosToBeDeleted = mongoTemplate.find(new Query(Criteria.where("albumId").is(albumId)), PhotoMetadata.class);

        // TODO: This is a temporary solution, as it is not efficient to check for each photo with a call to the database
        // Check if there are any other photos, where the gridFsId is the same as the one to be deleted
        photosToBeDeleted.forEach(photo -> {
            if (!mongoTemplate.exists(new Query(Criteria.where("gridFsId").is(photo.getGridFsId()).and("albumId").ne(albumId)), PhotoMetadata.class)) {
                // If there are no other photos with the same gridFsId, delete the file from GridFS
                gridFSBucket.delete(new ObjectId(photo.getGridFsId()));
            }
        });

        // The metadata can be deleted safely
        mongoTemplate.remove(new Query(Criteria.where("albumId").is(albumId)), PhotoMetadata.class);
    }

    /**
     * Delete a photo by its ID
     *
     * @param photoId the ID of the photo
     */
    public void deletePhotoById(String photoId) {
        // Since photo duplication is supported, we cannot just delete the file from GridFS
        PhotoMetadata photo = mongoTemplate.findById(photoId, PhotoMetadata.class);

        // Check if there are any other photos, where the gridFsId is the same as the one to be deleted
        if (!mongoTemplate.exists(new Query(Criteria.where("gridFsId").is(photo.getGridFsId()).and("albumId").ne(photo.getAlbumId())), PhotoMetadata.class)) {
            // If there are no other photos with the same gridFsId, delete the file from GridFS
            gridFSBucket.delete(new ObjectId(photo.getGridFsId()));
        }

        // The metadata can be deleted safely
        mongoTemplate.remove(new Query(Criteria.where("id").is(photoId)), PhotoMetadata.class);
    }

    /**
     * Add a tag to a photo
     *
     * @param photoId the ID of the photo
     * @param tag     the tag
     */
    public void tagPhoto(String photoId, String tag) {
        PhotoMetadata photo = mongoTemplate.findById(photoId, PhotoMetadata.class);
        photo.addTag(tag);
        mongoTemplate.save(photo);
    }

    /**
     * Edit tags of a photo
     *
     * @param photoId the ID of the photo
     * @param tags    the new tags
     */
    public void editTags(String photoId, Set<String> tags) {
        PhotoMetadata photo = mongoTemplate.findById(photoId, PhotoMetadata.class);
        photo.setTags(tags);
        mongoTemplate.save(photo);
    }

    /**
     * Get the amount of photos in the system
     *
     * @return the amount of photos in the system
     */
    public long getNumberOfPhotos() {
        return mongoTemplate.count(new Query(), PhotoMetadata.class);
    }

    /**
     * Get the amount of space used in the system
     *
     * @return the amount of space used in the system
     */
    public Long getSpaceUsed() {
        return gridFSBucket.find().into(new ArrayList<>()).stream().mapToLong(GridFSFile::getLength).sum();
    }

    /**
     * Get the amount of space used by an album
     *
     * @param albumId the ID of the album
     * @return the amount of space used by the album
     */
    public Long getSpaceUsedByAlbum(String albumId) {
        return mongoTemplate.find(new Query(Criteria.where("albumId").is(albumId)), PhotoMetadata.class)
                .stream().mapToLong(PhotoMetadata::getSize).sum();
    }

    /**
     * Delete all photos uploaded by a user
     *
     * @param userId the ID of the user
     */
    public void deletePhotosByUser(String userId) {
        List<PhotoMetadata> photos = mongoTemplate.find(new Query(Criteria.where("ownerId").is(userId)), PhotoMetadata.class);

        // Check if there are any other photos, where the gridFsId is the same as the one to be deleted
        for (PhotoMetadata photo : photos) {
            if (!mongoTemplate.exists(new Query(Criteria.where("gridFsId").is(photo.getGridFsId()).and("ownerId").ne(userId)), PhotoMetadata.class)) {
                // If there are no other photos with the same gridFsId, delete the file from GridFS
                gridFSBucket.delete(new ObjectId(photo.getGridFsId()));
            }
        }

        mongoTemplate.remove(new Query(Criteria.where("ownerId").is(userId)), PhotoMetadata.class);
    }

    /**
     * Get photos that have a given tag and group them by album
     *
     * @param tag    the tag
     * @param userId the ID of the user
     * @return a list of DTOs containing the album ID, album name, and a list of photos
     */
    public List<PhotoGroupedByAlbum> getPhotoMetadataByTagGroupedByAlbums(String tag, String userId) {
        // Get photos that have a given tag and group them by album
        // Photos must belong to the user (in this case they do not include shared photos)
        List<PhotoMetadata> photos = mongoTemplate.find(new Query(Criteria.where("userId").is(userId).and("tags").is(tag)), PhotoMetadata.class);

        // Group photos by album
        Map<String, List<PhotoMetadata>> photosByAlbum = photos.stream()
                .collect(Collectors.groupingBy(PhotoMetadata::getAlbumId));

        // Get album names (since user is the owner of the photos, no need to check permissions)
        // This could/should have been done by the album service
        Map<String, String> albumNames = mongoTemplate.find(new Query(Criteria.where("id").in(photosByAlbum.keySet())), Album.class)
                .stream().collect(Collectors.toMap(Album::getId, Album::getName));

        // Create DTOs
        return photosByAlbum.entrySet().stream()
                .map(entry -> new PhotoGroupedByAlbum(entry.getKey(), albumNames.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
    }

    private String createJsonMessage(String photoId) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("photoId", photoId);
            return new ObjectMapper().writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Error creating JSON message");
        }
    }

    private ByteArrayOutputStream generateThumbnail(InputStream inputStream) throws IOException {
        ByteArrayOutputStream thumbnailOutput = new ByteArrayOutputStream();
        try {
            Thumbnails.of(inputStream)
                    .size(200, 200)
                    .crop(Positions.CENTER)
                    .outputQuality(1.0)
                    .outputFormat("jpg")
                    .toOutputStream(thumbnailOutput);
        } catch (IOException e) {
            logger.error("Error generating thumbnail: {}", e.getMessage());
            throw e;
        }
        logger.info("Generated thumbnail");
        return thumbnailOutput;
    }

    private PhotoMetadata extractDataFromFile(MultipartFile file) {
        String filename = Objects.requireNonNull(file.getOriginalFilename());
        String contentType = file.getContentType();
        long size = file.getSize();
        Date uploadDate = Date.from(Instant.now());

        return new PhotoMetadata()
                .id(new ObjectId().toString())
                .filename(filename)
                .contentType(contentType)
                .size(size)
                .uploadDate(uploadDate);
    }

    private PhotoMetadata duplicateMetadata(PhotoMetadata originalMetadata) {
        return new PhotoMetadata()
                .id(new ObjectId().toString())
                .filename(originalMetadata.getFilename())
                .contentType(originalMetadata.getContentType())
                .size(originalMetadata.getSize())
                .uploadDate(Date.from(Instant.now()))
                .thumbnailId(originalMetadata.getThumbnailId())
                .tags(originalMetadata.getTags())
                .gridFs(originalMetadata.getGridFsId());
    }

    private boolean isPhotoFormatSupported(String filename) {
        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        String[] supportedFormats = Constants.SUPPORTED_FORMATS;
        return Arrays.stream(supportedFormats).anyMatch(supportedFormat -> supportedFormat.equalsIgnoreCase(fileExtension));
    }

}

