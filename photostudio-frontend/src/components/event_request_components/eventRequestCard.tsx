"use client"
import { useState } from "react"
import { Calendar, Loader2, MapPin } from "lucide-react"

import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogTrigger, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"
import { toast } from "sonner"

import {cancelOwnEventRequest} from "@/api/ApiEventRequestController"
import { type EventRequestStatus, reservationItemStatusColors, reservationItemStatusLabels } from "@/lib/constants"
import { baseColors, cardStyles, buttonStyles, loaderStyles } from "@/styles/common-styles"
import { formatDate, formatDateOnly } from "@/lib/utils"
import { type EventRequest } from "@/api/api.types"


interface EventRequestCardProps {
eventRequest: EventRequest
onCancel: (requestId: string, status: EventRequestStatus) => void
}

export function EventRequestCard({ eventRequest, onCancel }: EventRequestCardProps) {
const [isCancelling, setIsCancelling] = useState(false)
const [isOpen, setIsOpen] = useState(false)

const handleCancelRequest = async () => {
    setIsCancelling(true)
    try {
    await cancelOwnEventRequest(eventRequest.id)
    toast.success("Poprawnie anulowano wniosek.", {
        style: { color: baseColors.successColor }
    })
    onCancel(eventRequest.id, "CANCELLED")
    setIsOpen(false)
    } catch (error) {
    console.error("Failed to cancel request:", error)
    toast.error("Nie udało się anulować wniosku. Spróbuj ponownie później.", {
        style: { color: baseColors.failureColor }
    })
    } finally {
    setIsCancelling(false)
    }
}

return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
    <DialogTrigger asChild>
        <Card className={cardStyles.container}>
        <div className={cardStyles.contentWrapper}>
            <div className={cardStyles.headerRow}>
            <h3 className={`${cardStyles.headerTitle} flex-1 min-w-0 whitespace-normal break-words`}>{eventRequest.eventName}</h3>
            <Badge className={reservationItemStatusColors[eventRequest.status]}>{reservationItemStatusLabels[eventRequest.status]}</Badge>
            </div>

            <div className={cardStyles.textRow}>
            <Calendar className={cardStyles.icon} />
            <span>{formatDateOnly(eventRequest.eventDate)}</span>
            </div>

            <div className={cardStyles.textRow}>
            <MapPin className={cardStyles.icon} />
            <span>{eventRequest.eventLocation}</span>
            </div>
        </div>
        </Card>
    </DialogTrigger>

    <DialogContent className={cardStyles.dialogContentSize}>
        <div className="overflow-hidden max-w-full">
            <DialogHeader>
                <DialogTitle className="whitespace-normal break-words text-left">{eventRequest.eventName}</DialogTitle>
                <DialogDescription className="whitespace-normal break-words text-left">
                    Szczegóły twojego wniosku o dołączenie do wydarzenia.
                </DialogDescription>
            </DialogHeader>
        </div>
        
        <div className={cardStyles.dialogGrid}>
        <div className="grid grid-cols-1 gap-4">
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Data wydarzenia:</span>
            <p className={cardStyles.dialogValue}>{formatDateOnly(eventRequest.eventDate)}</p>
            </div>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Lokalizacja:</span>
            <p className={`${cardStyles.dialogValue} whitespace-normal break-words`}>{eventRequest.eventLocation}</p>
            </div>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Data złożenia:</span>
            <p className={cardStyles.dialogValue}>{formatDate(`${eventRequest.createdDate}T${eventRequest.createdTime}`)}</p>
            </div>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Status:</span>
            <div><Badge className={reservationItemStatusColors[eventRequest.status]}>{reservationItemStatusLabels[eventRequest.status]}</Badge></div>
            </div>
            <div className="flex flex-col gap-1">
            <span className={cardStyles.dialogLabel}>Komentarz:</span>
            <p className={`${cardStyles.dialogValue} whitespace-normal break-words`}>{eventRequest.comment || "Brak komentarza"}</p>
            </div>
        </div>
        
        <div className="space-y-2 pt-4">
            <Button 
                onClick={handleCancelRequest} 
                disabled={isCancelling || eventRequest.status !== "PENDING"}
                className={buttonStyles.primaryButton}
                size={buttonStyles.primaryButtonSize}
            >
                {isCancelling && <Loader2 className={loaderStyles.inButtonLoader} />}
                Zrezygnuj z wniosku
            </Button>
        </div>
        </div>
    </DialogContent>
    </Dialog>
)
}
