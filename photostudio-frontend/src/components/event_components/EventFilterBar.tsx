"use client"
import { X, Calendar as CalendarIcon } from "lucide-react"
import type { EventStatus, EventType } from "@/lib/constants"
import { eventStatusLabels, eventTypeLabels } from "@/lib/constants"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Calendar } from "@/components/ui/calendar"
import { format } from "date-fns"
import { formatDateToInput } from "@/lib/utils"

interface EventFilterBarProps {
searchTerm: string | undefined
onSearchChange: (term: string | undefined) => void
locationTerm: string | undefined
onLocationChange: (term: string | undefined) => void
selectedStatus: EventStatus | undefined
onStatusChange: (status: EventStatus | undefined) => void
selectedType: EventType | undefined
onTypeChange: (type: EventType | undefined) => void
onlyFreeSlots: boolean
onOnlyFreeSlotsChange: (onlyFree: boolean) => void
dateFrom: string | undefined
onDateFromChange: (date: string | undefined) => void
dateTo: string | undefined
onDateToChange: (date: string | undefined) => void
isFilterExpanded: boolean
onToggleFilterExpanded: () => void
handleSubmit: () => void
}

const EVENT_TYPES: EventType[] = ["KWF", "SKN", "WRSS", "URSS", "AGH", "AKRE", "CM", "AKT", "PRIVATE"]
const EVENT_STATUSES: EventStatus[] = ["PLANNED", "COMPLETED", "CANCELLED"]

export function EventFilterBar({
    searchTerm,
    onSearchChange,
    locationTerm,
    onLocationChange,
    selectedStatus,
    onStatusChange,
    selectedType,
    onTypeChange,
    onlyFreeSlots,
    onOnlyFreeSlotsChange,
    dateFrom,
    onDateFromChange,
    dateTo,
    onDateToChange,
    isFilterExpanded,
    onToggleFilterExpanded,
    handleSubmit
}: EventFilterBarProps) {
const hasActiveFilters = searchTerm !== undefined || selectedStatus !== undefined || selectedType !== undefined || dateFrom !== undefined || dateTo !== undefined || locationTerm !== undefined

const handleClearFilters = () => {
    onSearchChange(undefined)
    onStatusChange(undefined)
    onTypeChange(undefined)
    onDateFromChange(undefined)
    onDateToChange(undefined)
    onLocationChange(undefined)
    onOnlyFreeSlotsChange(false)
}

const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value
    if (value === "ALL") {
    onStatusChange(undefined)
    } else {
    onStatusChange(value as EventStatus)
    }
}

const handleTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value
    if (value === "ALL") {
    onTypeChange(undefined)
    } else {
    onTypeChange(value as EventType)
    }
}

return (
    <div className="max-w-4xl mx-auto w-full mb-6">
    <div className="bg-gray-50 border border-gray-200 rounded-lg">
        <button
        onClick={onToggleFilterExpanded}
        className="w-full p-3 flex items-center justify-between hover:bg-gray-100 transition-colors"
        >
        <span className="text-sm font-medium text-gray-700">Filtry wydarzeń</span>
        <span
            className={`text-xs text-gray-600 transition-transform ${
            isFilterExpanded ? "rotate-180" : ""
            }`}
        >
            ▼
        </span>
        </button>

        {isFilterExpanded && (
        <div className="p-3 border-t border-gray-200 space-y-3">
            {/* Search Input */}
            <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
                Szukaj po nazwie
            </label>
            <input
                type="text"
                placeholder="Wpisz nazwę wydarzenia..."
                value={searchTerm ?? ""} 
                onChange={(e) => onSearchChange(e.target.value)}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            </div>

            {/* Search location Input */}
            <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
                Szukaj po lokalizacji
            </label>
            <input
                type="text"
                placeholder="Wpisz nazwę lokalizacji..."
                value={locationTerm ?? ""} 
                onChange={(e) => onLocationChange(e.target.value)}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            </div>


            {/* Status Select */}
            <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
                Status
            </label>
            <select
                value={selectedStatus !== undefined ? selectedStatus : "ALL"}
                onChange={handleStatusChange}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
                <option value="ALL">Wszystkie statusy</option>
                {EVENT_STATUSES.map((status) => (
                <option key={status} value={status}>
                    {eventStatusLabels[status]}
                </option>
                ))}
            </select>
            </div>

            {/* Type Select */}
            <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
                Typ wydarzenia
            </label>
            <select
                value={selectedType !== undefined ? selectedType : "ALL"}
                onChange={handleTypeChange}
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
                <option value="ALL">Wszystkie typy</option>
                {EVENT_TYPES.map((type) => (
                <option key={type} value={type}>
                    {eventTypeLabels[type]}
                </option>
                ))}
            </select>
            </div>

            {/* Date range filter */}
            <div className="grid grid-cols-2 gap-2">
            <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                Od daty
                </label>
                <Popover>
                <PopoverTrigger asChild>
                    <Button 
                    variant="outline" 
                    className="w-full px-3 py-2 border-gray-300 justify-between h-auto font-normal text-gray-700"
                    >
                    {dateFrom ? format(new Date(dateFrom), "d MMM yyyy") : "Wybierz datę"}
                    <CalendarIcon className="h-4 w-4 opacity-50" />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                    mode="single"
                    selected={dateFrom ? new Date(dateFrom) : undefined}
                    onSelect={(d) => onDateFromChange(d ? formatDateToInput(d) : undefined)}
                    />
                </PopoverContent>
                </Popover>
            </div>

            <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                Do daty
                </label>
                <Popover>
                <PopoverTrigger asChild>
                    <Button 
                    variant="outline" 
                    className="w-full px-3 py-2 border-gray-300 justify-between h-auto font-normal text-gray-700"
                    >
                    {dateTo ? format(new Date(dateTo), "d MMM yyyy") : "Wybierz datę"}
                    <CalendarIcon className="h-4 w-4 opacity-50" />
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                    mode="single"
                    selected={dateTo ? new Date(dateTo) : undefined}
                    onSelect={(d) => onDateToChange(d ? formatDateToInput(d) : undefined)}
                    />
                </PopoverContent>
                </Popover>
            </div>
            </div>

            {/* Only Free Slots Checkbox */}
            <div className="flex items-center">
                <input
                    type="checkbox"
                    checked={onlyFreeSlots}
                    onChange={(e) => onOnlyFreeSlotsChange(e.target.checked)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label className="ml-2 block text-sm text-gray-700">
                    Pokaż tylko z wolnymi miejscami
                </label>
            </div>



            {/* Clear and Submit Buttons */}
            <div className="grid grid-cols-2 gap-2 pt-2">
            <Button
                onClick={handleClearFilters}
                disabled={!hasActiveFilters}
                variant="outline"
                className="gap-2"
            >
                <X className="h-4 w-4" />
                Wyczyść
            </Button>
            <Button
                onClick={handleSubmit}
                className="bg-main-site text-white hover:bg-black/90"
            >
                Szukaj
            </Button>
            </div>
        </div>
        )}
    </div>
    </div>
)
}
