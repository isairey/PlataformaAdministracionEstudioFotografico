import * as ApiEquipmentReservationController from "@/api/ApiEquipmentReservationController";
import { useEffect, useState } from "react";
import { EquipmentCard} from "../equipmentCard";
import type { Equipment } from "@/api/api.types";
// import { set } from "date-fns";
import { equipmentCategoriesLabels } from "@/lib/constants";

interface Props {
    startDate: string;
    endDate: string;
    selectedEquipmentIds: string[];
    statutory: boolean;
    eventChosen: boolean;
    onSelectionChange: (ids: string[]) => void;
}

export default function AvailableEquipmentList({ startDate, endDate, selectedEquipmentIds, statutory, eventChosen, onSelectionChange }: Props) {
    const [availableEquipment, setAvailableEquipment] = useState<Equipment[] | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>("");
    const [filteredEquipment, setFilteredEquipment] = useState<Equipment[]>([]);

    const fetchAvailableEquipment = async (start: string, end: string, statutory: boolean) => {
        setAvailableEquipment([]);

        try {
            const response = await ApiEquipmentReservationController.getAvailableEquipmentForTimeWindow(start, end, statutory);
            setAvailableEquipment(response);
        } catch (error) {
            console.error("Error fetching available equipment:", error);
        }
    }

    useEffect(() => {
        if (startDate && endDate && eventChosen) {
            fetchAvailableEquipment(startDate, endDate, statutory);
        }
    }, [startDate, endDate, statutory, eventChosen]);

    useEffect(() => {
        if (availableEquipment) {
            const filtered = availableEquipment.filter(equipment =>
                equipment.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                equipmentCategoriesLabels[equipment.equipmentCategory].toLowerCase().includes(searchTerm.toLowerCase())
            );
            
            const sorted = filtered.sort((a, b) => {
                const aSelected = selectedEquipmentIds.includes(a.id) ? 0 : 1;
                const bSelected = selectedEquipmentIds.includes(b.id) ? 0 : 1;
                return aSelected - bSelected;
            });
            
            setFilteredEquipment(sorted);
        }
    }, [searchTerm, availableEquipment, selectedEquipmentIds]);

    const handleEquipmentClick = (equipmentId: string) => {
        if (selectedEquipmentIds.includes(equipmentId)) {
            // Remove from selection
            onSelectionChange(selectedEquipmentIds.filter(id => id !== equipmentId));
        } else {
            // Add to selection
            onSelectionChange([...selectedEquipmentIds, equipmentId]);
        }
    }

    return (
        <div className="flex flex-col gap-4 p-6 max-w-4xl mx-auto">
            <input
                type="text"
                placeholder="Wyszukaj po nazwie lub kategorii sprzętu..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="px-3 py-2 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            {!startDate || !endDate || !eventChosen ? (
                <p className="text-sm text-muted-foreground">Wybierz daty rozpoczęcia, zakończenia oraz, aby zobaczyć dostępny sprzęt.</p>
            ) : filteredEquipment.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pr-2">
                    {filteredEquipment.map((equipment) => (
                        <div
                            key={equipment.id}
                            onClick={() => handleEquipmentClick(equipment.id)}
                            className={`cursor-pointer rounded-lg transition-all ${
                                selectedEquipmentIds.includes(equipment.id)
                                    ? 'ring-4 ring-black-500 bg-black-50'
                                    : 'hover:ring-2 hover:ring-gray-300'
                            }`}
                        >
                            <EquipmentCard equipment={equipment} />
                        </div>
                    ))}
                </div>
            ) : (
                <p>No equipment found.</p>
            )}
        </div>
    )
}