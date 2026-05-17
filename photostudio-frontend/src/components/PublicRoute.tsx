import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { ReactNode } from 'react';

interface PublicRouteProps {
    children: ReactNode;
}

export const PublicRoute = ({ children }: PublicRouteProps) => {
    const auth = useAuth();
    const  isAuthenticated  = auth?.isAuthenticated;

    if (isAuthenticated) {
        return <Navigate to="/home" replace />;
    }
    return children;
};