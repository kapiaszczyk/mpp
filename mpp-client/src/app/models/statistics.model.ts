export interface SystemStatistics {
    rolesInSystem: string[];
    numberOfUsersInSystem: number;
    numberOfPhotosInSystem: number;
    numberOfDomainsInSystem: number;
    usersInSystem: any;
    domainsInSystem: string[];
    spaceUsedInSystem: number;
}

export interface UserStatistics {
    userId: string;
    // Role of the user in the system
    role: string;
    username: string;
    email: string;
    // Domain statistics - domain name and used size
    albumStats: Map<string, number>;
    // Total space used by the user
    spaceUsed: number;
}
