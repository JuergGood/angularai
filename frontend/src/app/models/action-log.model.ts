export interface ActionLog {
    id: number;
    timestamp: string;
    login: string;
    action: string;
    details: string;
}

export interface ActionLogResponse {
    content: ActionLog[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}
