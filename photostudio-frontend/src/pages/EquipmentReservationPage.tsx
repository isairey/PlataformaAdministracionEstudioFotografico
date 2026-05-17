"use client";

import { useEffect, useState } from 'react'
import { checkUserStatus } from '@/api/ApiGetMe';
import type { UserRoleType } from '@/api/api.types';
import type { EquipmentCategory, ReservationStatus, ReservationItemStatus } from '@/lib/constants'
import { useNavigate } from 'react-router-dom';
import EquipmentReservationList from '@/components/equipment_reservation_components/EquipmentReservationList';
import EquipmentReservationDetails from '@/components/equipment_reservation_components/EquipmentReservationDetails';
import { Button } from '@/components/ui/button';

export interface userInfo {
  id: number;
  name: string;
  surname: string;
  email: string;
  username: string;
  activeMember: boolean;
  phoneNumber: string;
  role: UserRoleType;
}

export interface EquipmentReservationItem {
    id: string;
    name: string;
    activeMembers: boolean;
    statutoryEvent: boolean;
    equipmentCategory: EquipmentCategory;
    status: ReservationItemStatus;
}

export interface EquipmentReservation {
    id: string;
    status: ReservationStatus;
    creatorFullName: string;
    eventName: string | null;
    comment?: string | null;
    start: string;
    end: string;
    rewviewerFullName?: string | null;
}

export default function EquipmentReservationPage() {

    const [selectedReservation, setSelectedReservation] = useState<EquipmentReservation | null>(null);
    const [userData, setUserData] = useState<userInfo | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        fetchUserData();
    }, []);

    const fetchUserData = async () => {
        try {
            const data = await checkUserStatus();
            setUserData(data);
            localStorage.setItem('userId', data.id.toString());
        } catch (error) {
            console.error('Error fetching user status:', error);
        }
    };

    const navigate = useNavigate();

    const handleReservationStatusChange = (reservationId: string, newStatus: ReservationStatus) => {
        setSelectedReservation((prev) =>
            prev?.id === reservationId ? { ...prev, status: newStatus } : prev
        );
    };

  

    return (
    <div className="flex-1 flex flex-col min-h-0">
        <div className="flex flex-col flex-1 min-h-0 max-w-6xl w-full mx-auto p-6">
            <div className="flex flex-col gap-4 [@media(min-width:700px)]:flex-row [@media(min-width:700px)]:justify-between justify-center items-center [@media(min-width:700px)]:items-start p-4">
                <h1 className="text-2xl font-semibold">Moje Rezerwacje Sprzętu</h1>
                <Button
                    className="w-full max-w-xs bg-main-site text-white hover:bg-black/90 sm"
                    onClick={() => {
                    navigate(`/new-reservation`);
                    }}
                >
                    Dodaj Rezerwację
                </Button>
            </div>
            <div className="flex gap-6 flex-1 overflow-y-auto scrollbar-hide">
                <div className="flex-1">
                    <EquipmentReservationList
                        onSelect={setSelectedReservation}
                        onReservationStatusChange={handleReservationStatusChange}
                        isModerator={false}
                        isOwner={true}
                        userId={userData?.id.toString()}
                        refreshKey={refreshKey}
                    />
                </div>
                {selectedReservation && (
                    <EquipmentReservationDetails
                        reservation={selectedReservation}
                        isOpen={true}
                        onClose={() => setSelectedReservation(null)}
                        isModerator={false}
                        isOwner={true}
                        onResolve={() => setRefreshKey((k) => k + 1)}
                    />
                )}
            </div>
        </div>
    </div>
    );
}