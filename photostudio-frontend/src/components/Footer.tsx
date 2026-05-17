import { Mail, MapPin } from 'lucide-react'
import logo from "../assets/logo.svg"

export function Footer() {
    const currentYear = new Date().getFullYear()

    return (
        <footer className="bg-main-site text-white py-10 px-6">
            <div className="max-w-4xl mx-auto flex flex-col gap-6">
                
                <div className="flex flex-col md:flex-row items-center md:justify-between gap-4">
                    <div className="flex items-center gap-3">
                        <img src={logo} alt="agencja fotograficzna Logo" className="h-6 w-auto" />
                        <p className="text-sm">
                            Agencja fotograficzna
                        </p>
                    </div>

                    <div className="flex flex-row gap-4 text-sm">
                        <div className="hidden md:flex items-center gap-2">
                            <MapPin className="h-4 w-4" />
                            <span>Kraków, Polska</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <Mail className="h-4 w-4" />
                            <a href="mailto:kontakt@af.pl" className="hover:underline">
                                kontakt@af.pl
                            </a>
                        </div>
                    </div>
                </div>

                <div className="text-center text-xs text-gray-400 border-t border-gray-600 pt-4">
                    &copy; {currentYear} DAF. Wszelkie prawa zastrzeżone.
                </div>
                
            </div>
        </footer>
    )
}