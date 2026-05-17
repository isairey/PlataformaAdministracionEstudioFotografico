"use client"
import { useEffect, useState } from "react"
import { Calendar, Loader2, MapPin } from "lucide-react"

import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogTrigger, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"
import { toast } from "sonner"

import {approveEventRequest, rejectEventRequest} from "@/api/ApiEventRequestController"
import { type ReservationItemStatus, reservationItemStatusColors, reservationItemStatusLabels } from "@/lib/constants"
import { baseColors, cardStyles, buttonStyles, loaderStyles } from "@/styles/common-styles"
import { formatDate, formatDateOnly } from "@/lib/utils"
import { getUserFullById } from "@/api/ApiUserController"
import {type User} from "@/api/api.types"

export interface EventRequest {
id: string
eventId: string
userId: string
eventName: string
eventDate: string
eventLocation: string
createdDate: string
createdTime: string
comment?: string
status: ReservationItemStatus
}



interface EventRequestCardProps {
eventRequest: EventRequest
onStatusChanged?: (requestId: string, newStatus: ReservationItemStatus) => void
}

export function EventRequestCard({ eventRequest, onStatusChanged }: EventRequestCardProps) {
const [isOpen, setIsOpen] = useState(false)
const [user, setUser] = useState<User | undefined>(undefined)
const [isApproving, setIsApproving] = useState(false)
const [isRejecting, setIsRejecting] = useState(false)

const handleApproval = async () => {
    setIsApproving(true)
    try {
        await approveEventRequest(eventRequest.eventId, eventRequest.id, eventRequest.userId)
        setIsOpen(false)
        onStatusChanged?.(eventRequest.id, "APPROVED")
        toast.success("Wniosek został zaakceptowany.", {
            style: { color: baseColors.successColor }
        })
    } catch (error) {
        toast.error("Nie udało się zaakceptować wniosku.", {
            style: { color: baseColors.failureColor }
        })
    } finally {
        setIsApproving(false)
    }
}

const handleRejection = async () => {
    setIsRejecting(true)
    try {
        await rejectEventRequest(eventRequest.eventId,eventRequest.id, eventRequest.userId)
        setIsOpen(false)
        onStatusChanged?.(eventRequest.id, "REJECTED")
        toast.success("Wniosek został odrzucony.", {
            style: { color: baseColors.successColor }
        })
    } catch (error) {
        toast.error("Nie udało się odrzucić wniosku.", {
            style: { color: baseColors.failureColor }
        })
    } finally {
        setIsRejecting(false)
    }
}

useEffect(() => {
    if (!isOpen) return

    const importUserData = async () => {
        try {
            const user = await getUserFullById(eventRequest.userId)
            setUser(user)
        } catch (error) {
            console.error("Failed to fetch user:", error)
        }
    }
    importUserData()
}, [isOpen, eventRequest.userId])

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

             {user !== undefined && (
                <>
                <div className={cardStyles.dialogDetailRow}>
                <span className={cardStyles.dialogLabel}>Użytkownik:</span>
                    <p className={cardStyles.dialogValue}>{user.name + " " + user.surname}</p>
                </div>
                <div className={cardStyles.dialogDetailRow}>
                <span className={cardStyles.dialogLabel}>Aktywny:</span>
                    <p className={cardStyles.dialogValue}>{ user.activeMember === true ? "Tak" : "Nie"}</p>
                </div>
                </>
            )}

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
                onClick={handleApproval} 
                disabled={eventRequest.status == "CANCELLED" || eventRequest.status == "APPROVED"}
                className={buttonStyles.primaryButton}
                size={buttonStyles.primaryButtonSize}
            >
                {isApproving&& <Loader2 className={loaderStyles.inButtonLoader} />}
                Akceptuj 
            </Button>
            <Button 
                onClick={handleRejection} 
                disabled={eventRequest.status == "CANCELLED" || eventRequest.status == "REJECTED"}
                className={buttonStyles.primaryButton}
                size={buttonStyles.primaryButtonSize}
            >
                {isRejecting&& <Loader2 className={loaderStyles.inButtonLoader} />}
                Odrzuć 
            </Button>
        </div>
        </div>
    </DialogContent>
    </Dialog>
)
}
