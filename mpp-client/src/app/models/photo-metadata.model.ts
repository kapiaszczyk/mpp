export interface PhotoMetadata {
    id: string;
    filename: string;
    contentType: string;
    size: number;
    uploadDate: Date;
    userId: string;
    tags: string[];
    gridFsId: string;
    domainId: string;
    thumbnailId: string;
}
