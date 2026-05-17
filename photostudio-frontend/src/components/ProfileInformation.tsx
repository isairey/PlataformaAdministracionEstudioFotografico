import { useState, useEffect } from 'react';
import { checkUserStatus } from '../api/ApiGetMe'; 
import '../css/ProfileInformation.css'; 
import type { UserRoleType, UserFullType } from '@/api/api.types';

function ProfileInformation() {
    const [userData, setUserData] = useState<UserFullType | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchProfileData = async () => {
            try {
                const data = await checkUserStatus(); 
                setUserData(data);
            } catch (err) {
                console.error("Błąd pobierania danych profilu:", err);
                setError("Nie udało się pobrać danych. Spróbuj się zalogować ponownie.");
            } finally {
                setLoading(false);
            }
        };

        fetchProfileData();
    }, []); 

    if (loading) {
        return <div className="profile-container">Ładowanie danych...</div>;
    }

    if (error) {
        return <div className="profile-container error">{error}</div>;
    }

    if (!userData) {
        return <div className="profile-container">Brak danych użytkownika.</div>;
    }

    const mapActiveStatus = (isActive: boolean) => {
        if (isActive === true) {
            return "Tak";
        }
        return "Nie";
    };

    const mapRoleName = (role: UserRoleType) => {
        switch (role) {
            case 'ADMIN':
                return "Administrator";
            case 'USER':
                return "Fotograf";
            case 'MODERATOR':
                return "Moderator";
            case 'SUPER_ADMIN':
                return "Zarządca administracji";
            default:
                return role || "Brak roli";
        }
    };
    
    return (
        <div className="profile-container">
            <h2>Informacje o użytkowniku</h2>
            <div className="profile-card">
                
                <div className="profile-details">
                    <p><strong>Login:</strong> {userData.username || 'Brak'}</p>
                    <p><strong>Imię:</strong> {userData.name || 'Brak'}</p>
                    <p><strong>Nazwisko:</strong> {userData.surname || 'Brak'}</p>
                    <p><strong>E-mail:</strong> {userData.email || 'Brak'}</p>
                    <p><strong>Numer telefonu:</strong> {userData.phoneNumber || 'Brak'}</p>
                    <p><strong>Aktywny:</strong> {mapActiveStatus(userData.activeMember) || 'Brak'}</p>
                    <p><strong>Rola:</strong> {mapRoleName(userData.role) || 'Brak'}</p>
                </div>
            </div>
        </div>
    );
}

export default ProfileInformation;