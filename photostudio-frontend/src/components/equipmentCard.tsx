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
  type EquipmentCategory,
  equipmentCategoriesLabels,
} from "@/lib/constants";
import { cardStyles } from "@/styles/common-styles";
import { Card } from "@/components/ui/card";
import { type Equipment } from "@/api/api.types";

interface EquipmentCardProps {
  equipment: Equipment;
}

export function EquipmentCard({ equipment }: EquipmentCardProps) {
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
    <Card className={`${cardStyles.container} p-1`}>
      <div className={cardStyles.contentWrapper}>
        <div className={`${cardStyles.headerRow} justify-center`}>
          <h3 className={cardStyles.headerTitle}>{equipment.name}</h3>
        </div>
        <img
          src={getPicture(equipment.equipmentCategory)}
          alt={equipment.name}
          className="w-full h-8 object-contain"
        />

        <div className={`${cardStyles.textRow} justify-center`}>
          <span className={cardStyles.containerSubText}>
            Kategoria:
          </span>
          <span>{equipmentCategoriesLabels[equipment.equipmentCategory]}</span>
        </div>
      </div>
    </Card>
  );
}
