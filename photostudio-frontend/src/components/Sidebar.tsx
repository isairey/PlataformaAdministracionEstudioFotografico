import { Link } from "react-router-dom"
import { Menu } from "lucide-react"
import { Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { Button } from "@/components/ui/button"
import { useEffect, useState } from "react"
import { checkUserStatus } from "@/api/ApiGetMe"


interface AppSidebarProps {
    className?: string
}

export function AppSidebar({ className }: AppSidebarProps) {

  const [isModerator, setIsModerator] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [isSuperAdmin, setIsSuperAdmin] = useState(false);

  useEffect(() => {
        const fetchUserStatus = async () => {
            try {
                const userData = await checkUserStatus();
                setIsModerator(userData.role === 'MODERATOR' || userData.role === 'ADMIN' || userData.role === 'SUPER_ADMIN');
                setIsAdmin(userData.role === 'SUPER_ADMIN' || userData.role === 'ADMIN');
                setIsSuperAdmin(userData.role === 'SUPER_ADMIN');
            } catch (error) {
                console.error("Error checking user status:", error);
            }   
        };

        fetchUserStatus();
  }, []);
  
  return (
    <Sheet>
      <SheetTrigger asChild>
        <Button variant="ghost" size="icon" className={className}>
          <Menu className="h-6 w-6" />
        </Button>
      </SheetTrigger>
      <SheetContent side="left" className="w-60 flex flex-col">
        <SheetHeader>
          <SheetTitle>Menu</SheetTitle>
        </SheetHeader>
        <nav className="flex flex-col gap-2 mt-2 overflow-y-auto flex-1 pr-4" style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}>
          <div className="md:hidden flex flex-col">
            <span className="px-3 py-2 rounded-md font-semibold">Strony</span>
            <SheetClose asChild>
              <Link 
              to="/home"
              className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                Strona główna
              </Link>
            </SheetClose>
            <SheetClose asChild>
              <Link 
              to="/events"
              className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                Wydarzenia
              </Link>
            </SheetClose>
            <SheetClose asChild>
              <Link 
              to="/reservations"
              className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                Rezerwacje sprzętu
              </Link>
            </SheetClose>
          </div>
          <span className="px-3 py-2 rounded-md font-semibold">Moje wnioski</span>
          <SheetClose asChild>
            <Link 
                to="/event-requests" 
                className="px-3 py-1 rounded-md hover:bg-accent transition-colors font-normal">
                Wnioski o wydarzenia
            </Link>
          </SheetClose>
          {!isSuperAdmin && (
            <SheetClose asChild>
              <Link
                to="/reservations"
                className="px-3 py-1 rounded-md hover:bg-accent transition-colors font-normal">
                Wnioski o sprzęt
            </Link>
            </SheetClose>)}
          {isModerator && (
            <span className="px-3 py-2 rounded-md font-semibold">Zarządzanie</span>
          )}
          {isModerator && (
            <SheetClose asChild>
              <Link 
                to="/create-event" 
                className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                Dodaj wydarzenie
              </Link>
            </SheetClose>
          )}
          {isModerator && (
            <SheetClose asChild>
              <Link 
                to="/create-equipment" 
                className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                Dodaj sprzęt
              </Link>
            </SheetClose>

          )}
          {isAdmin && (
            <SheetClose asChild>
              <Link 
                to="/admin-panel" 
                className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                Panel administracyjny
              </Link>
            </SheetClose>
          )}
          {isModerator && (
            <>
            <span className="px-3 py-2 rounded-md font-semibold">Wnioski o rezerwacje</span>
            <SheetClose asChild>
              <Link 
                to="/reservations-managment" 
                className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                 Sprzęt
              </Link>
            </SheetClose>
            <SheetClose asChild>
              <Link 
                to="/event-requests-management" 
                className="px-3 py-1 rounded-md hover:bg-accent transition-colors">
                Wydarzenia
              </Link>
            </SheetClose>
            </>
          )}
        </nav>
      </SheetContent>
    </Sheet>
  )
}