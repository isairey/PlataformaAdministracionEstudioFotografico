import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import React from 'react';
import { NavBarNew } from '@/components/Navbar';

export const ProtectedRoute = ({ children }:{children: React.ReactNode}) => {
    const { isAuthenticated } = useAuth();
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return (
        <>
            <NavBarNew />
            {children}
        </>
    );
};