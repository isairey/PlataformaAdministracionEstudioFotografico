import { useEffect, useRef, useState } from "react";
import type { Equipment } from "@/api/api.types"
import { equipmentCategoriesLabels, type EquipmentCategory } from "@/lib/constants";
import EquipmentListElement from "./EquipmentListElement";
import EquipmentDetails from "./EquipmentDetails";
import {getEquipmentPage} from "@/api/ApiEquipmentController";

const PAGE_SIZE_OPTIONS = [5, 10, 20, 50];

interface Props {
  onSelect?: (id: string) => void;
  isAdmin?: boolean;
  onEdit?: (equipment: Equipment) => void;
  onDelete?: (equipment: Equipment) => void;
}

export default function EquipmentList({ onSelect, isAdmin = false, onEdit, onDelete }: Props) {
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [searchEquipmentName, setSearchEquipmentName] = useState('');
  const [debouncedName, setDebouncedName] = useState('');
  const [filterCategory, setFilterCategory] = useState<EquipmentCategory | 'ALL'>('ALL');
  const [isActiveMembersOnly, setIsActiveMembersOnly] = useState(false);
  const [isStatutoryOnly, setIsStatutoryOnly] = useState(false);
  const [isFilterExpanded, setIsFilterExpanded] = useState(false);

  const [equipments, setEquipments] = useState<Equipment[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageNo, setPageNo] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Debounce name input
  useEffect(() => {
    if (debounceTimer.current) clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      setPageNo(0);
      setDebouncedName(searchEquipmentName);
    }, 400);
    return () => {
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
    };
  }, [searchEquipmentName]);

  // Reset to page 0 on filter change
  useEffect(() => {
    setPageNo(0);
  }, [filterCategory, isActiveMembersOnly, isStatutoryOnly, pageSize]);

  // Fetch from API
  useEffect(() => {
    let cancelled = false;
    const fetchEquipments = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await getEquipmentPage(
          isActiveMembersOnly,
          isStatutoryOnly,
          debouncedName,
          filterCategory,
          pageNo,
          pageSize
        );
        if (!cancelled) {
          setEquipments(data.content);
          setTotalElements(data.totalElements);
          setTotalPages(data.totalPages);
        }
      } catch {
        if (!cancelled) setError('Nie udalo sie zaladowac sprzetu. Sprobuj ponownie.');
      } finally {
        setIsLoading(false);
      }
    };
    void fetchEquipments();
    return () => { cancelled = true; };
  }, [debouncedName, filterCategory, isActiveMembersOnly, isStatutoryOnly, pageNo, pageSize, refreshTrigger]);

  const allCategories = Object.keys(equipmentCategoriesLabels) as EquipmentCategory[];

  return (
    <div className="flex flex-col gap-4 h-full">
      {/* Filter panel */}
      <div className="bg-gray-50 border border-gray-200 rounded-lg">
        <button
          onClick={() => setIsFilterExpanded(!isFilterExpanded)}
          className="w-full p-2 flex items-center justify-between hover:bg-gray-100 transition-colors"
        >
          <span className="text-sm font-medium text-gray-700">Filtry sprzetu</span>
          <span className={`text-xl text-gray-600 transition-transform ${isFilterExpanded ? 'rotate-180' : ''}`}>
            ▼
          </span>
        </button>

        {isFilterExpanded && (
          <div className="p-2 border-t border-gray-200 space-y-2">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">
                Filtruj po nazwie sprzetu
              </label>
              <input
                type="text"
                placeholder="Wpisz nazwe sprzetu..."
                value={searchEquipmentName}
                onChange={(e) => setSearchEquipmentName(e.target.value)}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">
                Kategoria sprzetu
              </label>
              <select
                value={filterCategory}
                onChange={(e) => setFilterCategory(e.target.value as EquipmentCategory | 'ALL')}
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value="ALL">Wszystkie kategorie</option>
                {allCategories.map((cat) => (
                  <option key={cat} value={cat}>
                    {equipmentCategoriesLabels[cat]}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="flex items-center gap-2 text-xs text-gray-700 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={isActiveMembersOnly}
                  onChange={(e) => setIsActiveMembersOnly(e.target.checked)}
                  className="h-3.5 w-3.5"
                />
                Tylko dla aktywnych czlonkow
              </label>
              <label className="flex items-center gap-2 text-xs text-gray-700 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={isStatutoryOnly}
                  onChange={(e) => setIsStatutoryOnly(e.target.checked)}
                  className="h-3.5 w-3.5"
                />
                Tylko dla wydarzen statutowych
              </label>
            </div>

            <div className="text-xs text-gray-600 pt-1 border-t border-gray-100">
              Wyswietlono {equipments.length} z {totalElements} pozycji
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
            disabled={pageNo === 0}
            onClick={() => setPageNo(0)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            «
          </button>
          <button
            disabled={pageNo === 0}
            onClick={() => setPageNo((p) => p - 1)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            ‹
          </button>
          <span className="px-2 text-xs text-gray-600">
            {totalPages === 0 ? '0 / 0' : `${pageNo + 1} / ${totalPages}`}
          </span>
          <button
            disabled={pageNo >= totalPages - 1}
            onClick={() => setPageNo((p) => p + 1)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            ›
          </button>
          <button
            disabled={pageNo >= totalPages - 1}
            onClick={() => setPageNo(totalPages - 1)}
            className="px-2 py-1 text-xs border rounded disabled:opacity-40 hover:bg-gray-100 transition-colors"
          >
            »
          </button>
        </div>
      </div>

      {/* Equipment list */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-2">
        {isLoading ? (
          <div className="text-center py-8 text-gray-500 text-sm">Ladowanie...</div>
        ) : error ? (
          <div className="text-center py-8 text-red-500 text-sm">{error}</div>
        ) : equipments.length > 0 ? (
          equipments.map((equipment) => (
            <EquipmentListElement
              key={equipment.id}
              equipment={equipment}
              selected={selectedId === equipment.id}
              onClick={() => {
                setSelectedId(equipment.id);
                setDetailsOpen(true);
                onSelect?.(equipment.id);
              }}
              isAdmin={isAdmin}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))
        ) : (
          <div className="text-center py-8 text-gray-500">
            Nie znaleziono sprzetu spelniajacego kryteria
          </div>
        )}
      </div>
{selectedId && (
      <EquipmentDetails
        id={selectedId}
        isOpen={detailsOpen}
        onClose={() => setDetailsOpen(false)}
        onSaved={() => setRefreshTrigger((n) => n + 1)}
      />
    )}
    </div>
  );
}
