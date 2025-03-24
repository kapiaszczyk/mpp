export interface Domain {
    id: string;
    name: string;
    parentId: string;
    path: string;
    permissions: Map<string, string>;
    ownerId: string;
    createdAt: Date;
    photoCount: number;
    thumbnailId?: string;
    thumbnailUrl?: string;
}


export function mapToDomain(data: any): Domain {
    return {
        id: data.id,
        name: data.name,
        parentId: data.parentId,
        path: data.path,
        permissions: new Map(Object.entries(data.permissions)),
        ownerId: data.ownerId,
        createdAt: new Date(data.createdAt),
        photoCount: data.photoCount,
        thumbnailId: data.thumbnailId,
    };
}
