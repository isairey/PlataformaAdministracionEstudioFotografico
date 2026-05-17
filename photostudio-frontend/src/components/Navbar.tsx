"use client"
import { Link, useNavigate } from "react-router-dom"
import { User, LogOut } from "lucide-react"

import { Button } from "@/components/ui/button"
import { NavigationMenu, NavigationMenuItem, NavigationMenuLink,
  NavigationMenuList, } from "@/components/ui/navigation-menu"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator,
  DropdownMenuTrigger, } from "@/components/ui/dropdown-menu"
import { AppSidebar } from "./Sidebar"
import { handleLogout } from "@/api/ApiUserController"
import { useAuth } from "@/context/AuthContext"
import logo from "../assets/logo.svg";
import { useEffect, useState } from "react"
import { checkUserStatus } from "@/api/ApiGetMe"

export function NavBarNew() {

    const auth = useAuth()
    const authLogout = auth?.authLogout
    const navigate = useNavigate()
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

    const handleLogoutClick = async () => {
        try {
            await handleLogout()
        } catch (error) {
            console.error("Logout API failed", error)
        }
        authLogout?.()
        navigate('/login')
    }
    const deactivateHover="hover:bg-transparent hover:text-white focus:bg-transparent focus:text-white active:bg-transparent active:text-white"

    return (
        <nav className="sticky top-0 w-full z-50">
            <NavigationMenu viewport className="max-w-none justify-between px-8 py-5 bg-main-site text-white">
                <NavigationMenuList>
                    <NavigationMenuItem>
                        <AppSidebar className={deactivateHover} />
                    </NavigationMenuItem>
                </NavigationMenuList>

                <NavigationMenuList className="flex-wrap gap-4 hidden md:flex">
                    <NavigationMenuItem>
                        <img src={logo} alt="agencja fotograficzna Logo" className="h-6 w-auto" />
                    </NavigationMenuItem>
                    <NavigationMenuItem>
                        <NavigationMenuLink asChild className={deactivateHover}>
                            <Link to="/home">Strona główna</Link>
                        </NavigationMenuLink>
                    </NavigationMenuItem>
                    <NavigationMenuItem>
                        <NavigationMenuLink asChild className={deactivateHover}>
                            <Link to="/events">Wydarzenia</Link>
                        </NavigationMenuLink>
                    </NavigationMenuItem>
                    {!isSuperAdmin && (
                        <NavigationMenuItem>
                            <NavigationMenuLink asChild className={deactivateHover}>
                                <Link to="/reservations">Rezerwacja sprzętu</Link>
                            </NavigationMenuLink>
                        </NavigationMenuItem>
                    )}
                </NavigationMenuList>

                <NavigationMenuList>
                    <NavigationMenuItem>
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className={deactivateHover}>
                                    <User className="h-6 w-6" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-48">
                                <DropdownMenuItem asChild>
                                    <Link to="/profile" className="flex items-center gap-2 cursor-pointer">
                                        <User className="h-4 w-4" />
                                        Mój Profil
                                    </Link>
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={handleLogoutClick} className="flex items-center gap-2 cursor-pointer text-destructive">
                                    <LogOut className="h-4 w-4" />
                                    Wyloguj
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </NavigationMenuItem>
                </NavigationMenuList>
            </NavigationMenu>
        </nav>
    )
}