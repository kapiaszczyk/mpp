import { PhotoMetadata } from "./photo-metadata.model";

export interface PhotoMetadataWithBlobUrls extends PhotoMetadata {
    fullUrl: string;
    url: string;
}