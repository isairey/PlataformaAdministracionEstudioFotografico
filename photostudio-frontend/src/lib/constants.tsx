export type EventType =
  | "KWF"
  | "SKN"
  | "WRSS"
  | "URSS"
  | "AGH"
  | "AKRE"
  | "CM"
  | "AKT"
  | "PRIVATE";
export type EventStatus = "PLANNED" | "COMPLETED" | "CANCELLED";
export type ReservationStatus = "NOT_RESOLVED" | "RESOLVED" | "CANCELLED";
export type ReservationItemStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
export type EquipmentCategory =
  | "CAMERA"
  | "BATTERY"
  | "LENS"
  | "STUDIO"
  | "LIGHTING"
  | "TRIPOD"
  | "ACCESSORIES";

export type EventRequestStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
export const eventRequestStatusLabels: Record<EventRequestStatus, string> = {
  PENDING: "Oczekująca",
  APPROVED: "Zaakceptowana",
  REJECTED: "Odrzucona",
  CANCELLED: "Anulowana",
};

export const eventStatusColors: Record<EventStatus, string> = {
  PLANNED: "bg-yellow-block text-white",
  COMPLETED: "bg-green-block text-white",
  CANCELLED: "bg-red-block text-white",
};

export const eventTypeLabels: Record<EventType, string> = {
  KWF: "KWF",
  SKN: "SKN",
  WRSS: "WRSS",
  URSS: "URSS",
  AGH: "AGH",
  AKRE: "AKRE",
  CM: "CM",
  AKT: "AKT",
  PRIVATE: "PRYWATNE",
};

export const eventStatusLabels: Record<EventStatus, string> = {
  PLANNED: "Zaplanowane",
  COMPLETED: "Zrealizowane",
  CANCELLED: "Odwołane",
};

export const reservationStatusColors: Record<ReservationStatus, string> = {
  NOT_RESOLVED: "bg-yellow-block text-white",
  RESOLVED: "bg-green-block text-white",
  CANCELLED: "bg-red-block text-white",
};

export const reservationStatusLabels: Record<ReservationStatus, string> = {
  NOT_RESOLVED: "Nierozpatrzona",
  RESOLVED: "Rozpatrzona",
  CANCELLED: "Anulowana",
};

export const reservationItemStatusColors: Record<ReservationItemStatus, string> = {
  PENDING: "bg-yellow-block text-white",
  APPROVED: "bg-green-block text-white",
  REJECTED: "bg-red-block text-white",
  CANCELLED: "bg-gray-400 text-white",
};

export const reservationItemStatusLabels: Record<ReservationItemStatus, string> = {
  PENDING: "Oczekujący",
  APPROVED: "Zaakceptowany",
  REJECTED: "Odrzucony",
  CANCELLED: "Anulowany",
};

export const equipmentCategoriesLabels: Record<EquipmentCategory, string> = {
  CAMERA: "Aparat",
  BATTERY: "Bateria",
  LENS: "Obiektyw",
  STUDIO: "Sprzęt studyjny",
  LIGHTING: "Oświetlenie",
  TRIPOD: "Statyw",
  ACCESSORIES: "Akcesorium",
};
