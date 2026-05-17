import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import React from 'react';
import { checkUserStatus } from '@/api/ApiGetMe';
import { NavBarNew } from '@/components/Navbar';

export const ProtectedAdminRoute = ({ children }: { children: React.ReactNode }) => {
    const { isAuthenticated } = useAuth();
    const location = useLocation();
    const [isAdmin, setIsAdmin] = React.useState<boolean | null>(null);

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    React.useEffect(() => {
        checkUserStatus()
            .then(data => setIsAdmin(data.role === 'ADMIN' || data.role === 'SUPER_ADMIN'))
            .catch(() => setIsAdmin(false));
    }, []);

    if (isAdmin === null) return null; 

    if (!isAuthenticated || !isAdmin) {
        return <Navigate to="/home" replace />;
    }

    return (
        <>
            <NavBarNew />
            {children}
        </>
    );
};