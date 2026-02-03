import { User } from "./user";

export interface Department {
    id: number;
    name: string;
    members: User[];
    supervisorName: string;
}
