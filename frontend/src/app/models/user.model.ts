export interface User {
    id?: number;
    firstName: string;
    lastName: string;
    login: string;
    password?: string;
    email: string;
    birthDate?: string | null;
    address: string;
    role?: string;
    createdAt?: string;
    recaptchaToken?: string;
}
