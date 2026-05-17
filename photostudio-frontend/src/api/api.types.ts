import {type EquipmentCategory , type EventStatus, type ReservationItemStatus} from "@/lib/constants"

export type EventType = 'KWF' | "SKN" | "WRSS" | "URSS" | "AGH" | "AKRE" | "CM" | "AKT" | "PRIVATE"
export type createEventType = {
    date: string;
    time: string;
    name: string;
    description: string;
    location: string
    numberOfPeopleRequired: string;
    type: EventType;
}
export interface EventRequest {
id: string
eventId: string
userId: string
eventName: string
eventDate: string
eventLocation: string
createdDate: string
createdTime: string
comment?: string
status: ReservationItemStatus
}


export function isEventType(value: string): value is EventType {
  return (
    value === "KWF" ||
    value === "SKN" ||
    value === "WRSS" ||
    value === "URSS" ||
    value === "AGH" ||
    value === "AKRE" ||
    value === "CM" ||
    value === "AKT" ||
    value === "PRIVATE"
  );
}
export type UserRoleType = "ADMIN" | "USER" | "MODERATOR" | "SUPER_ADMIN";
export interface UserType {
    name: string;
    surname: string;
    email: string;
    username: string;
    phoneNumber: number;
}
export interface UserFullType extends UserType{
    role: UserRoleType;
    activeMember: boolean;
}

export interface Equipment {
  name: string
  activeMembers: boolean
  statutoryEvent: boolean
  equipmentCategory: EquipmentCategory
}

export type User = {
  id: number;
  name: string;
  surname: string;
  email: string;
  username: string;
  activeMember: boolean;
  phoneNumber: string;
  role: string;
};

export interface Equipment {
  id: string;
  name: string;
  activeMembers: boolean;
  statutoryEvent: boolean;
  equipmentCategory: EquipmentCategory;
}


export interface Event {
    id: string
    date: string
    time: string
    name: string
    description?: string
    location: string
    numberOfPeopleRequired: number
    numberOfAssignedPeople: number
    type: EventType
    status: EventStatus
}