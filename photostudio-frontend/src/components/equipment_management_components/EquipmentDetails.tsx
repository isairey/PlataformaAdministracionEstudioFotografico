import { useEffect, useState } from "react";
import type { Equipment } from "@/api/api.types"
import { type EquipmentCategory, equipmentCategoriesLabels } from "@/lib/constants";
import { toast } from "sonner";
import { baseColors } from "@/styles/common-styles";
import {getEquipmentById, deleteEquipment, modifyEquipment} from "@/api/ApiEquipmentController";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import camera from "@/assets/camera.png";
import battery from "@/assets/battery.png";
import lens from "@/assets/lens.png";
import studio from "@/assets/studio.png";
import lighting from "@/assets/lighting.png";
import tripod from "@/assets/tripod.png";
import accessories from "@/assets/accessories.png";
import warning from "@/assets/warning.png";
import { Button } from "@/components/ui/button";

interface Props {
    id: string;
    isOpen: boolean;
    onClose: () => void;
    onSaved?: () => void;
}

const getPicture = (category: EquipmentCategory): string => {
  switch (category) {
    case "CAMERA":    return camera;
    case "BATTERY":   return battery;
    case "LENS":      return lens;
    case "STUDIO":    return studio;
    case "LIGHTING": return lighting;
    case "TRIPOD":    return tripod;
    case "ACCESSORIES": return accessories;
    default:          return warning;
  }
};

export default function EquipmentDetails({ id, isOpen, onClose, onSaved }: Props) {
  const [equipment, setEquipment] = useState<Equipment | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editActive, setEditActive] = useState(false);
  const [editStatutory, setEditStatutory] = useState(false);
  const [editCategory, setEditCategory] = useState<EquipmentCategory>("CAMERA");
  const [editName, setEditName] = useState("");

  useEffect(() => {
    if (isOpen) {
      setEquipment(null);
      setIsEditing(false);
      loadEquipment(id);
    }
  }, [isOpen, id]);

  const loadEquipment = async (equipmentId: string) => {
    setIsLoading(true);
    try {
      const data = await getEquipmentById(equipmentId);
      setEquipment(data);
      setEditActive(data.activeMembers);
      setEditStatutory(data.statutoryEvent);
      setEditCategory(data.equipmentCategory);
      setEditName(data.name);
    } catch (error) {
      console.error("Error loading equipment details:", error);
      setEquipment(null);
      toast.error("Nie można załadować szczegółów sprzętu.", {
        style: { color: baseColors.failureColor },
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = () => {
    if (equipment) {
      setEditName(equipment.name);
      setEditActive(equipment.activeMembers);
      setEditStatutory(equipment.statutoryEvent);
      setEditCategory(equipment.equipmentCategory);
    }
    setIsEditing(true);
  };

  const handleCancel = () => {
    setEditName(equipment?.name ?? "");
    setEditActive(equipment?.activeMembers ?? false);
    setEditStatutory(equipment?.statutoryEvent ?? false);
    setEditCategory(equipment?.equipmentCategory ?? "CAMERA");
    setIsEditing(false);
  };

  const handleRemoveEquipment = () => {
      toast(`Czy na pewno chcesz usunąć ${equipment?.name}?`, {
        duration: 10000,
        action: {
          label: "Usuń",
          onClick: () => {
            void deleteEquipment(id);
            onClose();
            onSaved?.();
            toast.success("Pomyślnie usunięto sprzęt.");
          },
        },
        cancel: {
          label: "Anuluj",
          onClick: () => {},
        },
      });
    }

  const handleSave = () => {
    modifyEquipment(id, editName, editActive, editStatutory, editCategory).then(() => {
        loadEquipment(id);
        onSaved?.();
        toast.success("Pomyślnie zapisano zmiany.");
    }).catch((error) => {
        const backendMsg = error?.response?.data?.message ?? error?.response?.data?.fields
          ? JSON.stringify(error?.response?.data)
          : null;
        console.error("Error saving equipment details:", error?.response?.data ?? error);
        toast.error(backendMsg ?? "Nie można zapisać zmian. Spróbuj ponownie później.", {
            style: { color: baseColors.failureColor },
        });
    })
      
    console.log("save", { editActive, editStatutory, editCategory });
    setIsEditing(false);
  };

  return (
    <Dialog open={isOpen} modal={false} onOpenChange={(open) => { if (!open) onClose(); }}>
      <DialogContent className="sm:max-w-sm" aria-describedby={undefined}>
        <DialogHeader>
          <DialogTitle>Szczegóły sprzętu</DialogTitle>
        </DialogHeader>

        {isLoading && (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500" />
          </div>
        )}

        {!isLoading && equipment && (
          <div className="flex flex-col items-center gap-4 py-2">
            <img
              src={getPicture(equipment.equipmentCategory)}
              alt={equipment.name}
              className="w-24 h-24 object-contain"
            />

            <div className="w-full">
              <label className="block text-xs font-medium text-gray-500 mb-1">Nazwa</label>
              <input
                type="text"
                value={isEditing ? editName : equipment.name}
                disabled={!isEditing}
                onChange={(e) => setEditName(e.target.value)}
                className="w-full px-3 py-2 text-sm border border-gray-200 rounded-md bg-gray-100 text-gray-700"
              />
            </div>

            <div className="w-full">
              <label className="block text-xs font-medium text-gray-500 mb-1">Kategoria</label>
              <select
                value={editCategory}
                disabled
                onChange={(e) => setEditCategory(e.target.value as EquipmentCategory)}
                className="w-full px-3 py-2 text-sm border border-gray-200 rounded-md bg-gray-100 text-gray-700 appearance-none"
              >
                {(Object.keys(equipmentCategoriesLabels) as EquipmentCategory[]).map((cat) => (
                  <option key={cat} value={cat}>{equipmentCategoriesLabels[cat]}</option>
                ))}
              </select>
            </div>

            {isEditing ? (
              <div className="w-full flex flex-col gap-2">
                <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={editActive}
                    onChange={(e) => setEditActive(e.target.checked)}
                    className="h-4 w-4"
                  />
                  Tylko dla aktywnych członków
                </label>
                <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={editStatutory}
                    onChange={(e) => setEditStatutory(e.target.checked)}
                    className="h-4 w-4"
                  />
                  Tylko dla wydarzeń statutowych
                </label>
              </div>
            ) : (
              <div className="flex gap-2 flex-wrap justify-center">
                {equipment.activeMembers && (
                  <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-300">
                    Tylko aktywni członkowie
                  </Badge>
                )}
                {equipment.statutoryEvent && (
                  <Badge variant="outline" className="bg-purple-50 text-purple-700 border-purple-300">
                    Tylko wydarzenia statutowe
                  </Badge>
                )}
                {!equipment.activeMembers && !equipment.statutoryEvent && (
                  <Badge variant="outline" className="bg-green-50 text-green-700 border-green-300">
                    Dostępny dla wszystkich
                  </Badge>
                )}
              </div>
            )}

            <div className="w-full flex gap-2 pt-2">
              {isEditing ? (
                <>
                  <button
                    onClick={handleSave}
                    className="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors"
                  >
                    Zapisz
                  </button>
                  <button
                    onClick={handleCancel}
                    className="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
                  >
                    Anuluj
                  </button>
                </>
              ) : (
                <button
                  onClick={handleEdit}
                  className="w-full px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors"
                >
                  Edytuj
                </button>
              )}
            </div>
            {isEditing ? (
                <Button
                    className="w-full px-4 py-2 text-sm font-medium text-white bg-red-400 hover:bg-red-600 cursor-pointer border-transparent"
                    onClick={() => handleRemoveEquipment()}
                >
                    Usuń
                </Button>
        ) : null}
          </div>
        )}

        {!isLoading && !equipment && (
          <div className="flex flex-col items-center gap-2 py-8 text-gray-400">
            <img src={warning} alt="brak danych" className="w-12 h-12 object-contain opacity-50" />
            <p className="text-sm">Nie udało się załadować danych sprzętu.</p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
