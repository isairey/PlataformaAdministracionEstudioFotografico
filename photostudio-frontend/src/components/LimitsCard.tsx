
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { type EquipmentCategory, equipmentCategoriesLabels } from "@/lib/constants";
import {getUserLimits} from "@/api/ApiLimits";
import {changeUserLimit} from "@/api/ApiLimits";

interface DevPageProps {
  userId: number;
}

const categories = Object.keys(equipmentCategoriesLabels) as EquipmentCategory[];

export default function DevPage({ userId: userId }: DevPageProps) {
  const [limits, setLimits] = useState<Record<EquipmentCategory, string>>({
    CAMERA: "0",
    BATTERY: "0",
    LENS: "0",
    STUDIO: "0",
    LIGHTING: "0",
    TRIPOD: "0",
    ACCESSORIES: "0",
  });
  const [savingCategory, setSavingCategory] = useState<EquipmentCategory | null>(null);


  useEffect(() => {
    getUserLimits(userId)
      .then((data) => {
        setLimits((prev) => {
          const next = { ...prev };
          for (const category of categories) {
            const value = data[category];
            if (typeof value === "number") {
              next[category] = value.toString();
            }
          }
          return next;
        });
      })
      .catch(() => {
        toast.error("Nie udało się pobrać limitów.");
      });
  }, [userId]);


  const handleChange = (category: EquipmentCategory, value: string) => {
    setLimits((prev) => ({ ...prev, [category]: value }));
  };

  const handleSave = async (category: EquipmentCategory) => {

    const parsedValue = Number(limits[category]);
    try {
      setSavingCategory(category);
      await changeUserLimit(userId, category, parsedValue);
      toast.success(`Zapisano limit dla kategorii: ${equipmentCategoriesLabels[category]}.`);
    } catch {
      toast.error("Nie udało się zapisać limitu.");
    } finally {
      setSavingCategory(null);
    }
  };

  return (
    <div className="p-6 max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>Limity rezerwacji sprzętu</CardTitle>
          <CardDescription>Ustaw limity dla każdej kategorii.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {categories.map((category) => (
            <div key={category} className="grid grid-cols-[1fr_120px_auto] gap-2 items-center">
              <span className="text-sm">{equipmentCategoriesLabels[category]}</span>
              <Input
                type="number"
                min={0}
                value={limits[category]}
                onChange={(event) => handleChange(category, event.target.value)}
                className="h-8"
              />
              <Button
                size="sm"
                onClick={() => void handleSave(category)}
                disabled={savingCategory === category}
              >
                {savingCategory === category ? "Zapisywanie..." : "Zapisz"}
              </Button>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
