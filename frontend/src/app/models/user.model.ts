export interface User {
    id?: number;
    firstName: string;
    lastName: string;
    login: string;
    password?: string;
    email: string;
    birthDate: string;
    address: string;
    role?: string;
}
