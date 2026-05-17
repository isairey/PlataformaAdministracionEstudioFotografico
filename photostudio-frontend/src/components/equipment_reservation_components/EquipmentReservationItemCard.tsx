"use client";

import camera from "@/assets/camera.png";
import battery from "@/assets/battery.png";
import lens from "@/assets/lens.png";
import studio from "@/assets/studio.png";
import lighting from "@/assets/lighting.png";
import tripod from "@/assets/tripod.png";
import accessories from "@/assets/accessories.png";
import warning from "@/assets/warning.png";
import {
  reservationItemStatusColors,
  reservationItemStatusLabels,
  type EquipmentCategory,
  type ReservationItemStatus,
  equipmentCategoriesLabels,
} from "@/lib/constants";
import { cardStyles } from "@/styles/common-styles";
import type { EquipmentReservationItem } from "@/pages/EquipmentReservationPage";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface EquipmentReservationCardProps {
  equipmentReservationItem: EquipmentReservationItem;
  handleRemove: (item: EquipmentReservationItem) => void;
  handleStatusChange?: (item: EquipmentReservationItem, newStatus: ReservationItemStatus) => void;
  isOwner?: boolean;
  isModerator?: boolean;
}

export function EquipmentReservationItemCard(
  { equipmentReservationItem, handleRemove, handleStatusChange, isOwner = false, isModerator = false }: EquipmentReservationCardProps)
  {
  

  const getPicture = (category: EquipmentCategory): string => {
    switch (category) {
      case "CAMERA":
        return camera;
      case "BATTERY":
        return battery;
      case "LENS":
        return lens;
      case "STUDIO":
        return studio;
      case "LIGHTING":
        return lighting;
      case "TRIPOD":
        return tripod;
      case "ACCESSORIES":
        return accessories;
      default:
        return warning;
    }
  };

  return (
    <Card className="w-full flex flex-col pb-3">
      <div>
        <div>
          <h3 className="flex justify-center text-md font-semibold">{equipmentReservationItem.name}</h3>
        </div>
        <img
          src={getPicture(equipmentReservationItem.equipmentCategory)}
          alt={equipmentReservationItem.name}
          className="w-full h-8 object-contain"
        />
        <div className={`${cardStyles.textRow} justify-center`}>
          <span className={cardStyles.containerSubText}>Kategoria:</span>
          <span>{equipmentCategoriesLabels[equipmentReservationItem.equipmentCategory]}</span>
        </div>
      </div>
      <div className="flex items-center justify-between gap-3 px-4 pt-3 pb-0 border-t">
        <Badge 
          className={reservationItemStatusColors[equipmentReservationItem.status]}
          variant="outline"
        >
          {reservationItemStatusLabels[equipmentReservationItem.status]}
        </Badge>

        {isModerator ? (
          <select
            className="px-2 py-1 border rounded text-xs"
            value={equipmentReservationItem.status}
            onChange={(event) =>
              handleStatusChange?.(
                equipmentReservationItem,
                event.target.value as ReservationItemStatus
              )
            }
          >
            <option value="PENDING">Oczekujący</option>
            <option value="APPROVED">Zaakceptowany</option>
            <option value="REJECTED">Odrzucony</option>
          </select>
        ) : null}

        {isOwner ? (
          <Badge
            className="bg-red-500 text-white hover:bg-red-600 cursor-pointer border-transparent"
            onClick={() => handleRemove(equipmentReservationItem)}
          >
            Usuń
          </Badge>
        ) : null}
      </div>
    </Card>
  );
}