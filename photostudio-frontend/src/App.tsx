import {Routes, Route} from "react-router-dom"
import { Toaster } from 'sonner'
import { AuthProvider } from './context/AuthContext';
import './App.css'

import { Footer } from './components/Footer'
import { ProtectedRoute } from './components/ProtectedRoute';
import { PublicRoute } from './components/PublicRoute';

import Login from './pages/Login'
import Home from "./pages/Home"
import Profile from "./pages/Profile"
import Event from './pages/Events'
import NotFoundPage from './pages/404'
import EventRequestsPage from './pages/EventRequests'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import ConfirmAccount from './pages/ConfirmAccount'
import EquipmentReservationPage from './pages/EquipmentReservationPage'
import AddReservationForm from "./pages/AddEquipmentReservationPage";
import Register from "./pages/Registration";
import ModeratrionPage from "./pages/ModeratorPage/ModeratorPage";
import AddEventForm from "./pages/AddEventForm";
import AddEquipmentForm from "./pages/CreateEquipment";
import ModeratorEquipmentReservationPage from "./pages/ModeratorEquipmentReservationPage";
import EquipmentManagmentPage from "./components/EquipmentManagmentPage";
import { ProtectedModeratorRoute } from "./components/ProtectedModeratorRoute";
import { ProtectedAdminRoute } from "./components/ProtectedAdminRoute";
import EventRequestsManagementPage from "./pages/EventRequestsManagement";

function App() {
    return (
        <AuthProvider>
            < Toaster />
            <div className="min-h-screen flex flex-col">
                <main className="flex-1 flex flex-col min-h-0">
                    <Routes>
                        <Route path='/login' element={<PublicRoute><Login /></PublicRoute>} />
                        <Route path='/' element={<PublicRoute><Login /></PublicRoute>} />
                        <Route path='/*' element={<PublicRoute><NotFoundPage /></PublicRoute>} />

                        <Route path='/home' element={<ProtectedRoute><Home /></ProtectedRoute>} />
                        <Route path='/profile' element={<ProtectedRoute><Profile /></ProtectedRoute>} />
                        <Route path='/events' element={<ProtectedRoute><Event /></ProtectedRoute>} />
                        <Route path='/event-requests' element={<ProtectedRoute><EventRequestsPage /></ProtectedRoute>} />
                        <Route path='/reservations' element={<ProtectedRoute><EquipmentReservationPage /></ProtectedRoute>} />
                        <Route path='/new-reservation' element={<ProtectedRoute><AddReservationForm /></ProtectedRoute>} />

                        <Route path='/forgot-password' element={<PublicRoute><ForgotPassword /></PublicRoute>} />
                        <Route path='/reset-password' element={<PublicRoute><ResetPassword /></PublicRoute>} />
                        <Route path='/auth/confirm' element={<PublicRoute><ConfirmAccount /></PublicRoute>} />
                        <Route path='/register' element={<PublicRoute><Register /></PublicRoute>} />

                        <Route path='/equipment-management' element={<ProtectedAdminRoute><EquipmentManagmentPage /></ProtectedAdminRoute>} />
                        <Route path='/create-equipment' element={<ProtectedAdminRoute><AddEquipmentForm /></ProtectedAdminRoute>} />
                        <Route path='/admin-panel' element={<ProtectedModeratorRoute><ModeratrionPage /></ProtectedModeratorRoute>} />

                        <Route path='/reservations-managment' element={<ProtectedModeratorRoute><ModeratorEquipmentReservationPage /></ProtectedModeratorRoute>} />
                        <Route path='/event-requests-management' element={<ProtectedModeratorRoute><EventRequestsManagementPage /></ProtectedModeratorRoute>} />
                        <Route path='/create-event' element={<ProtectedModeratorRoute><AddEventForm /></ProtectedModeratorRoute>} />

                    </Routes>
                </main>
                <Footer />
            </div>
        </AuthProvider>
    );
}


export default App
