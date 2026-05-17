import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { DataTable } from "../../components/moderator_components/DataTable";
import { ServerEventDataTable } from "../../components/moderator_components/ServerEventDataTable";
import {
  userColumns,
  equipmentColumns,
  eventColumns,
  type User,
  type EquipmentItem,
  type EventItem,
} from "./moderatorColumns";
import { useState, useEffect, useMemo } from "react";
import { getAllUsers, deleteUser, adminUpdateUser} from "@/api/ApiUserController";
import {updateEvent, deleteEvent} from "@/api/ApiEventController";
import {getEquipmentPage, modifyEquipment, deleteEquipment} from "@/api/ApiEquipmentController";

const equipmentCategoryLabels: Record<string, string> = {
  CAMERA: "Aparat",
  BATTERY: "Bateria",
  LENS: "Obiektyw",
  STUDIO: "Studio",
  LIGHTNING: "Oświetlenie",
  TRIPOD: "Statyw",
  ACCESSORIES: "Akcesoria",
};

const eventStatusLabels: Record<string, string> = {
  PLANNED: "Zaplanowane",
  COMPLETED: "Zakończone",
  CANCELLED: "Anulowane",
};

function ModeratrionPage() {
  // ── Data state ──
  const [users, setUsers] = useState<User[]>([]);
  const [equipment, setEquipment] = useState<EquipmentItem[]>([]);
  const [loading, setLoading] = useState({ users: true, equipment: true });
  const [error, setError] = useState<{ users: string | null; equipment: string | null }>({
    users: null,
    equipment: null,
  });
  const [eventRefreshKey, setEventRefreshKey] = useState(0);

  // ── Dialog state: which item is being edited ──
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deletingUser, setDeletingUser] = useState<User | null>(null);
  const [editingEquipment, setEditingEquipment] = useState<EquipmentItem | null>(null);
  const [deletingEquipment, setDeletingEquipment] = useState<EquipmentItem | null>(null);
  const [editingEvent, setEditingEvent] = useState<EventItem | null>(null);
  const [deletingEvent, setDeletingEvent] = useState<EventItem | null>(null);

  // ── Form state for user edit ──
  const [userName, setUserName] = useState("");
  const [userSurname, setUserSurname] = useState("");
  const [userPhone, setUserPhone] = useState("");
  const [userRole, setUserRole] = useState("");
  const [userActiveMember, setUserActiveMember] = useState("true");

  // ── Form state for equipment edit ──
  const [eqName, setEqName] = useState("");
  const [eqCategory, setEqCategory] = useState("");
  const [eqActiveMembers, setEqActiveMembers] = useState("false");
  const [eqStatutoryEvent, setEqStatutoryEvent] = useState("false");

  // ── Form state for event edit ──
  const [evName, setEvName] = useState("");
  const [evDescription, setEvDescription] = useState("");
  const [evDate, setEvDate] = useState("");
  const [evTime, setEvTime] = useState("");
  const [evLocation, setEvLocation] = useState("");
  const [evPeople, setEvPeople] = useState(0);
  const [evType, setEvType] = useState("");

  // Populate form when editing item changes
  useEffect(() => {
    if (editingUser) {
      setUserName(editingUser.name);
      setUserSurname(editingUser.surname);
      setUserPhone(editingUser.phoneNumber);
      setUserRole(editingUser.role);
      setUserActiveMember(String(editingUser.activeMember));
    }
  }, [editingUser]);

  useEffect(() => {
    if (editingEquipment) {
      setEqName(editingEquipment.name);
      setEqCategory(editingEquipment.equipmentCategory);
      setEqActiveMembers(String(editingEquipment.activeMembers));
      setEqStatutoryEvent(String(editingEquipment.statutoryEvent));
    }
  }, [editingEquipment]);

  useEffect(() => {
    if (editingEvent) {
      setEvName(editingEvent.name);
      setEvDescription(editingEvent.description);
      setEvDate(editingEvent.date);
      setEvTime(editingEvent.time);
      setEvLocation(editingEvent.location);
      setEvPeople(editingEvent.numberOfPeopleRequired);
      setEvType(editingEvent.type);
    }
  }, [editingEvent]);

  // ── Fetch users ──
  useEffect(() => {
    (async () => {
      try {
        const data = await getAllUsers();
        setUsers(data);
      } catch (err) {
        console.error("Failed to fetch users:", err);
        setError((prev) => ({ ...prev, users: "Nie udało się załadować użytkowników" }));
      } finally {
        setLoading((prev) => ({ ...prev, users: false }));
      }
    })();
  }, []);

  // ── Fetch equipment ──
  useEffect(() => {
    (async () => {
      try {
        const page = await getEquipmentPage(false, false, "", "ALL", 0, 1000);
        setEquipment(page.content ?? []);
      } catch (err) {
        console.error("Failed to fetch equipment:", err);
        setError((prev) => ({ ...prev, equipment: "Nie udało się załadować sprzętu" }));
      } finally {
        setLoading((prev) => ({ ...prev, equipment: false }));
      }
    })();
  }, []);

  // ── Handlers ──
  async function handleSaveUser() {
    if (!editingUser) return;
    try {
      const { id } = editingUser;
      await adminUpdateUser(id, {
        name: userName,
        surname: userSurname,
        phoneNumber: userPhone,
        role: userRole,
        activeMember: userActiveMember === "true",
      });
      setUsers((prev) =>
        prev.map((u) =>
          u.id === id
            ? { ...u, name: userName, surname: userSurname, phoneNumber: userPhone, role: userRole, activeMember: userActiveMember === "true" }
            : u,
        ),
      );
      setEditingUser(null);
    } catch (err) {
      console.error("Failed to edit user:", err);
    }
  }

  async function handleDeleteUser() {
    if (!deletingUser) return;
    try {
      await deleteUser(deletingUser.id);
      setUsers((prev) => prev.filter((u) => u.id !== deletingUser.id));
      setDeletingUser(null);
    } catch (err) {
      console.error("Failed to delete user:", err);
    }
  }

  async function handleSaveEquipment() {
    if (!editingEquipment) return;
    try {
      const { id } = editingEquipment;
      await modifyEquipment(String(id), eqName, eqActiveMembers === "true", eqStatutoryEvent === "true", editingEquipment.equipmentCategory);
      setEquipment((prev) =>
        prev.map((e) =>
          e.id === id
            ? { ...e, name: eqName, activeMembers: eqActiveMembers === "true", statutoryEvent: eqStatutoryEvent === "true" }
            : e,
        ),
      );
      setEditingEquipment(null);
    } catch (err) {
      console.error("Failed to edit equipment:", err);
    }
  }

  async function handleDeleteEquipment() {
    if (!deletingEquipment) return;
    try {
      await deleteEquipment(String(deletingEquipment.id));
      setEquipment((prev) => prev.filter((e) => e.id !== deletingEquipment.id));
      setDeletingEquipment(null);
    } catch (err) {
      console.error("Failed to delete equipment:", err);
    }
  }

  async function handleSaveEvent() {
    if (!editingEvent) return;
    try {
      await updateEvent(editingEvent.id, {
        name: evName,
        description: evDescription,
        date: evDate,
        time: evTime,
        location: evLocation,
        numberOfPeopleRequired: evPeople,
        type: evType,
      });
      setEventRefreshKey((k) => k + 1);
      setEditingEvent(null);
    } catch (err) {
      console.error("Failed to edit event:", err);
    }
  }

  async function handleDeleteEvent() {
    if (!deletingEvent) return;
    try {
      await deleteEvent(deletingEvent.id);
      setEventRefreshKey((k) => k + 1);
      setDeletingEvent(null);
    } catch (err) {
      console.error("Failed to delete event:", err);
    }
  }

  // ── Column overrides (stable via useMemo) ──
  const userCols = useMemo(() =>
    userColumns.map((col) => {
      if (col.id === "actions") {
        return {
          ...col,
          cell: ({ row }: any) => (
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => setEditingUser(row.original)}>
                Edytuj
              </Button>
              <Button variant="destructive" size="sm" onClick={() => setDeletingUser(row.original)}>
                Usuń
              </Button>
            </div>
          ),
        };
      }
      return col;
    }), [],
  );

  const equipCols = useMemo(() =>
    equipmentColumns.map((col) => {
      if (col.id === "actions") {
        return {
          ...col,
          cell: ({ row }: any) => (
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => setEditingEquipment(row.original)}>
                Edytuj
              </Button>
              <Button variant="destructive" size="sm" onClick={() => setDeletingEquipment(row.original)}>
                Usuń
              </Button>
            </div>
          ),
        };
      }
      return col;
    }), [],
  );

  const evtCols = useMemo(() =>
    eventColumns.map((col) => {
      if (col.id === "actions") {
        return {
          ...col,
          cell: ({ row }: any) => {
            const ev: EventItem = row.original;
            return (
              <div className="flex gap-2">
                {ev.status === "PLANNED" && (
                  <Button variant="outline" size="sm" onClick={() => setEditingEvent(ev)}>
                    Edytuj
                  </Button>
                )}
                {ev.status !== "COMPLETED" && ev.status !== "CANCELLED" && (
                  <Button variant="destructive" size="sm" onClick={() => setDeletingEvent(ev)}>
                    Usuń
                  </Button>
                )}
              </div>
            );
          },
        };
      }
      return col;
    }), [],
  );

  return (
    <>
    <div>
      <div className="p-5 flex items-center justify-center md:p-20 overflow-hidden ">
        <Tabs defaultValue="users" className="h-full w-full">
          <TabsList className="flex items-center">
            <TabsTrigger value="users">Użytkownicy</TabsTrigger>
            <TabsTrigger value="equipment">Sprzęt</TabsTrigger>
            <TabsTrigger value="events">Wydarzenia</TabsTrigger>
          </TabsList>

          <TabsContent value="users">
            {loading.users ? (
              <p className="p-4 text-muted-foreground">Ładowanie użytkowników...</p>
            ) : error.users ? (
              <p className="p-4 text-destructive">{error.users}</p>
            ) : (
              <DataTable columns={userCols} data={users} />
            )}
          </TabsContent>

          <TabsContent value="equipment">
            {loading.equipment ? (
              <p className="p-4 text-muted-foreground">Ładowanie wyposażenia…</p>
            ) : error.equipment ? (
              <p className="p-4 text-destructive">{error.equipment}</p>
            ) : (
              <DataTable columns={equipCols} data={equipment} />
            )}
          </TabsContent>

          <TabsContent value="events">
            <ServerEventDataTable columns={evtCols} refreshKey={eventRefreshKey} />
          </TabsContent>
        </Tabs>
      </div>

      {/* ═══════ Dialogs rendered at page level ═══════ */}

      {/* ── User Edit Dialog ── */}
      <Dialog open={!!editingUser} onOpenChange={(open) => { if (!open) setEditingUser(null); }}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Edycja Użytkownika</DialogTitle>
            {editingUser && (
              <DialogDescription>
                Edytowanie {editingUser.name} {editingUser.surname} ({editingUser.username})
              </DialogDescription>
            )}
          </DialogHeader>
          {editingUser && (
            <div className="grid gap-4 py-4 max-h-[60vh] overflow-y-auto">
              <div className="grid gap-2">
                <Label>Imię</Label>
                <Input value={userName} onChange={(e) => setUserName(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Nazwisko</Label>
                <Input value={userSurname} onChange={(e) => setUserSurname(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Email</Label>
                <Input defaultValue={editingUser.email} disabled />
              </div>
              <div className="grid gap-2">
                <Label>Nazwa</Label>
                <Input defaultValue={editingUser.username} disabled />
              </div>
              <div className="grid gap-2">
                <Label>Numer telefonu</Label>
                <Input value={userPhone} onChange={(e) => setUserPhone(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Rola</Label>
                <Select value={userRole} onValueChange={setUserRole}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">Użytkownik</SelectItem>
                    <SelectItem value="MODERATOR">Moderator</SelectItem>
                    <SelectItem value="ADMIN">Administrator</SelectItem>
                    <SelectItem value="SUPER_ADMIN">Super administrator</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="grid gap-2">
                <Label>Aktywny członek</Label>
                <Select value={userActiveMember} onValueChange={setUserActiveMember}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Tak</SelectItem>
                    <SelectItem value="false">Nie</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditingUser(null)}>Anuluj</Button>
            <Button onClick={handleSaveUser}>Zapisz</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* ── Equipment Edit Dialog ── */}
      <Dialog open={!!editingEquipment} onOpenChange={(open) => { if (!open) setEditingEquipment(null); }}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Edycja Sprzętu</DialogTitle>
            <DialogDescription>Edycja szczegółów wyposażenia</DialogDescription>
          </DialogHeader>
          {editingEquipment && (
            <div className="grid gap-3 py-4">
              <Label>Nazwa</Label>
              <Input value={eqName} onChange={(e) => setEqName(e.target.value)} />
              <Label>Kategoria</Label>
              <Select value={eqCategory} onValueChange={setEqCategory} disabled>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {["CAMERA", "BATTERY", "LENS", "STUDIO", "LIGHTNING", "TRIPOD", "ACCESSORIES"].map((c) => (
                    <SelectItem key={c} value={c}>{equipmentCategoryLabels[c] ?? c}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Label>Tylko dla aktywnych użytkowników</Label>
              <Select value={eqActiveMembers} onValueChange={setEqActiveMembers}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="true">Tak</SelectItem>
                  <SelectItem value="false">Nie</SelectItem>
                </SelectContent>
              </Select>
              <Label>Tylko na wydarzenia statutowe</Label>
              <Select value={eqStatutoryEvent} onValueChange={setEqStatutoryEvent}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="true">Tak</SelectItem>
                  <SelectItem value="false">Nie</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditingEquipment(null)}>Anuluj</Button>
            <Button onClick={handleSaveEquipment}>Zapisz</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* ── Equipment Delete Confirmation ── */}
      <AlertDialog open={!!deletingEquipment} onOpenChange={(open) => { if (!open) setDeletingEquipment(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Czy na pewno usunąć sprzęt?</AlertDialogTitle>
            <AlertDialogDescription>
              Ta akcja trwale usunie "{deletingEquipment?.name}".
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setDeletingEquipment(null)}>Anuluj</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteEquipment}>Usuń</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* ── Event Edit Dialog ── */}
      <Dialog open={!!editingEvent} onOpenChange={(open) => { if (!open) setEditingEvent(null); }}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Edycja Wydarzenia</DialogTitle>
            {editingEvent && (
              <DialogDescription>Modyfikowanie "{editingEvent.name}".</DialogDescription>
            )}
          </DialogHeader>
          {editingEvent && (
            <div className="grid gap-4 py-4 max-h-[60vh] overflow-y-auto">
              <div className="grid gap-2">
                <Label>Nazwa</Label>
                <Input value={evName} onChange={(e) => setEvName(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Opis</Label>
                <Input value={evDescription} onChange={(e) => setEvDescription(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Data</Label>
                <Input type="date" value={evDate} onChange={(e) => setEvDate(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Czas</Label>
                <Input type="time" value={evTime} onChange={(e) => setEvTime(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Lokalizacja</Label>
                <Input value={evLocation} onChange={(e) => setEvLocation(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label>Liczba wymaganych osób</Label>
                <Input type="number" min={0} value={evPeople} onChange={(e) => setEvPeople(Number(e.target.value))} />
              </div>
              <div className="grid gap-2">
                <Label>Typ</Label>
                <Select value={evType} onValueChange={setEvType}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {["KWF", "SKN", "WRSS", "URSS", "AGH", "AKRE", "CM", "AKT", "PRIVATE"].map((t) => (
                      <SelectItem key={t} value={t}>{t}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid gap-2">
                <Label>Status</Label>
                <Input value={eventStatusLabels[editingEvent.status] ?? editingEvent.status} disabled />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditingEvent(null)}>Anuluj</Button>
            <Button onClick={handleSaveEvent}>Zapisz</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* ── User Delete Confirmation ── */}
      <AlertDialog open={!!deletingUser} onOpenChange={(open) => { if (!open) setDeletingUser(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Czy na pewno usunąć użytkownika?</AlertDialogTitle>
            <AlertDialogDescription>
              Ta akcja na stałe usunie użytkownika "{deletingUser?.name} {deletingUser?.surname}" ({deletingUser?.username}).
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setDeletingUser(null)}>Anuluj</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteUser}>Usuń</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* ── Event Delete Confirmation ── */}
      <AlertDialog open={!!deletingEvent} onOpenChange={(open) => { if (!open) setDeletingEvent(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Czy na pewno usunąć wydarzenie?</AlertDialogTitle>
            <AlertDialogDescription>
              Ta akcja anuluje wydarzenie "{deletingEvent?.name}".Wszyscy zapisani użytkownicy zostaną powiadomieni.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setDeletingEvent(null)}>Anuluj</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteEvent}>Usuń</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
    </>
  );
}

export default ModeratrionPage;
