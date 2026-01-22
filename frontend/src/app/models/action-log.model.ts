export interface ActionLog {
    id: number;
    timestamp: string;
    login: string;
    action: string;
    details: string;
    ipAddress?: string;
    country?: string;
    city?: string;
    latitude?: number;
    longitude?: number;
    userAgent?: string;
}

export interface ActionLogResponse {
    content: ActionLog[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}
