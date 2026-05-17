import type { EquipmentReservation, EquipmentReservationItem } from "@/pages/EquipmentReservationPage";
import { useEffect, useState } from "react";
import axios from "axios";
import { reservationStatusLabels, type ReservationItemStatus } from "@/lib/constants";
import { EquipmentReservationItemCard } from "./EquipmentReservationItemCard";
import * as ApiEquipmentReservationController from "@/api/ApiEquipmentReservationController";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { cardStyles } from "@/styles/common-styles";
import { Calendar, User, Clock, CheckCheck, XCircle, Pencil } from "lucide-react";
import { formatDate } from "@/lib/utils";
import { toast } from "sonner";
import { baseColors } from "@/styles/common-styles";
import { Button } from "@/components/ui/button";

interface props {
    reservation: EquipmentReservation;
    isOpen: boolean;
    isOwner?: boolean;
    isModerator?: boolean;
    onClose: () => void;
    onResolve: () => void;
}

export default function EquipmentReservationDetails({ reservation, isOpen, isOwner = false, isModerator = false, onClose, onResolve }: props) {
  const [reservationItems, setReservationItems] = useState<EquipmentReservationItem[] | null>(null);
  const [localStatusMap, setLocalStatusMap] = useState<Record<string, ReservationItemStatus>>({});
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    if (isOpen) {
        loadReservationData(reservation.id);
        setIsEditing(false);
    }
  }, [isOpen, reservation.id]);

  const loadReservationData = async (reservationId: string) => {
    try {      
      const items: EquipmentReservationItem[] = await ApiEquipmentReservationController.getReservationItems(reservationId);
      setReservationItems(items);
      const map: Record<string, ReservationItemStatus> = {};
      items.forEach((item) => { map[item.id] = item.status; });
      setLocalStatusMap(map);
    } catch (error) {
      console.error("Error loading reservation items:", error);
      setReservationItems([]);
      const backendMessage = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined)
        : undefined;
      toast.error(backendMessage || "Nie można załadować szczegółów rezerwacji. Spróbuj ponownie później.",
        { style: { color: baseColors.failureColor} }
      );
    }
  }

  const removeItem = async (item: EquipmentReservationItem) => {
    try {
      await ApiEquipmentReservationController.cancelSingleReservationItem(item.id);
      loadReservationData(reservation.id);
    } catch (error) {
      console.error("Error removing reservation item:", error);
      const backendMessage = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined)
        : undefined;
      toast.error(backendMessage || "Nie można usunąć elementu rezerwacji. Spróbuj ponownie później.",
        { style: { color: baseColors.failureColor} }
      );
    }
  }

  const handleRemoveItem = (item: EquipmentReservationItem) => {
    toast(`Czy na pewno chcesz usunąć ${item.name} z tej rezerwacji?`, {
      duration: 10000,
      action: {
        label: "Usuń",
        onClick: () => {
          void removeItem(item);
        },
      },
      cancel: {
        label: "Anuluj",
        onClick: () => {},
      },
    });
  }

  const handleLocalStatusChange = (
    item: EquipmentReservationItem,
    newStatus: ReservationItemStatus
  ) => {
    if (newStatus === "PENDING" || newStatus === "CANCELLED") return;
    setLocalStatusMap((prev) => ({ ...prev, [item.id]: newStatus }));
  };

  const setAllApproved = () => {
    if (!reservationItems) return;
    setLocalStatusMap((prev) => {
      const updated = { ...prev };
      reservationItems.forEach((item) => {
        if (updated[item.id] !== "CANCELLED") {
          updated[item.id] = "APPROVED";
        }
      });
      return updated;
    });
  };

  const setAllRejected = () => {
    if (!reservationItems) return;
    setLocalStatusMap((prev) => {
      const updated = { ...prev };
      reservationItems.forEach((item) => {
        if (updated[item.id] !== "CANCELLED") {
          updated[item.id] = "REJECTED";
        }
      });
      return updated;
    });
  };

  const canResolve =
    !!reservationItems &&
    reservationItems.length > 0 &&
    reservationItems
      .filter((item) => localStatusMap[item.id] !== "CANCELLED")
      .every((item) => localStatusMap[item.id] !== "PENDING");

  const buildAcceptanceMap = () => {
    const acceptanceMap: Record<string, boolean> = {};
    reservationItems!.forEach((item) => {
      const status = localStatusMap[item.id];
      if (status !== "CANCELLED") {
        acceptanceMap[item.id] = status === "APPROVED";
      }
    });
    return acceptanceMap;
  };

  const handleResolve = async () => {
    if (!reservationItems || !canResolve) return;
    try {
      await ApiEquipmentReservationController.resolveEquipmentReservation(reservation.id, buildAcceptanceMap());
      toast.success("Rezerwacja została rozpatrzona.");
      onResolve();
      onClose();
    } catch (error) {
      console.error("Error resolving reservation:", error);
      const backendMessage = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined)
        : undefined;
      toast.error(backendMessage || "Nie można rozpatrzyć rezerwacji. Spróbuj ponownie później.",
        { style: { color: baseColors.failureColor} }
      );
    }
  };

 const handleSaveEdit = async () => {
    if (!reservationItems || !canResolve) return;
    try {
      await ApiEquipmentReservationController.modifyEquipmentReservation(reservation.id, buildAcceptanceMap());
      toast.success("Rezerwacja została zmodyfikowana.");
      onResolve();
      setIsEditing(false);
      loadReservationData(reservation.id);
        } catch (error) {
      console.error("Error modifying reservation:", error);
      const message = axios.isAxiosError(error)
        ? (error.response?.data as string | undefined)
        : undefined;
      toast.error(message || "Nie można zmodyfikować rezerwacji. Spróbuj ponownie później.",
        { style: { color: baseColors.failureColor} }
      );
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-[400px] md:max-w-[850px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Szczegóły rezerwacji sprzętu</DialogTitle>
        </DialogHeader>

        <div className="grid gap-4 py-4">
             <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-muted/50 p-4 rounded-lg">
                <div className={cardStyles.dialogDetailRow}>
                    <User className="h-4 w-4 text-muted-foreground" />
                    <span className={cardStyles.dialogLabel}>Twórca:</span>
                    <span className="font-medium">{reservation.creatorFullName}</span>
                </div>
                 <div className={cardStyles.dialogDetailRow}>
                     <span className={cardStyles.dialogLabel}>Typ wydarzenia:</span>
                     <span className="font-medium">{reservation.eventName || "Brak (Prywatne)"}</span>
                 </div>
                 <div className={cardStyles.dialogDetailRow}>
                    <Clock className="h-4 w-4 text-muted-foreground" />
                    <span className={cardStyles.dialogLabel}>Status:</span>
                    <span className="font-medium">{reservationStatusLabels[reservation.status]}</span>
                 </div>
                 <div className={cardStyles.dialogDetailRow}>
                    <Calendar className="h-4 w-4 text-muted-foreground" />
                    <span className={cardStyles.dialogLabel}>Termin:</span>
                    <span className="text-sm">
                        {formatDate(reservation.start)} - {formatDate(reservation.end)}
                    </span>
                 </div>
                 <div className="md:col-span-2 flex items-start gap-2">
                  <span className={cardStyles.dialogLabel}>Komentarz:</span>
                  <span className="text-sm break-words">
                    {reservation.comment?.trim() ? reservation.comment : "Brak komentarza"}
                  </span>
                 </div>
                 <div className={cardStyles.dialogDetailRow}>
                     <span className={cardStyles.dialogLabel}>Rozpatrzony przez:</span>
                     <span className="font-medium">{reservation.rewviewerFullName || "Nie rozpatrzony"}</span>
                 </div>
             </div>

            <div className="mt-4">
                {reservationItems ? (
                reservationItems.length > 0 ? (
                    <>
                    {isModerator && reservation.status === 'NOT_RESOLVED' && (
                      <div className="flex gap-2 mb-4">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={setAllApproved}
                          className="flex items-center gap-1 border-green-500 text-green-700 hover:bg-green-50"
                        >
                          <CheckCheck className="h-4 w-4" />
                          Zaakceptuj wszystkie
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={setAllRejected}
                          className="flex items-center gap-1 border-red-500 text-red-700 hover:bg-red-50"
                        >
                          <XCircle className="h-4 w-4" />
                          Odrzuć wszystkie
                        </Button>
                      </div>
                    )}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 justify-items-center">
                        {reservationItems.map((item) => (
                        <EquipmentReservationItemCard 
                          key={item.id}
                            equipmentReservationItem={{ ...item, status: localStatusMap[item.id] ?? item.status }} 
                            handleRemove={handleRemoveItem}
                          handleStatusChange={handleLocalStatusChange}
                            isOwner={isOwner}
                            isModerator={isModerator && (reservation.status === 'NOT_RESOLVED' || isEditing)}
                        />
                        ))}
                    </div>
                    </>
                ) : (
                    <p className="text-muted-foreground py-4 text-center">Brak zarezerwowanego sprzętu.</p>
                )
                ) : (
                <div className="flex justify-center py-8">Ładowanie...</div>
                )}
            </div>

            {isModerator && reservation.status === 'NOT_RESOLVED' && (
              <div className="flex flex-col gap-1 items-end pt-2">
                {!canResolve && reservationItems && reservationItems.length > 0 && (
                  <p className="text-xs text-muted-foreground">
                    Wszystkie elementy muszą mieć status inny niż "oczekujący".
                  </p>
                )}
                <Button
                  onClick={handleResolve}
                  disabled={!canResolve}
                  className="bg-main-site text-white hover:bg-black/90"
                >
                  Rozpatrz rezerwację
                </Button>
              </div>
            )}

            {isModerator && reservation.status === 'RESOLVED' && !isEditing && (
              <div className="flex justify-end pt-2">
                <Button
                  variant="outline"
                  onClick={() => setIsEditing(true)}
                  className="flex items-center gap-1"
                >
                  <Pencil className="h-4 w-4" />
                  Edytuj rezerwację
                </Button>
              </div>
            )}

            {isModerator && reservation.status === 'RESOLVED' && isEditing && (
              <div className="flex flex-col gap-1 items-end pt-2">
                {!canResolve && reservationItems && reservationItems.length > 0 && (
                  <p className="text-xs text-muted-foreground">
                    Wszystkie elementy muszą mieć status inny niż "oczekujący".
                  </p>
                )}
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    onClick={() => {
                      setIsEditing(false);
                      loadReservationData(reservation.id);
                    }}
                  >
                    Anuluj
                  </Button>
                  <Button
                    onClick={handleSaveEdit}
                    disabled={!canResolve}
                    className="bg-main-site text-white hover:bg-black/90"
                  >
                    Zapisz zmiany
                  </Button>
                </div>
              </div>
            )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
