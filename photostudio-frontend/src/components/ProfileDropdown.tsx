import { useState, useEffect, useRef} from 'react';
import { Link } from 'react-router-dom';
import '../css/ProfileDropdown.css';
import { handleLogout } from '@/api/ApiUserController';
import { useAuth } from '../context/AuthContext';

const ProfileIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" width="28" height="28">
        <path strokeLinecap="round" strokeLinejoin="round" d="M17.982 18.725A7.488 7.488 0 0012 15.75a7.488 7.488 0 00-5.982 2.975m11.963 0a9 9 0 10-11.963 0m11.963 0A8.966 8.966 0 0112 21a8.966 8.966 0 01-5.982-2.275M15 9.75a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
);


function ProfileDropdown() {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement | null>(null);
    const auth = useAuth()
    const  authLogout  = auth?.authLogout;

    const toggleDropdown = () => {
        setIsOpen(!isOpen);
    };

    const handleLogoutClick = async () => {
        setIsOpen(false);
        try {
            await handleLogout();
        } catch (error) {
            console.error("Logout API failed, but forcing local state logout.", error);
        }
        authLogout();
    };

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            const el = dropdownRef.current;
            if (!el) return;
            if (event.target instanceof Node && !el.contains(event.target)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    return (
        <div className="profile-dropdown-container" ref={dropdownRef}>
            
            <div 
                className={`profile-dropdown-icon ${isOpen ? 'active' : ''}`} 
                onClick={toggleDropdown}
            >
                <ProfileIcon />
            </div>

            <div className={`profile-dropdown-menu ${isOpen ? 'active' : ''}`}>
                <Link to="/profile" onClick={() => setIsOpen(false)}>
                    Mój Profil
                </Link>
                
                <button onClick={handleLogoutClick}>
                    Wyloguj
                </button>
            </div>

        </div>
    );
}

export default ProfileDropdown;