"use client";

import { useState, useEffect} from "react";
import { buttonStyles, loaderStyles } from "@/styles/common-styles";
import AvailableEquipmentList from "../components/equipment_reservation_components/AvailableEquipmentList";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { Calendar as CalendarIcon } from "lucide-react";
import { format} from "date-fns";
import { formatDateToInput } from "@/lib/utils";
import { toast } from "sonner";
import { baseColors } from "@/styles/common-styles";
import {getAllActiveEventRequests, } from "@/api/ApiEventRequestController";
import axios from "axios";
import { checkUserStatus } from "@/api/ApiGetMe";
import { createEquipmentReservation } from "@/api/ApiEquipmentReservationController";

export default function AddReservationForm() {

    interface EventRequestOption {
        id: string;
        name: string;
        eventDate: string;
    }

    const [eventRequestId, setEventRequestId] = useState<string>("");
    const [dateStart, setDateStart] = useState<string>("");
    const [dateEnd, setDateEnd] = useState<string>("");
    const [isEmergency, setIsEmergency] = useState<boolean>(false);
    const [isPrivate, setIsPrivate] = useState<boolean>(false);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<string[]>([]);
    const [eventChosen, setEventChosen] = useState<boolean>(false);
    const [statutory, setStatutory] = useState<boolean>(false);
    const [comment, setComment] = useState<string>("");
    const [eventRequests, setEventRequests] = useState<EventRequestOption[]>([]);
    const [isSuperAdmin, setIsSuperAdmin] = useState(false);

     useEffect(() => {
        const fetchUserStatus = async () => {
            try {
                const userData = await checkUserStatus();
                setIsSuperAdmin(userData.role === 'SUPER_ADMIN');
            } catch (error) {
                console.error("Error checking user status:", error);
            }
        };
        fetchUserStatus();
    }, []);

    useEffect(() => {
        if (isEmergency || isPrivate) {
        setEventRequestId("");
        }
    }, [isEmergency,isPrivate]);

    useEffect(() => {
        setStatutory(!isPrivate && (eventRequestId != "" || isEmergency))
    }, [isPrivate, eventRequestId, isEmergency]);

    useEffect(() => {
        setEventChosen(eventRequestId !== "" || isEmergency || isPrivate);
    }, [eventRequestId, isEmergency, isPrivate]);

    // Fetch all current event requests

    useEffect(() => {
        const fetchEventRequests = async () => {
        try {
            const response = await getAllActiveEventRequests();
            const formatted: EventRequestOption[] = response.map((item: any) => ({
                id: item.id.toString(),
                name: item.eventName,
                eventDate: item.eventDate
            }));
            setEventRequests(formatted);
        } catch (error) {
            console.error("Failed to fetch event requests:", error);
            toast.error("Nie udało się załadować wniosków o wydarzenia.", { 
            style: { color: baseColors.failureColor } 
            });
        }
        };

        fetchEventRequests();
    }, []);

    const formatLocalDate = (date: string) => {

        const [year, month, day] = date.split("-");
        if (!year || !month || !day) {
            return date;
        }

        return `${day}.${month}.${year}`;
    };

    const selectedEvent = eventRequests.find((eventRequest) => eventRequest.id === eventRequestId);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        const start = new Date(dateStart);
        const end = new Date(dateEnd);
        const now = new Date();
        const isCommentRequired = isEmergency || isPrivate;
        const trimmedComment = comment.trim();
        
        // Input data validation
        const validations = [
            { condition: Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()), message: "Podaj poprawne daty rozpoczęcia i zakończenia." },
            { condition: start <= now, message: "Data rozpoczęcia musi być w przyszłości." },
            { condition: end <= now, message: "Data zakończenia musi być w przyszłości." },
            { condition: start >= end, message: "Data rozpoczęcia musi poprzedzać datę zakończenia." },
            { condition: selectedEquipmentIds.length === 0, message: "Wybierz przynajmniej jeden sprzęt." },
            { condition: isCommentRequired && trimmedComment.length === 0, message: "Komentarz jest wymagany dla nagłego lub prywatnego wydarzenia." },
            {condition : comment.length > 250, message: "Komentarz nie może przekraczać 250 znaków." }
        ];

        for (const { condition, message } of validations) {
            if (condition) {
                toast.error(message, { style: { color: baseColors.failureColor } });
                return;
            }
        }

        try {
            setIsLoading(true);
            await createEquipmentReservation(
                Number(eventRequestId),
                dateStart,
                dateEnd,
                selectedEquipmentIds.map((id) => Number(id)),
                comment,
                isPrivate,
                isEmergency
            );
            toast.success("Rezerwacja została dodana pomyślnie!", { style: { color: baseColors.successColor } });
            // Reset form after successful submission
            setEventRequestId("");
            setDateStart("");
            setDateEnd("");
            setIsEmergency(false);
            setIsPrivate(false);
            setSelectedEquipmentIds([]);
            setComment("");
        } catch (error) {
            console.error("Failed to create reservation:", error);
            const backendMessage = axios.isAxiosError(error)
                ? (error.response?.data?.message as string | undefined)
                : undefined;
            toast.error(backendMessage || "Nie udało się dodać rezerwacji. Sprawdź dane i spróbuj ponownie.", {
                style: { color: baseColors.failureColor },
            });
        } finally {
            setIsLoading(false);
        }
    };


    return (
        <>
        <div className="h-full">
        <div className="p-6 max-w-4xl mx-auto mt-2 mb-1">
            <Card>
            <CardHeader>
                <CardTitle>Dodaj Rezerwację</CardTitle>
                <CardDescription>Wybierz sprzęt i określ okres rezerwacji</CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit} className="space-y-6">
                {/* Event Request & Checkboxes Row */}
                <div className="grid grid-cols-1 gap-4">
                    <div className="gap-4">
                    <div className="flex flex-col gap-2">
                        <Label htmlFor="eventRequest" className="text-sm font-medium justify-center">
                        Wybierz wydarzenie dotyczące rezerwacji
                        </Label>
                        <select
                        id="eventRequest"
                        value={eventRequestId}
                        onChange={(e) => setEventRequestId(e.target.value)}
                        className="px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:bg-gray-100 disabled:opacity-50"
                        disabled={isEmergency || isPrivate}
                        required={!isEmergency && !isPrivate}
                        >
                        <option value="" hidden>
                            Wybierz wydarzenie
                        </option>
                        {eventRequests.map((req) => (
                            <option key={req.id} value={req.id}>
                            {`${req.name} (${formatLocalDate(req.eventDate)})`}
                            </option>
                        ))}
                        </select>
                        {selectedEvent ? (
                            <p className="text-xs text-gray-600">
                                Data wydarzenia: {formatLocalDate(selectedEvent.eventDate)}
                            </p>
                        ) : null}
                    </div>
                    <div className="flex pt-4 justify-center gap-10">
                        <div className="flex items-end gap-2">
                        <input
                            id="emergency"
                            type="checkbox"
                            checked={isEmergency}
                            onChange={(e) => {
                            setIsEmergency(e.target.checked);
                            if (e.target.checked) setIsPrivate(false);
                            }}
                            className="rounded cursor-pointer"
                        />
                        <Label htmlFor="emergency" className="text-sm font-medium cursor-pointer">
                            Nagłe wydarzenie
                        </Label>
                        </div>

                        <div className="flex items-end gap-2">
                        <input
                            id="private"
                            type="checkbox"
                            checked={isPrivate}
                            onChange={(e) => {
                            setSelectedEquipmentIds([]);
                            setIsPrivate(e.target.checked);
                            if (e.target.checked) setIsEmergency(false);
                            }}
                            className="rounded cursor-pointer"
                        />
                        <Label htmlFor="private" className="text-sm font-medium cursor-pointer">
                            Prywatne wydarzenie
                        </Label>
                        </div>
                    </div>
                    </div>
                </div>

                <div className="flex flex-col gap-2">
                    <Label htmlFor="reservationComment" className="text-sm font-medium">
                        Komentarz {(isEmergency || isPrivate) ? "" : "(opcjonalnie)"}
                    </Label>
                    <textarea
                        id="reservationComment"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        required={isEmergency || isPrivate}
                        rows={3}
                        placeholder="Dodaj komentarz do rezerwacji"
                        className="w-full px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
                    />
                </div>

                {/* Date Fields */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="flex flex-col gap-2">
                    <Label className="text-sm font-medium">
                        Data Rozpoczęcia
                    </Label>
                    <div className="flex gap-2">
                        <Popover>
                        <PopoverTrigger asChild>
                            <Button variant="outline" className="flex-1 px-2 py-1 border-gray-300 justify-between h-auto font-normal text-gray-700">
                            {dateStart ? format(new Date(dateStart), "d MMM yyyy") : "Wybierz datę"}
                            <CalendarIcon className="h-4 w-4 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-auto p-0" align="start">
                            <Calendar
                            mode="single"
                            selected={dateStart ? new Date(dateStart) : undefined}
                            onSelect={(d) => {
                                if (d) {
                                const dateStr = formatDateToInput(d);
                                const time = dateStart.split('T')[1] || '09:00';
                                setDateStart(`${dateStr}T${time}`);
                                }
                            }}
                            />
                        </PopoverContent>
                        </Popover>
                        <Input
                        type="time"
                        value={dateStart.split('T')[1] || '09:00'}
                        onChange={(e) => {
                            const date = dateStart.split('T')[0] || formatDateToInput(new Date());
                            setDateStart(`${date}T${e.target.value}`);
                        }}
                        className="px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm w-24"
                        />
                    </div>
                    </div>

                    <div className="flex flex-col gap-2">
                    <Label className="text-sm font-medium">
                        Data Zakończenia
                    </Label>
                    <div className="flex gap-2">
                        <Popover>
                        <PopoverTrigger asChild>
                            <Button variant="outline" className="flex-1 px-2 py-1 border-gray-300 justify-between h-auto font-normal text-gray-700">
                            {dateEnd ? format(new Date(dateEnd), "d MMM yyyy") : "Wybierz datę"}
                            <CalendarIcon className="h-4 w-4 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-auto p-0" align="start">
                            <Calendar
                            mode="single"
                            selected={dateEnd ? new Date(dateEnd) : undefined}
                            onSelect={(d) => {
                                if (d) {
                                const dateStr = formatDateToInput(d);
                                const time = dateEnd.split('T')[1] || '18:00';
                                setDateEnd(`${dateStr}T${time}`);
                                }
                            }}
                            />
                        </PopoverContent>
                        </Popover>
                        <Input
                        type="time"
                        value={dateEnd.split('T')[1] || '18:00'}
                        onChange={(e) => {
                            const date = dateEnd.split('T')[0] || formatDateToInput(new Date());
                            setDateEnd(`${date}T${e.target.value}`);
                        }}
                        className="px-2 py-1 text-sm border border-gray-300 rounded-md shadow-sm w-24"
                        />
                    </div>
                    </div>
                </div>

                {/* Submit Button */}
                <Button
                    type="submit"
                    className={buttonStyles.primaryButton}
                    disabled={isLoading || isSuperAdmin}
                >
                    {isLoading && <div className={`${loaderStyles.inButtonLoader} inline-block`}></div>}
                    <span>Dodaj Rezerwację</span>
                </Button>
                </form>
            </CardContent>
            </Card>
        </div>
            <div className="max-w-4xl mx-auto mb-6">
                <AvailableEquipmentList  
                startDate={dateStart} 
                endDate={dateEnd}
                selectedEquipmentIds={selectedEquipmentIds}
                statutory={statutory}
                eventChosen={eventChosen}
                onSelectionChange={setSelectedEquipmentIds}
                />
            </div>
        </div>
        </>
    );
}