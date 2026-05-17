import { createContext, useContext, useState, useEffect} from 'react';
import type { ReactNode } from 'react';
import { initializeCsrf } from '../api/ApiCsrf';
import { checkUserStatus } from '../api/ApiGetMe';
import { filterUserData } from '../utils/FilterUser';

interface AuthContextType {
    user: any;
    isLoading: boolean;
    isAuthenticated: boolean;
    authLogout: () => void;
    authLogin: (userData: any) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState(() => {
        const savedUser = localStorage.getItem('user_cache');
        try {
            return savedUser ? JSON.parse(savedUser) : null;
        } catch (error) {
            console.error('Błąd parsowania user_cache:', error);
            localStorage.removeItem('user_cache');
            return null;
        }
    });
    
    const [isLoading, setIsLoading] = useState(!user); 

    useEffect(() => {
        const initAuth = async () => {
            try {
                await initializeCsrf();
                const userData = await checkUserStatus();
                setUser(userData);
                localStorage.setItem('user_cache', JSON.stringify(filterUserData(userData)));
            } catch (error) {
                console.log('Sesja nieważna:', error);
                setUser(null);
                localStorage.removeItem('user_cache');
            } finally {
                setIsLoading(false);
            }
        };

        initAuth();
    }, []);


    useEffect(() => {
        const syncAuth = (event: StorageEvent) => {
            if (event.key === 'user_cache') {
                const newUser = event.newValue ? JSON.parse(event.newValue) : null;
                setUser(newUser);
            }
        };

        window.addEventListener('storage', syncAuth);
        return () => window.removeEventListener('storage', syncAuth);
    }, []);

    const authLogin = (userData: any) => {
        setUser(userData);
        localStorage.setItem('user_cache', JSON.stringify(filterUserData(userData)));
    };

    const authLogout = () => {
        setUser(null);
        localStorage.removeItem('user_cache');
    };

    return (
        <AuthContext.Provider value={{ user, isLoading, isAuthenticated: !!user, authLogout, authLogin }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = (): AuthContextType => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within <AuthProvider>");
  }
  return ctx;
};