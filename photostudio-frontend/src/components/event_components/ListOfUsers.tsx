import { useState } from "react"
import { Loader2 } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { toast } from "sonner"

import { buttonStyles, loaderStyles, baseColors } from "@/styles/common-styles"
import {getAllUsersAssignedToEvent} from "@/api/ApiEventController"
import {type User} from "@/api/api.types"


function EventAttendees({ eventId }: { eventId: string }) {

const [users, setUsers] = useState<User[] | null>(null)
const [isLoading, setIsLoading] = useState(false)

const handleFetchUsers = async () => {
    setIsLoading(true)
    try {
    const userList = await getAllUsersAssignedToEvent(eventId)
    setUsers(userList)
    
    } catch (error) {
    console.error("Błąd pobierania użytkowników", error)
    toast.error("Nie udało się pobrać listy użytkowników. Spróbuj ponownie później.", {
        style: { color: baseColors.failureColor }
    })
    } finally {
    setIsLoading(false)
    }
}

if (!users && !isLoading) {
    return (
    <div className="pt-2">
        <Button 
        size={buttonStyles.primaryButtonSize}
        className={buttonStyles.primaryButton}
        onClick={handleFetchUsers}
        >
        Pokaż uczestników
        </Button>
    </div>
    )
}

if (isLoading) {
    return (
    <div className="flex justify-center py-4">
        <Loader2 className={loaderStyles.mediumLoader} />
    </div>
    )
}

if (users && users.length === 0) {
    return <p className="text-sm text-muted-foreground text-center py-2">Brak przypisanych osób.</p>
}

return (
    <div className="space-y-2 animate-in fade-in zoom-in-95 duration-200">
    <Separator className="my-2" />
    <h4 className="font-medium text-sm">Lista uczestników:</h4>
    
    <div className="max-h-[150px] overflow-y-auto pr-2 space-y-2">
        {users?.map((user) => (
        <div key={user.id} className="text-sm bg-muted/50 p-2 rounded-md font-medium">
            {user.name} {user.surname}
        </div>
        ))}
    </div>
    </div>
)
}
export default EventAttendees
