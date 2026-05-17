import { Link } from "react-router-dom"
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "sonner";

import RegistrationForm from "@/components/RegistrationForm";
import LeftLogoSide from "../components/LeftLogoSide";
import { createUser } from "@/api/ApiUserController";
import { baseColors } from "@/styles/common-styles";

const ERROR_MESSAGES = {
    'Email already exists': 'Ten email jest już zarejestrowany',
    'Phone number already exists': 'Numer telefonu już istnieje',
    'Username already exists': 'Ta nazwa użytkownika już istnieje',
    'Passwords do not match': 'Hasła muszą być identyczne',
    'confirmPassword': 'Hasło musi mieć co najmniej 8 znaków, zawierać wielką i małą literę, cyfrę oraz znak specjalny',
    'password': 'Hasło musi mieć co najmniej 8 znaków, zawierać wielką i małą literę, cyfrę oraz znak specjalny',
    'username': 'Nieprawidłowy login użytkownika',
    'name': "Nieprawidłowe imię",
    'surname': "Nieprawidłowe nazwisko",
    'email': "Email musi należeć do domeny @pas.pl",
    'phoneNumber': 'Nieprawidłowy numer telefonu',
};

function Register() {
    const navigate = useNavigate();

    const handleAttempt = async (data: Record<string, string> ) => {

        try {
            await createUser(
                data.name,
                data.surname, 
                data.email, 
                data.password,
                data.confirmPassword,
                data.username,
                data.phoneNumber
            )
            toast.success('Konto utworzone! Potwierdź swój email.', {style: {color: baseColors.successColor}});
            
            setTimeout(() => {
                navigate('/login');
            }, 3000);

        } catch (error) {
            let messageToDisplay = 'Spróbuj ponownie później'; 

            if (axios.isAxiosError(error)) {
                if (error.response?.data?.message) {
                    const backendKey = error.response.data.message;
                    messageToDisplay = ERROR_MESSAGES[backendKey as keyof typeof ERROR_MESSAGES]  || 'Wystąpił nieznany błąd';
                } 
                // 3. Sprawdź pola (fields)
                else if (error.response?.data?.fields) {
                    const firstField = Object.keys(error.response.data.fields)[0];
                    messageToDisplay = ERROR_MESSAGES[firstField as keyof typeof ERROR_MESSAGES] || 'Błąd walidacji';
                }
            }
            toast.error(messageToDisplay, { style: { color: baseColors.failureColor } });
        }
    };
    return (
        <div className="min-h-screen flex flex-col lg:flex-row w-full items-center gap-8 justify-center lg:justify-between mx-auto px-6 lg:max-w-[1500px] my-10">
            <LeftLogoSide />
            <div>
                <RegistrationForm onSubmit={handleAttempt} />
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

export default Register;