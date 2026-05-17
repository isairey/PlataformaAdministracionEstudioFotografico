import { Link } from "react-router-dom"
import { useNavigate } from "react-router-dom";
import { useState } from 'react';
import axios from "axios";


import { AuthCard } from "../components/AuthCard";
import LeftLogoSide from "../components/LeftLogoSide";

import { useAuth } from '../context/AuthContext';
import { filterUserData } from "../utils/FilterUser";
import { handleLogin, getMe} from "@/api/ApiUserController";
import apiClient from "@/api/ApiClient";

const LOGIN_FIELDS_CONFIG = [
     { name: "login", type: "email", placeholder: "Email" },
     { name: "password", type: "password", placeholder: "Hasło" },
     ];

async function initCsrf() {
    await apiClient.get('/api/csrf');
}

function Login() {
     const navigate = useNavigate();
     const auth = useAuth();
     const authLogin = auth?.authLogin;

     const [errorMessage, setErrorMessage] = useState(''); 
     const [message, setMessage] = useState('');

     const handleLoginAttempt = async (formData: Record<string, string>) => {
          setErrorMessage('')
          setMessage('')

          try {
               initCsrf();
               const login = formData.login;
               const password = formData.password;
               await handleLogin(login, password);
               setMessage('Poprawne logowanie');
               setErrorMessage('');
               const userData = await getMe();

               if (authLogin) {
                    authLogin(filterUserData(userData));
               } else {
                    console.error('authLogin is undefined');
               }

               navigate('/home', { replace: true });

          } catch (error) {
               setMessage('')

               if (axios.isAxiosError(error) && error.response && error.response.status === 401) {
                    setErrorMessage("Nieprawidłowa nazwa użytkownika lub hasło.");
               } else {
                    setErrorMessage("Spróbuj ponownie później")
               }
          }
     };
     return (
          <div className="min-h-screen flex flex-col lg:flex-row w-full items-center gap-8 justify-center lg:justify-between mx-auto px-6 lg:max-w-[1500px]">
               <LeftLogoSide />
               <div>
                         <AuthCard 
                              onSubmit={handleLoginAttempt} 
                              error={errorMessage} 
                              message={message}
                              fields={LOGIN_FIELDS_CONFIG}
                              title="Zaloguj się do swojego konta"
                              submitText="Zaloguj się"
                         />
                         <p className="mt-4 text-sm text-gray-600">
                         Nie masz konta?{' '}
                         <Link to="/register" className="text-gray-600 hover:underline">
                              Zarejestruj się
                         </Link>
                         </p>
                         <p className="mt-2 text-sm text-gray-600">
                         Zapomniałeś hasła?{' '}
                         <Link to="/forgot-password" className="text-gray-600 hover:underline">
                              Zmień hasło
                         </Link>
                    </p>
               </div>
          </div>
     );
}

export default Login