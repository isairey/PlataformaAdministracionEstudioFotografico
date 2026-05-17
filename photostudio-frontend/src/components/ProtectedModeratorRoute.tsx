import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import React from 'react';
import { checkUserStatus } from '@/api/ApiGetMe';
import { NavBarNew } from '@/components/Navbar';

export const ProtectedModeratorRoute = ({ children }: { children: React.ReactNode }) => {
    const { isAuthenticated } = useAuth();
    const location = useLocation();
    const [isModerator, setIsModerator] = React.useState<boolean | null>(null);

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    React.useEffect(() => {
        checkUserStatus()
            .then(data => setIsModerator(data.role === 'ADMIN' || data.role === 'SUPER_ADMIN' || data.role === 'MODERATOR'))
            .catch(() => setIsModerator(false));
    }, []);

    if (isModerator === null) return null; 

    if (!isAuthenticated || !isModerator) {
        return <Navigate to="/home" replace />;
    }

    return (
        <>
            <NavBarNew />
            {children}
        </>
    );
};