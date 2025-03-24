package dev.kapiaszczyk.mpp.requests;

/**
 * Represents request to delete an album.
 */
public class AlbumDeletionRequest {

    /**
     * Specifies if children albums and photos in those albums should be moved
     * to the parent of the album to be deleted or should they be deleted too.
     */
    public boolean moveChildrenAlbumsToParentAlbum;

    /**
     * Specifies if photos in the album to be deleted should be moved
     * to the parent of the album to be deleted or should they be deleted too.
     */
    public boolean movePhotosToParentAlbum;

    public AlbumDeletionRequest(boolean moveChildrenAlbumsToParentAlbum, boolean movePhotosToParentAlbum) {
        this.moveChildrenAlbumsToParentAlbum = moveChildrenAlbumsToParentAlbum;
        this.movePhotosToParentAlbum = movePhotosToParentAlbum;
    }

    public boolean moveChildrenDataToParent() {
        return moveChildrenAlbumsToParentAlbum;
    }

    public boolean movePhotosToParentAlbum() {
        return movePhotosToParentAlbum;
    }
}
