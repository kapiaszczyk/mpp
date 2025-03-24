export interface CompositeDomainInfo {
    name: string;
    createdAt: Date;
    ownerUsername: string;
    usersWithAccess: Map<string, string>;
}

export interface DomainSharedUsersInfo {
    id: string;
    username: string;
    permission: string;
}
