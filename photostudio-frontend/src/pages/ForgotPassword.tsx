import { Link } from "react-router-dom"
import { useState } from 'react';
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { AuthCard } from "../components/AuthCard";
import LeftLogoSide from "../components/LeftLogoSide";
import { handleForgotPassword } from "@/api/ApiUserController";

const CONST_EMAIL = [
    { name: "email", type: "text", placeholder: "Email" },
];

function ForgotPassword() {
     const navigate = useNavigate();

     const [errorMessage, setErrorMessage] = useState('');
     const [message, setMessage] = useState('');

     const handleAttempt = async (formData: Record<string, string>) => {
        setErrorMessage('')
        setMessage('')

        const email = formData.email;

        try {
            await handleForgotPassword(email)
            setErrorMessage('');
            setMessage('Link został wysłany na email')

            setTimeout(() => {
                navigate('/login');
            }, 3000);

        } catch (error) {
            setMessage('')
            if (axios.isAxiosError(error) && error.response && error.response.status === 404) {
                    setErrorMessage("Podany email nie istnieje");
            } else {
                    setErrorMessage("Spróbuj ponownie później");
            }
        }
     };
     return (
            <div className="min-h-screen flex flex-col lg:flex-row w-full items-center gap-8 justify-center lg:justify-between mx-auto px-6 lg:max-w-[1500px]">
                <LeftLogoSide />
                    <div>
                            <AuthCard 
                                onSubmit={handleAttempt} 
                                error={errorMessage} 
                                message={message}
                                fields={CONST_EMAIL}
                                title="Podaj email do zmiany hasła"
                                submitText="Wyślij link"
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

export default ForgotPassword