import { useState } from 'react';
import { Link } from "react-router-dom"
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

import LeftLogoSide from "../components/LeftLogoSide";
import { handleConfirmation } from '@/api/ApiUserController';
import { AuthCard } from '../components/AuthCard';

function ConfirmAccount() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [message, setMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const token = searchParams.get('token');

    const confirmEmail = async () => {

        if (!token) {
            setErrorMessage('Nieprawidłowy link potwierdzenia');
        } else {
            try {
                await handleConfirmation(token);
                setMessage('Przekierowuję na logowanie...');
                setErrorMessage('');

                setTimeout(() => {
                    navigate('/login');
                }, 3000);

            } catch (error) {

                if (axios.isAxiosError(error) && error.response?.status === 400) {
                    setErrorMessage('Link wygasł lub jest nieprawidłowy');
                } else {
                    setErrorMessage('Błąd potwierdzenia. Spróbuj ponownie później');
                }
                setMessage('');

                setTimeout(() => {
                    navigate('/login');
                }, 3000);
            }
        }
    };

    return (
       <div className="min-h-screen flex flex-col lg:flex-row w-full items-center gap-8 justify-center lg:justify-between mx-auto px-6 lg:max-w-[1500px]">
            <LeftLogoSide />
                <div>
                    <AuthCard  
                        onSubmit={confirmEmail} 
                        error={errorMessage}
                        message={message}
                        fields={[]}
                        title="Potwierdź swój adres email"
                        submitText="Potwierdź konto"
                    />
                    <p className="mt-4 text-sm text-gray-600">
                        Masz już konto?{" "}
                        <Link to="/login" className="text-gray-600 hover:underline">
                            Zaloguj się
                        </Link>
                    </p>
                    
                </div>
        </div>
    );
}

export default ConfirmAccount