"use client"
import { useEffect, useState } from "react"
import { Calendar, Loader2 } from "lucide-react"
import { toast } from "sonner"

import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogTrigger, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"

import { eventStatusColors, eventTypeLabels, eventStatusLabels } from "@/lib/constants"
import { formatDate } from "@/lib/utils"
import { cardStyles, buttonStyles, baseColors, loaderStyles } from "@/styles/common-styles"

import {userAlreadyRequested, createOwnEventRequest} from "@/api/ApiEventRequestController"
import EventAttendees from "./ListOfUsers"

import { type Event } from "@/api/api.types"


interface EventCardProps {
    event: Event
}

export function EventCard({ event }: EventCardProps) {
const [isJoining, setIsJoining] = useState(false)
const [alreadyRequested, setAlreadyRequested] = useState(false)
const [isDialogOpen, setIsDialogOpen] = useState(false)

useEffect(() => {
    if (!isDialogOpen) {
        setAlreadyRequested(false)
        return
    }
    const checkUserRequestStatus = async () => {
        try {
            const hasRequested = await userAlreadyRequested(event.id)
            setAlreadyRequested(hasRequested)
        } catch (error) {
            console.error("Failed to check if user already requested to join event:", error)
            setAlreadyRequested(true) // Assume user has requested if check fails 
        }
    }

    checkUserRequestStatus()
}, [isDialogOpen])

const handleJoinEvent = async () => {
    setIsJoining(true)
    try {
    await createOwnEventRequest(event.id)
    toast.success("Pomyślnie zapisano na wydarzenie.", {
        style: { color: baseColors.successColor }
    })
    setAlreadyRequested(true)
    } catch (error) {
    console.error("Failed to join event:", error)
    toast.error("Nie udało się zapisać na wydarzenie. Spróbuj ponownie później.", {
        style: { color: baseColors.failureColor }
    })
    } finally {
    setIsJoining(false)
    }
}

return (
    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
    <DialogTrigger asChild>
        <Card className={cardStyles.container}>
        <div className={cardStyles.contentWrapper}>
            <div className={cardStyles.headerRow}>
            <h3 className={`${cardStyles.headerTitle} flex-1 min-w-0 whitespace-normal break-words`}>{event.name}</h3>
            <Badge className={eventStatusColors[event.status]}>{eventStatusLabels[event.status]}</Badge>
            </div>

            <div className={cardStyles.textRow}>
            <Calendar className={cardStyles.icon} />
            <span>{formatDate(event.date + "T" + event.time)}</span>
            </div>

            <div className={cardStyles.textRow}>
            <span className={cardStyles.containerSubText}>Typ wydarzenia:</span>
            <Badge className="bg-main-site text-white">{eventTypeLabels[event.type]}</Badge>
            </div>
        </div>
        </Card>
    </DialogTrigger>

    <DialogContent className={`${cardStyles.dialogContentSize} min-w-0 max-w-full`}>
        <div className="overflow-hidden max-w-full">
        <DialogHeader>
        <DialogTitle className="whitespace-normal break-words text-center">{event.name}</DialogTitle>
        <DialogDescription className="whitespace-normal break-words text-center">
            {event.description || "Brak opisu wydarzenia"}
        </DialogDescription>
        </DialogHeader>

        <div className={cardStyles.dialogGrid}>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Data:</span>
            <p className={cardStyles.dialogValue}>{formatDate(event.date + "T" + event.time)}</p>
            </div>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Lokalizacja:</span>
            <p className={`${cardStyles.dialogValue} whitespace-normal break-words`}>{event.location}</p>
            </div>
        <div className={cardStyles.dialogGridCols}>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Wymagane osoby:</span>
            <p className={cardStyles.dialogValue}>{event.numberOfPeopleRequired}</p>
            </div>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Przypisane osoby:</span>
            <p className={cardStyles.dialogValue}>{event.numberOfAssignedPeople}</p>
            </div>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Typ:</span>
            <div><Badge className="bg-main-site text-white">{eventTypeLabels[event.type]}</Badge></div>
            </div>
            <div className={cardStyles.dialogDetailRow}>
            <span className={cardStyles.dialogLabel}>Status:</span>
            <div><Badge className={eventStatusColors[event.status]}>{eventStatusLabels[event.status]}</Badge></div>
            </div>
        </div>
        </div>
        
        <div className="space-y-2">
            <EventAttendees eventId={event.id} />
            <Button 
                onClick={handleJoinEvent} 
                disabled={isJoining || event.status !== "PLANNED" || event.numberOfAssignedPeople >= event.numberOfPeopleRequired || alreadyRequested}
                className={buttonStyles.primaryButton}
                size={buttonStyles.primaryButtonSize}
            >
                {isJoining && <Loader2 className={loaderStyles.inButtonLoader} />}
                Zapisz się
            </Button>
        </div>
        </div>
    </DialogContent>
    </Dialog>
)
}
