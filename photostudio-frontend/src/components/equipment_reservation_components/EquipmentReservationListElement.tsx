import { reservationStatusColors, reservationStatusLabels } from '@/lib/constants';
import type { EquipmentReservation } from '@/pages/EquipmentReservationPage';
import { baseColors, cardStyles } from '@/styles/common-styles';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Calendar, User } from 'lucide-react';
import * as ApiEquipmentReservationController from '@/api/ApiEquipmentReservationController';
import { toast } from 'sonner';
import type { ReservationStatus } from '@/lib/constants';

interface props {
    reservation: EquipmentReservation;
    selected?: boolean;
    onClick?: () => void;
    isModerator?: boolean;
    isOwner?: boolean;
  onReservationStatusChange?: (reservationId: string, newStatus: ReservationStatus) => void;
}

export default function equipmentReservationListElement({reservation, onClick, isModerator = false, isOwner = false, onReservationStatusChange}: props) {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
  };

  const cancelReservation = async () => {
    try {
      await ApiEquipmentReservationController.cancelEquipmentReservation(reservation.id);
      onReservationStatusChange?.(reservation.id, 'CANCELLED');
    }
    catch (error) {
      console.error('Error cancelling reservation:', error);
      toast.error('Nie można anulować rezerwacji. Spróbuj ponownie później.',
        { style: { color: baseColors.failureColor} }
      );
    }
  }

  const handleRemove = () => {
    toast('Czy na pewno chcesz usunąć tę rezerwację?', {
      duration: 10000,
      action: {
        label: 'Usuń',
        onClick: () => {
          void cancelReservation();
        },
      },
      cancel: {
        label: 'Anuluj',
        onClick: () => {},
      },
    });
  }

  return (
    <Card 
      className={`${cardStyles.container} cursor-pointer transition-shadow hover:shadow-md hover:bg-gray-50`}
      onClick={onClick}
    >
      <div className={cardStyles.contentWrapper}>
        <div className={cardStyles.headerRow}>
          <h3 className={cardStyles.headerTitle}>
            {reservation.eventName || 'Rezerwacja prywatna'}
          </h3>
          <div className="flex items-center gap-2">
            <Badge className={reservationStatusColors[reservation.status]}>
              {reservationStatusLabels[reservation.status]}
            </Badge>
            {isOwner && <Badge
              className="bg-red-500 text-white hover:bg-red-600 cursor-pointer border-transparent"
              onClick={(e) => { e.stopPropagation(); handleRemove(); }}
              hidden={reservation.status === 'CANCELLED'}
            >
              Usuń
            </Badge>}
          </div>
        </div>

        <div className={cardStyles.textRow}>
          <User className={cardStyles.icon} />
          <span className={cardStyles.containerSubText}>Twórca:</span>
          <span>{reservation.creatorFullName}</span>
        </div>

        <div className={cardStyles.textRow}>
          <Calendar className={cardStyles.icon} />
          <span>{formatDate(reservation.start)} - {formatDate(reservation.end)}</span>
        </div>

        {isModerator && (
          <div className="flex items-center gap-2 mt-2">
            <span className="text-xs text-gray-500">Rozpatrzenie rezerwacji dostępne w szczegółach</span>
          </div>
        )}
      </div>
    </Card>
  )
}