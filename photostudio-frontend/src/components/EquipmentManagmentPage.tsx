import { useNavigate } from "react-router-dom";
import { Button } from '@/components/ui/button';
import EquipmentList from "@/components/equipment_management_components/EquipmentList";
export default function EquipmentManagmentPage() {

    const navigate = useNavigate();
return (
<div className="flex-1 flex flex-col min-h-0">
        <div className="flex flex-col flex-1 min-h-0 max-w-6xl w-full mx-auto p-6">
            <div className="flex flex-col gap-4 [@media(min-width:700px)]:flex-row [@media(min-width:700px)]:justify-between justify-center items-center [@media(min-width:700px)]:items-start p-4">
                <h1 className="text-2xl font-semibold">Zarządzanie Wyposażeniem</h1>
                <Button
                    className="w-full max-w-xs bg-main-site text-white hover:bg-black/90 sm"
                    onClick={() => {
                    navigate(`/create-equipment`);
                    }}
                >
                    Dodaj Wyposażenie
                </Button>
            </div>
            <div className="flex gap-6 flex-1 overflow-y-auto scrollbar-hide">
                <div className="flex-1"><EquipmentList /></div>
            </div>
        </div>
    </div>
    );
}
