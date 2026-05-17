import { Link } from "react-router-dom"
import { useNavigate, useSearchParams } from "react-router-dom";
import { useState } from 'react';
import axios from "axios";

import { AuthCard } from "../components/AuthCard";
import { resetPasswordApi } from "@/api/ApiUserController";
import LeftLogoSide from "../components/LeftLogoSide";

const CONST_PASSWORD = [
    { name: "newPassword", type: "password", placeholder: "Hasło" },
     { name: "confirmNewPassword", type: "password", placeholder: "Potwierdź hasło" },
];

function ResetPassword() {
     const navigate = useNavigate();
     const [searchParams] = useSearchParams();
     const token = searchParams.get('token');

     const [errorMessage, setErrorMessage] = useState(''); 
     const [message, setMessage] = useState('')

     const handleAttempt = async (formData: Record<string, string>) => {
            const newPassword = formData.newPassword;
            const confirmNewPassword = formData.confirmNewPassword;
          if (!token) {
               setErrorMessage('Nieprawidłowy link');
          } else {
               try {
                    await resetPasswordApi(token, newPassword, confirmNewPassword )

                    setMessage('Poprawnie zmieniono hasło');
                    setErrorMessage('');
                    
                    setTimeout(() => {
                         navigate('/login');
                    }, 1000);

               } catch (error) {

                    if (axios.isAxiosError(error) && error.response && error.response.status === 400) {
                         setErrorMessage("Hasła muszą być identyczne, zawierać minimum 8 znaków, w tym wielką i małą literę, cyfrę oraz znak specjalny");
                    } else {
                         setErrorMessage("Token jest nieważny");
                    }
                    setMessage('')
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
                                   fields={CONST_PASSWORD}
                                   title="Wpisz nowe hasło"
                                   submitText="Zmień hasło"
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

export default ResetPassword