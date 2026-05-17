"use client";

import {createEquipment} from "@/api/ApiEquipmentController";
import { toast } from "sonner";
import { baseColors } from "@/styles/common-styles";
import LeftLogoSide from '@/components/LeftLogoSide';
import { CreateEquipmentCard } from '@/components/CreateEquipmentCard';
import { type Equipment } from '@/api/api.types';


const AddEquipmentForm = () => {

  const handleSubmit = async (data : Pick<Equipment, "name" | "activeMembers" | "statutoryEvent" | "equipmentCategory">) => {
    if (!data.equipmentCategory) return;

    try {
      await createEquipment(data.name, data.activeMembers, data.statutoryEvent, data.equipmentCategory
      );
      toast.success('Wyposażenie poprawnie utworzone!.', {style: {color: baseColors.successColor}});

    } catch (error) {
      console.log(error)
      toast.error("Błąd w czasie tworzenia wyposażenia. Spróbuj ponownie.", { style: { color: baseColors.failureColor } });
    }
  };

  return (
    <>
      <div className="min-h-screen flex flex-col lg:flex-row w-full items-center gap-8 justify-center lg:justify-between mx-auto px-6 lg:max-w-[1500px]">
               <div className="hidden lg:block">
          <LeftLogoSide />
        </div>
        <CreateEquipmentCard onSubmit={handleSubmit}/>
      </div>
    </>
  );
};

export default AddEquipmentForm;