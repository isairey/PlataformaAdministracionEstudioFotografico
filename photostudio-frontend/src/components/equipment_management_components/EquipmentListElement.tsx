import camera from "@/assets/camera.png";
import battery from "@/assets/battery.png";
import lens from "@/assets/lens.png";
import studio from "@/assets/studio.png";
import lighting from "@/assets/lighting.png";
import tripod from "@/assets/tripod.png";
import accessories from "@/assets/accessories.png";
import warning from "@/assets/warning.png";
import { type EquipmentCategory, equipmentCategoriesLabels } from "@/lib/constants";
import type { Equipment } from "@/api/api.types";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface EquipmentListElementProps {
  equipment: Equipment;
  selected?: boolean;
  onClick?: () => void;
  isAdmin?: boolean;
  onEdit?: (equipment: Equipment) => void;
  onDelete?: (equipment: Equipment) => void;
}

export default function EquipmentListElement({
  equipment,
  selected,
  onClick,
  isAdmin = false,
  onEdit,
  onDelete,
}: EquipmentListElementProps) {
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
    <Card
      className={`w-full flex flex-col cursor-pointer transition-shadow hover:shadow-md hover:bg-gray-50 ${
        selected ? "ring-2 ring-blue-500" : ""
      }`}
      onClick={onClick}
    >
      <div className="p-2">
        <h3 className="text-center text-xs font-semibold leading-tight truncate">{equipment.name}</h3>
        <img
          src={getPicture(equipment.equipmentCategory)}
          alt={equipment.name}
          className="w-full h-6 object-contain my-1"
        />
        <div className="flex justify-center gap-1 flex-wrap">
          <span className="text-[10px] text-gray-500">{equipmentCategoriesLabels[equipment.equipmentCategory]}</span>
        </div>
        <div className="flex justify-center gap-1 mt-1 flex-wrap">
          {equipment.activeMembers && (
            <Badge variant="outline" className="text-[9px] px-1 py-0 bg-blue-50 text-blue-700 border-blue-300">
              Aktywni
            </Badge>
          )}
          {equipment.statutoryEvent && (
            <Badge variant="outline" className="text-[9px] px-1 py-0 bg-purple-50 text-purple-700 border-purple-300">
              Statutowe
            </Badge>
          )}
        </div>
      </div>

      {isAdmin && (
        <div className="flex items-center justify-end gap-1 px-2 pt-1 pb-1 border-t">
          {onEdit && (
            <Badge
              className="text-[9px] px-1 py-0 bg-blue-500 text-white hover:bg-blue-600 cursor-pointer border-transparent"
              onClick={(e) => {
                e.stopPropagation();
                onEdit(equipment);
              }}
            >
              Edytuj
            </Badge>
          )}
          {onDelete && (
            <Badge
              className="text-[9px] px-1 py-0 bg-red-500 text-white hover:bg-red-600 cursor-pointer border-transparent"
              onClick={(e) => {
                e.stopPropagation();
                onDelete(equipment);
              }}
            >
              Usuń
            </Badge>
          )}
        </div>
      )}
    </Card>
  );
}
