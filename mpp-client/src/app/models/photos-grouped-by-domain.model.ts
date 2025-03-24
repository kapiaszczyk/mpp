import { PhotoMetadataWithBlobUrls } from "./photo-metadata-with-urls.model";

export interface PhotosGroupedByAlbum {
    albumId: string;
    albumName: string;
    photos: PhotoMetadataWithBlobUrls[];
}
