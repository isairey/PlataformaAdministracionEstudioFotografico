"use client"
import { useEffect, useRef, useState } from "react"
import { Loader2, ChevronFirst, ChevronLast, ChevronLeft, ChevronRight } from "lucide-react"
import { toast } from "sonner"

import { EventRequestCard, type EventRequest } from "@/components/event-management_components/EventManagementCard"
import { EventRequestManagementFilterBar } from "@/components/event-management_components/EventManagementFilter"
import {getAllFilteredRequests} from "@/api/ApiEventRequestController"
import { baseColors, loaderStyles } from "@/styles/common-styles"
import type { EventRequestStatus } from "@/lib/constants"
import { Button } from "@/components/ui/button"

export default function EventRequestsManagementPage() {
  const [requests, setRequests] = useState<EventRequest[]>([])
  const [isLoading, setIsLoading] = useState(true)

  const [searchTerm, setSearchTerm] = useState<string | undefined>(undefined)
  const [userName, setUserName] = useState<string | undefined>(undefined)
  const [isFilterExpanded, setIsFilterExpanded] = useState(false)
  const [selectedStatus, setSelectedStatus] = useState<EventRequestStatus | undefined>("PENDING")
  const [dateFrom, setDateFrom] = useState<string | undefined>(new Date().toISOString().split('T')[0])
  const [dateTo, setDateTo] = useState<string | undefined>(undefined)

  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const PAGE_SIZE = 10
  const submitTriggerRef = useRef(false)

  const handleSubmit = async () => {
    try {
      setIsLoading(true)
      submitTriggerRef.current = true

      if (dateFrom && dateTo && new Date(dateFrom) > new Date(dateTo)) {
        toast.error("Błędne daty zakresu.", {
          style: { color: baseColors.failureColor },
        })
        return
      }

      const data = await getAllFilteredRequests({
        search: searchTerm || undefined,
        name: userName || undefined,
        status: selectedStatus || undefined,
        dateFrom: dateFrom || undefined,
        dateTo: dateTo || undefined,
        page: 0,
        pageSize: PAGE_SIZE,
      })
      setRequests(data.content)
      setTotalPages(data.totalPages)
      setCurrentPage(0)
    } catch (error) {
      toast.error("Nie udało się pobrać wniosków. Spróbuj ponownie później.", {
        style: { color: baseColors.failureColor },
      })
      setRequests([])
    } finally {
      setIsLoading(false)
    }
  }

  const handleStatusChanged = (requestId: string, newStatus: any) => {
    setRequests((prevRequests) =>
      prevRequests.map((req) =>
        req.id === requestId
          ? { ...req, status: newStatus }
          : req
      )
    )
  }

  useEffect(() => {
    handleSubmit()
    window.scrollTo(0, 0)
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
        const data = await getAllFilteredRequests({
          search: searchTerm || undefined,
          status: selectedStatus || undefined,
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          page: currentPage,
          pageSize: PAGE_SIZE,
        })
        setRequests(data.content)
        setTotalPages(data.totalPages)
      } catch (error) {
        console.error("Failed to fetch requests:", error)
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
          <h1 className="text-2xl font-semibold">Zarządzanie wnioskami o wydarzenia</h1>
        </div>

        <div className="max-w-4xl mx-auto w-full flex flex-col flex-1">
          <EventRequestManagementFilterBar
            searchTerm={searchTerm}
            onSearchChange={setSearchTerm}
            userName={userName}
            onUserNameChange={setUserName}
            selectedStatus={selectedStatus}
            onStatusChange={setSelectedStatus}
            dateFrom={dateFrom}
            onDateFromChange={setDateFrom}
            dateTo={dateTo}
            onDateToChange={setDateTo}
            isFilterExpanded={isFilterExpanded}
            onToggleFilterExpanded={() => setIsFilterExpanded(!isFilterExpanded)}
            handleSubmit={handleSubmit}
          />

          {isLoading ? (
            <div className="flex justify-center py-12">
              <Loader2 className={loaderStyles.mediumLoader} />
            </div>
          ) : (
            <div className="flex-1" style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}>
              <div className="grid grid-cols-1 gap-4 pb-4">
                {requests.length > 0 ? (
                  requests.map((request) => <EventRequestCard key={request.id} eventRequest={request} onStatusChanged={handleStatusChanged} />)
                ) : (
                  <p className="text-muted-foreground py-4 text-center">Brak wniosków spełniających kryteria.</p>
                )}
              </div>
            </div>
          )}
        </div>

        <div className="flex items-center justify-between gap-2 mt-4 max-w-4xl mx-auto w-full">
          <span className="text-sm text-gray-600">
            Strona {currentPage + 1} z {totalPages}
          </span>
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
