"use client";

import {createEvent} from '@/api/ApiEventController';
import CreateEventForm from '@/components/event_components/CreateEventCard';
import { toast } from "sonner";
import type {EventType } from '@/api/api.types';
import { baseColors } from "@/styles/common-styles";
import LeftLogoSide from '@/components/LeftLogoSide';

type FormData = {
  date: string;
  time: string;
  name: string;
  description: string;
  location: string;
  numberOfPeopleRequired: number;
  type: EventType | '';
};

const AddEventForm = () => {

  const handleSubmit = async (data : FormData) => {
    if (!data.type) return;

    try {
      await createEvent(
        data.date,
        data.time,
        data.name,
        data.description,
        data.location,
        data.numberOfPeopleRequired,
        data.type
      );
      toast.success('Wydarzenie poprawnie utworzone!.', {style: {color: baseColors.successColor}});

    } catch (error) {
      console.log(error)
      toast.error("Błąd w czasie tworzenia wydarzenia. Spróbuj ponownie.", { style: { color: baseColors.failureColor } });
    }
  };

  return (
    <>
      <div className="min-h-screen flex flex-col lg:flex-row w-full items-center justify-center lg:justify-between mx-auto px-6 lg:max-w-[1500px]">
        <div className="hidden lg:block">
          <LeftLogoSide />
        </div>
        <CreateEventForm onSubmit={handleSubmit}/>
      </div>
    </>
  );
};

export default AddEventForm;