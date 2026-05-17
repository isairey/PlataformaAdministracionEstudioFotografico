"use client"
import { X, Calendar as CalendarIcon } from "lucide-react"
import type { EventRequestStatus } from "@/lib/constants"
import { eventRequestStatusLabels } from "@/lib/constants"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Calendar } from "@/components/ui/calendar"
import { format } from "date-fns"
import { formatDateToInput } from "@/lib/utils"

interface EventRequestFilterBarProps {
searchTerm: string | undefined
onSearchChange: (term: string | undefined) => void
selectedStatus: EventRequestStatus | undefined
onStatusChange: (status: EventRequestStatus | undefined) => void
dateFrom: string | undefined
onDateFromChange: (date: string | undefined) => void
dateTo: string | undefined
onDateToChange: (date: string | undefined) => void
isFilterExpanded: boolean
onToggleFilterExpanded: () => void
handleSubmit: () => void
}

const RESERVATION_STATUSES: EventRequestStatus[] = ["PENDING", "APPROVED", "REJECTED", "CANCELLED"]

export function EventRequestFilterBar({
    searchTerm,
    onSearchChange,
    selectedStatus,
    onStatusChange,
    dateFrom,
    onDateFromChange,
    dateTo,
    onDateToChange,
    isFilterExpanded,
    onToggleFilterExpanded,
    handleSubmit
}: EventRequestFilterBarProps) {
const hasActiveFilters = searchTerm !== undefined || selectedStatus !== undefined || dateFrom !== undefined || dateTo !== undefined

const handleClearFilters = () => {
    onSearchChange(undefined)
    onStatusChange(undefined)
    onDateFromChange(undefined)
    onDateToChange(undefined)
}

const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value
    if (value === "ALL") {
    onStatusChange(undefined)
    } else {
    onStatusChange(value as EventRequestStatus)
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
                {RESERVATION_STATUSES.map((status) => (
                <option key={status} value={status}>
                    {eventRequestStatusLabels[status]}
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
