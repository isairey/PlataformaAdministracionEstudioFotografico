"use client"
import { useEffect, useRef, useState } from "react"
import { Loader2, ChevronFirst, ChevronLast, ChevronLeft, ChevronRight } from "lucide-react"
import { toast } from "sonner"

import { EventCard } from "../components/event_components/EventCard"
import type { Event } from "@/api/api.types"
import { EventFilterBar } from "../components/event_components/EventFilterBar"
import {getFilteredEvents} from "@/api/ApiEventController"
import { baseColors, loaderStyles } from "@/styles/common-styles"
import type { EventStatus, EventType } from "@/lib/constants"
import { Button } from "@/components/ui/button"

export default function Event() {
    const [events, setEvents] = useState<Event[]>([])
    const [isLoading, setIsLoading] = useState(true)

    const [searchTerm, setSearchTerm] = useState<string | undefined>(undefined)
    const [locationTerm, setLocationTerm] = useState<string | undefined>(undefined)
    const [isFilterExpanded, setIsFilterExpanded] = useState(false)
    const [selectedStatus, setSelectedStatus] = useState<EventStatus>()
    const [selectedType, setSelectedType] = useState<EventType>()
    const [dateFrom, setDateFrom] = useState<string | undefined>(new Date().toISOString().split('T')[0])
    const [dateTo, setDateTo] = useState<string | undefined>(undefined)
    const [onlyFreeSlots, setOnlyFreeSlots] = useState(false)

    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(1)
    const PAGE_SIZE = 10
    const submitTriggerRef = useRef(false)

    const handleSubmit = async () => {
        try {
            setIsLoading(true)
            submitTriggerRef.current = true
            
            if (dateFrom && dateTo && new Date(dateFrom) > new Date(dateTo)) {
                toast.error("Błędne daty zakresu wydarzeń.", {
                    style: { color: baseColors.failureColor }
                })
                return
            }

            const data = await getFilteredEvents({
                page: 0,
                pageSize: PAGE_SIZE,
                search: searchTerm,
                location: locationTerm,
                status: selectedStatus || undefined,
                type: selectedType || undefined,
                onlyWithFreeSpots: onlyFreeSlots,
                dateFrom: dateFrom || undefined,
                dateTo: dateTo || undefined
            })
            setEvents(data.content)
            setTotalPages(data.totalPages)
            setCurrentPage(0)

        } catch (error) {
            console.error("Failed to fetch events:", error)
            toast.error("Nie udało się pobrać wydarzeń. Spróbuj ponownie później.", {
                style: {color: baseColors.failureColor}
            })
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        handleSubmit()
    }, [])

    useEffect(() => {
        if (submitTriggerRef.current) {
            submitTriggerRef.current = false
            window.scrollTo(0, 0)
            return
        }
    
        const fetchPageData = async () => {
            try {
                setIsLoading(true)
                window.scrollTo(0, 0)
                const data = await getFilteredEvents({
                    page: currentPage,
                    pageSize: PAGE_SIZE,
                    search: searchTerm,
                    location: locationTerm,
                    status: selectedStatus || undefined,
                    type: selectedType || undefined,
                    onlyWithFreeSpots: onlyFreeSlots,
                    dateFrom: dateFrom || undefined,
                    dateTo: dateTo || undefined
                })
                setEvents(data.content)
                setTotalPages(data.totalPages)
            } catch (error) {
                console.error("Failed to fetch events:", error)
            } finally {
                setIsLoading(false)
            }
        }
        
        fetchPageData()
    }, [currentPage])


    return (
        <div>
            <div className="min-h-screen flex flex-col p-8">
                <div className="flex justify-center items-center mb-4 max-w-4xl mx-auto w-full">
                    <h1 className="text-2xl font-semibold">Wydarzenia</h1>
                </div>
                <EventFilterBar
                searchTerm={searchTerm}
                onSearchChange={setSearchTerm}
                locationTerm={locationTerm}
                onLocationChange={setLocationTerm}
                selectedStatus={selectedStatus}
                onStatusChange={setSelectedStatus}
                selectedType={selectedType}
                onTypeChange={setSelectedType}
                onlyFreeSlots={onlyFreeSlots}
                onOnlyFreeSlotsChange={setOnlyFreeSlots}
                dateFrom={dateFrom}
                onDateFromChange={setDateFrom}
                dateTo={dateTo}
                onDateToChange={setDateTo}
                isFilterExpanded={isFilterExpanded}
                onToggleFilterExpanded={() => setIsFilterExpanded(!isFilterExpanded)}
                handleSubmit={handleSubmit}
                />
                <div className="max-w-4xl mx-auto w-full flex flex-col flex-1">
                    {isLoading ? (
                        <div className="flex justify-center py-12">
                            <Loader2 className={loaderStyles.mediumLoader} />
                        </div>
                    ) : (
                    <div className="flex-1">
                        <div className="grid grid-cols-1 gap-4 pb-4">
                        {events.length > 0 ? (
                            events.map((event) => <EventCard key={event.id} event={event} />)
                        ) : (
                            <p className="text-center text-gray-600">Brak wydarzeń do wyświetlenia.</p>
                        )}
                        </div>
                    </div>
                    )}
                </div>
                <div className="flex items-center justify-between gap-2 mt-4 max-w-4xl mx-auto w-full">
                    <span className="text-sm text-gray-600">Strona {currentPage + 1} z {totalPages}</span>
                    <div className="flex items-center gap-1">
                        <Button
                            variant="outline"
                            size="icon"
                            className="h-8 w-8"
                            onClick={() => setCurrentPage(0)}
                            disabled={currentPage === 0}
                        >
                            <ChevronFirst className="h-4 w-4" />
                        </Button>
                        <Button
                            variant="outline"
                            size="icon"
                            className="h-8 w-8"
                            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                            disabled={currentPage === 0}
                        >
                            <ChevronLeft className="h-4 w-4" />
                        </Button>
                        <Button
                            variant="outline"
                            size="icon"
                            className="h-8 w-8"
                            onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                            disabled={currentPage === totalPages - 1}
                        >
                            <ChevronRight className="h-4 w-4" />
                        </Button>
                        <Button
                            variant="outline"
                            size="icon"
                            className="h-8 w-8"
                            onClick={() => setCurrentPage(totalPages - 1)}
                            disabled={currentPage === totalPages - 1}
                        >
                            <ChevronLast className="h-4 w-4" />
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    )
}
