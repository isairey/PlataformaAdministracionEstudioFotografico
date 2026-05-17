import { useState, useEffect } from 'react';
import type { EquipmentReservation } from '@/pages/EquipmentReservationPage';
import type { ReservationStatus } from '@/lib/constants';
import { reservationStatusLabels } from '@/lib/constants';
import EquipmentReservationListElement from './EquipmentReservationListElement';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Calendar } from '@/components/ui/calendar';
import { Button } from '@/components/ui/button';
import { Calendar as CalendarIcon } from 'lucide-react';
import { format } from 'date-fns';
import { formatDateToInput } from '@/lib/utils';
import * as ApiEquipmentReservationController from '@/api/ApiEquipmentReservationController';
import { isAxiosError } from 'axios';

const PAGE_SIZE_OPTIONS = [5, 10, 20, 50];

interface Props {
  onSelect?: (reservation: EquipmentReservation) => void;
  isModerator?: boolean;
  isOwner?: boolean;
  userId?: string;
  onReservationStatusChange?: (reservationId: string, newStatus: ReservationStatus) => void;
  refreshKey?: number;
}

export default function EquipmentReservationList({ onSelect, isModerator = false, isOwner = false, userId, onReservationStatusChange, refreshKey = 0 }: Props) {
  const [reservations, setReservations] = useState<EquipmentReservation[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageNo, setPageNo] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(false);

  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [searchUserName, setSearchUserName] = useState('');
  const [searchEventName, setSearchEventName] = useState('');
  const [filterStatus, setFilterStatus] = useState<ReservationStatus | ''>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [isFilterExpanded, setIsFilterExpanded] = useState(false);

  // Debounced values for text inputs
  const [debouncedUserName, setDebouncedUserName] = useState('');
  const [debouncedEventName, setDebouncedEventName] = useState('');

  useEffect(() => {
    const t = setTimeout(() => setDebouncedUserName(searchUserName), 400);
    return () => clearTimeout(t);
  }, [searchUserName]);

  useEffect(() => {
    const t = setTimeout(() => setDebouncedEventName(searchEventName), 400);
    return () => clearTimeout(t);
  }, [searchEventName]);

  // Reset to first page whenever filters change
  useEffect(() => {
    setPageNo(0);
  }, [debouncedUserName, debouncedEventName, filterStatus, dateFrom, dateTo, pageSize]);

  // Fetch whenever page or debounced filters change
  useEffect(() => {
    const fetchPage = async () => {
      setLoading(true);
      try {
        let result;
        if (isOwner && userId) {
          result = await ApiEquipmentReservationController.getPageByFiltersForUser(
            userId,
            debouncedUserName,
            debouncedEventName,
            filterStatus,
            dateFrom,
            dateTo,
            pageNo,
            pageSize
          );
        } else {
          result = await ApiEquipmentReservationController.getPageByFilters(
            debouncedUserName,
            debouncedEventName,
            filterStatus,
            dateFrom,
            dateTo,
            pageNo,
            pageSize
          );
        }
        setReservations(result.content);
        setTotalPages(result.totalPages);
        setTotalElements(result.totalElements);
      } catch (error) {
        if (isAxiosError(error)) {
          console.error(
            `[EquipmentReservationList] HTTP ${error.response?.status ?? 'N/A'} – ${error.config?.method?.toUpperCase()} ${error.config?.url}`,
            '\nResponse:', error.response?.data ?? '(no response body)',
            '\nFull error:', error
          );
        } else {
          console.error('[EquipmentReservationList] Unexpected error:', error);
        }
      } finally {
        setLoading(false);
      }
    };
    fetchPage();
  }, [pageNo, pageSize, debouncedUserName, debouncedEventName, filterStatus, dateFrom, dateTo, refreshKey, isOwner, userId]);

  const handleStatusChange = (reservationId: string, newStatus: ReservationStatus) => {
    setReservations(prev =>
      prev.map(r => r.id === reservationId ? { ...r, status: newStatus } : r)
    );
    onReservationStatusChange?.(reservationId, newStatus);
  };

  const allReservationStatuses: Array<ReservationStatus> = ['NOT_RESOLVED', 'RESOLVED', 'CANCELLED'];

  return (
    <div className="flex flex-col gap-4 h-full">
      {/* Filter panel */}
      <div className="bg-gray-50 border border-gray-200 rounded-lg">
        <button
          onClick={() => setIsFilterExpanded(!isFilterExpanded)}
          className="w-full p-2 flex items-center justify-between hover:bg-gray-100 transition-colors"
        >
          <span className="text-sm font-medium text-gray-700">Filtry rezerwacji</span>
          <span className={`text-xl text-gray-600 transition-transform ${isFilterExpanded ? 'rotate-180' : ''}`}>
            ▼
          </span>
        </button>

        {isFilterExpanded && (
          <div className="p-2 border-t border-gray-200 space-y-2">
            {isModerator && (
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                  Filtruj po nazwie użytkownika
                </label>
                <input
                  type="text"
                  placeholder="Wpisz imię i nazwisko..."
                  value={searchUserName}
                  onChange={(e) => setSearchUserName(e.target.value)}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>
            )}

            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">
                Filtruj po nazwie wydarzenia
              </label>
              <input
                type="text"
                placeholder="Wpisz nazwę wydarzenia..."
                value={searchEventName}
                onChange={(e) => setSearchEventName(e.target.value)}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">
                Status rezerwacji
              </label>
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value as ReservationStatus | '')}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value="">Wszystkie statusy</option>
                {allReservationStatuses.map((status) => (
                  <option key={status} value={status}>
                    {reservationStatusLabels[status]}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-1.5">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Od daty</label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" className="w-full px-2 py-1 border-gray-300 justify-between h-auto font-normal text-gray-700">
                      {dateFrom ? format(new Date(dateFrom), "d MMM yyyy") : "Wybierz datę"}
                      <CalendarIcon className="h-4 w-4 opacity-50" />
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={dateFrom ? new Date(dateFrom) : undefined}
                      onSelect={(d) => setDateFrom(d ? formatDateToInput(d) : '')}
                    />
                  </PopoverContent>
                </Popover>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Do daty</label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" className="w-full px-2 py-1 border-gray-300 justify-between h-auto font-normal text-gray-700">
                      {dateTo ? format(new Date(dateTo), "d MMM yyyy") : "Wybierz datę"}
                      <CalendarIcon className="h-4 w-4 opacity-50" />
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={dateTo ? new Date(dateTo) : undefined}
                      onSelect={(d) => setDateTo(d ? formatDateToInput(d) : '')}
                    />
                  </PopoverContent>
                </Popover>
              </div>
            </div>

            {/* Results count */}
            <div className="text-xs text-gray-600 pt-1 border-t border-gray-100">
              Wyświetlono {reservations.length} z {totalElements} rezerwacji
            </div>
          </div>
        )}
      </div>
      {/* Pagination controls */}
      <div className="flex items-center justify-between gap-2 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2">
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-600">Na stronie:</span>
          <select
            value={pageSize}
            onChange={(e) => { setPageSize(Number(e.target.value)); setPageNo(0); }}
            className="px-2 py-1 text-xs border border-gray-300 rounded shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            {PAGE_SIZE_OPTIONS.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>
        <div className="flex items-center gap-1">
          <button
            disabled={pageNo === 0 || loading}
            onClick={() => setPageNo(0)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            «
          </button>
          <button
            disabled={pageNo === 0 || loading}
            onClick={() => setPageNo((p) => p - 1)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            ‹
          </button>
          <span className="px-2 text-xs text-gray-600">
            {totalPages === 0 ? '0 / 0' : `${pageNo + 1} / ${totalPages}`}
          </span>
          <button
            disabled={pageNo >= totalPages - 1 || loading}
            onClick={() => setPageNo((p) => p + 1)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            ›
          </button>
          <button
            disabled={pageNo >= totalPages - 1 || loading}
            onClick={() => setPageNo(totalPages - 1)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            »
          </button>
        </div>
      </div>
      {/* Reservation list */}
      <div className="flex-1 space-y-2">
        {loading ? (
          <div className="text-center py-8 text-gray-500">Ładowanie...</div>
        ) : reservations.length > 0 ? (
          reservations.map((reservation) => (
            <EquipmentReservationListElement
              key={reservation.id}
              reservation={reservation}
              selected={selectedId === reservation.id}
              onClick={() => {
                setSelectedId(reservation.id);
                onSelect?.(reservation);
              }}
              isModerator={isModerator}
              isOwner={isOwner}
              onReservationStatusChange={handleStatusChange}
            />
          ))
        ) : (
          <div className="text-center py-8 text-gray-500">
            Nie znaleziono rezerwacji spełniających kryteria
          </div>
        )}
      </div>
    </div>
  );
}
