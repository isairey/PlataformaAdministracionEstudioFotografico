"use client";

import EquipmentReservationDetails from '@/components/equipment_reservation_components/EquipmentReservationDetails';
import EquipmentReservationList from '@/components/equipment_reservation_components/EquipmentReservationList';
import { useState } from 'react'
import type { EquipmentReservation } from './EquipmentReservationPage';
import type { ReservationStatus } from '@/lib/constants';

export default function ModeratorEquipmentReservationPage() {
    const [selectedReservation, setSelectedReservation] = useState<EquipmentReservation | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    const handleReservationStatusChange = (reservationId: string, newStatus: ReservationStatus) => {
        setSelectedReservation((prev) =>
            prev?.id === reservationId ? { ...prev, status: newStatus } : prev
        );
    };


    return (
    <div className="flex-1 flex flex-col min-h-0">
        <div className="flex flex-col flex-1 min-h-0 max-w-6xl w-full mx-auto p-6">
            <h1 className="text-2xl font-bold mb-6">Rezerwacje Sprzętu</h1>
            <div className="flex flex-col md:flex-row gap-6 h-full">
                <div className="flex-1">
                    <EquipmentReservationList
                        onSelect={setSelectedReservation}
                        isModerator={true}
                        onReservationStatusChange={handleReservationStatusChange}
                        refreshKey={refreshKey}
                    />
                </div>
                {selectedReservation && (
                    <EquipmentReservationDetails
                        reservation={selectedReservation}
                        isOpen={true}
                        onClose={() => setSelectedReservation(null)}
                        isOwner={false}
                        isModerator={true}
                        onResolve={() => setRefreshKey((k) => k + 1)}
                    />
                )}
            </div>
        </div>
    </div>
    );
}
