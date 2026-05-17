import { type ColumnDef } from "@tanstack/react-table";
import { Badge } from "@/components/ui/badge";
import type { EquipmentCategory, EventType, EventStatus } from "@/lib/constants";

// ── Types ──

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

export type EquipmentItem = {
  id: number;
  name: string;
  activeMembers: boolean;
  statutoryEvent: boolean;
  equipmentCategory: EquipmentCategory;
};

export type EventItem = {
  id: number;
  name: string;
  description: string;
  date: string;
  time: string;
  location: string;
  numberOfPeopleRequired: number;
  type: EventType;
  status: EventStatus;
};

const roleLabels: Record<string, string> = {
  USER: "Użytkownik",
  MODERATOR: "Moderator",
  ADMIN: "Administrator",
  SUPER_ADMIN: "Super administrator",
};

const statusLabels: Record<string, string> = {
  PLANNED: "Zaplanowane",
  COMPLETED: "Zakończone",
  CANCELLED: "Anulowane",
};

// ── User columns ──

export const userColumns: ColumnDef<User>[] = [
  {
    accessorKey: "name",
    header: "Imię",
  },
  {
    accessorKey: "surname",
    header: "Nazwisko",
  },
  {
    accessorKey: "email",
    header: "Email",
    meta: { filterVariant: "text" as const },
  },
  {
    accessorKey: "username",
    header: "Nazwa",
    meta: { filterVariant: "text" as const },
  },
  {
    accessorKey: "role",
    header: "Rola",
    meta: { filterVariant: "select" as const },
    cell: ({ row }) => (
      <Badge variant="outline">{roleLabels[row.original.role] ?? row.original.role}</Badge>
    ),
  },
  {
    accessorKey: "activeMember",
    header: "Aktywny",
    meta: { filterVariant: "select" as const },
    cell: ({ row }) => (
      <Badge variant={row.original.activeMember ? "default" : "secondary"}>
        {row.original.activeMember ? "Tak" : "Nie"}
      </Badge>
    ),
  },
  {
    id: "actions",
    header: "Akcje",
    enableSorting: false,
    enableColumnFilter: false,
    cell: () => null, // placeholder – overridden in ModeratorPage
  },
];

// ── Equipment columns ──

export const equipmentColumns: ColumnDef<EquipmentItem>[] = [
  {
    accessorKey: "name",
    header: "Nazwa",
    meta: { filterVariant: "text" as const },
  },
  {
    accessorKey: "equipmentCategory",
    header: "Kategoria",
    meta: { filterVariant: "select" as const },
  },
  {
    accessorKey: "activeMembers",
    header: "Tylko dla aktywnych",
    cell: ({ row }) => (row.original.activeMembers ? "Tak" : "Nie"),
  },
  {
    accessorKey: "statutoryEvent",
    header: "Tylko dla statutowych",
    cell: ({ row }) => (row.original.statutoryEvent ? "Tak" : "Nie"),
  },
  {
    id: "actions",
    header: "Akcje",
    enableSorting: false,
    enableColumnFilter: false,
    cell: () => null, // placeholder – overridden in ModeratorPage
  },
];

// ── Event columns ──

export const eventColumns: ColumnDef<EventItem>[] = [
  {
    accessorKey: "name",
    header: "Nazwa",
    meta: { filterVariant: "text" as const },
  },
  {
    accessorKey: "date",
    header: "Data",
  },
  {
    accessorKey: "location",
    header: "Lokalizacja",
    meta: { filterVariant: "text" as const },
  },
  {
    accessorKey: "type",
    header: "Typ",
    meta: { filterVariant: "select" as const },
  },
  {
    accessorKey: "status",
    header: "Status",
    meta: { filterVariant: "select" as const },
    cell: ({ row }) => {
      const s = row.original.status;
      const variant = s === "COMPLETED" ? "default" : s === "CANCELLED" ? "destructive" : "secondary";
      return <Badge variant={variant}>{statusLabels[s] ?? s}</Badge>;
    },
  },
  {
    accessorKey: "numberOfPeopleRequired",
    header: "Wymagana liczba osób",
  },
  {
    id: "actions",
    header: "Akcje",
    enableSorting: false,
    enableColumnFilter: false,
    cell: () => null, // placeholder – overridden in ModeratorPage
  },
];