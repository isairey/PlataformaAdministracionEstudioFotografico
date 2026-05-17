import { useState } from 'react';
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { buttonStyles } from '@/styles/common-styles';
import { DatePickerTime } from '../CalendarPick';
import type {EventType } from '@/api/api.types';

const eventTypes: EventType[] = ['KWF', 'SKN', 'WRSS', 'URSS', 'AGH', 'AKRE', 'CM', 'AKT', 'PRIVATE'];

type FormData = {
    date: string;
    time: string;
    name: string;
    description: string;
    location: string;
    numberOfPeopleRequired: number;
    type: EventType | '';
};

export default function CreateEventForm({ onSubmit }: { onSubmit: (formData: FormData) => void }) {

    const [data, setData] = useState<FormData>({
        date: '',
        time: '',
        name: '',
        description: '',
        location: '',
        numberOfPeopleRequired: 0,
        type: ''
    });

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const Name = e.target.name;
        const Value = e.target.value;
        setData({
            ...data,
            [Name]: Value
        });
    }
    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        onSubmit?.(data)

        setData({
            date: '',
            time: '',
            name: '',
            description: '',
            location: '',
            numberOfPeopleRequired: 0,
            type: ''
        });
  }

    return (
        <Card className='max-w-3xl'>
            <CardHeader>
                <CardTitle>Utwórz nowe wydarzenie</CardTitle>
                <CardDescription>Dodaj szczegóły dotyczące wydarzenia w opisie. Pamiętaj aby podać datę rozpoczęcia wydarzenia. W przypadku
                    braku informacji na jej temat prosimy o poinformowanie o tym w opisie.
                </CardDescription>
            </CardHeader>
            <form onSubmit={handleSubmit}>
                <CardContent>
                    <div className="flex flex-col gap-4">
                        <div className="grid gap-2">
                            <Label>Nazwa wydarzenia</Label>
                            <Input
                                name="name"
                                type="text"
                                placeholder="Michał Leja - Z KARTKI"
                                value={data.name}
                                onChange={handleInputChange}    
                                required
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label>Lokalizacja</Label>
                            <Input
                                name="location"
                                type="text"
                                placeholder="Klub Studio"
                                value={data.location}
                                onChange={handleInputChange}    
                                required
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label>Opis / Informacje dodatkowe</Label>
                            <Input
                                name="description"
                                type="text"
                                placeholder="Dostaliśmy prośbę by fotografowie pojawili się 10 minut wcześniej"
                                value={data.description}
                                onChange={handleInputChange}    
                            />
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="grid gap-2">
                                <DatePickerTime 
                                onChange={(newDate, newTime) => {
                                    setData({
                                    ...data,
                                    date: newDate ? newDate.toISOString().split('T')[0] : '',
                                    time: newTime
                                    });
                                }}
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label>Liczba osób</Label>
                                <Input
                                name="numberOfPeopleRequired"
                                type="number"
                                placeholder="2"
                                className="w-full"
                                value={data.numberOfPeopleRequired}
                                onChange={handleInputChange}  
                                required  
                                />
                            </div>
                            </div>
                        <div className="flex flex-wrap gap-2 justify-center md:justify-start">
                            {eventTypes.map((typeEvent) => (
                                <Button
                                key={typeEvent}
                                type="button"
                                size="sm"
                                variant={data.type === typeEvent ? "default" : "outline"}
                                onClick={() => { setData({ ...data, type: typeEvent }) }}
                                className="px-3"
                                >
                                {typeEvent}
                                </Button>
                            ))}
                        </div>
                    </div>
                </CardContent>
                <CardFooter className='pt-4'>
                    <Button type="submit" className={buttonStyles.primaryButton}>
                        Dodaj wydarzenie
                    </Button>
                </CardFooter>
            </form>
        </Card>
    )
}

