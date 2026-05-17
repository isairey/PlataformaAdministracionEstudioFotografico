"use client"
import { useState } from "react"

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"

import { equipmentCategoriesLabels, type EquipmentCategory } from "@/lib/constants"
import { buttonStyles } from "@/styles/common-styles"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { type Equipment } from "@/api/api.types"



const equipmentCategories: EquipmentCategory[] = ['CAMERA', 'BATTERY', 'LENS', 'STUDIO', 'LIGHTING', 'TRIPOD', 'ACCESSORIES']

export function CreateEquipmentCard({ onSubmit }: { onSubmit: (formData: Pick<Equipment, "name" | "activeMembers" | "statutoryEvent" | "equipmentCategory">) => void }) {

    const [data, setData] = useState<Pick<Equipment, "name" | "activeMembers" | "statutoryEvent" | "equipmentCategory">>({
        name: '',
        activeMembers: false,
        statutoryEvent: false,
        equipmentCategory: 'CAMERA'
    });

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const Name = e.target.name;
        const Value = e.target.value;
        setData({
            ...data,
            [Name]: Value
        });
    }

    const handleNewEquipment = async (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(data);
    }

  return (
        <Card className='max-w-4xl'>
            <CardHeader>
                <CardTitle>Dodaj nowe wyposażenie</CardTitle>
                <CardDescription>Dodaj szczegóły dotyczące nowego wyposażenia. Pamiętaj aby podać nazwę, kategorię oraz informacje o aktywności członków i wydarzeniu statutowym.
                </CardDescription>
            </CardHeader>
            <form onSubmit={handleNewEquipment}>
                <CardContent>
                    <div className="flex flex-col gap-4">
                        <div className="grid gap-2">
                            <Label>Nazwa wyposażenia</Label>
                            <Input
                                name="name"
                                type="text"
                                placeholder="Zenit 12xp"
                                value={data.name}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="flex flex-col gap-4">
                            <div className="flex items-center gap-3">
                                <input
                                    id="activeMembers"
                                    name="activeMembers"
                                    type="checkbox"
                                    checked={data.activeMembers}
                                    onChange={(e) => setData({ ...data, activeMembers: e.target.checked })}
                                    className="w-4 h-4 cursor-pointer"
                                />
                                <Label htmlFor="activeMembers" className="cursor-pointer">
                                    Tylko dla członków aktywnych
                                </Label>
                            </div>
                            <div className="flex items-center gap-3">
                                <input
                                    id="statutoryEvent"
                                    name="statutoryEvent"
                                    type="checkbox"
                                    checked={data.statutoryEvent}
                                    onChange={(e) => setData({ ...data, statutoryEvent: e.target.checked })}
                                    className="w-4 h-4 cursor-pointer"
                                />
                                <Label htmlFor="statutoryEvent" className="cursor-pointer">
                                    Dla wydarzeń statutowych
                                </Label>
                            </div>
                        </div>
                        <div className="min-h-12 gap-2">
                            <div className="flex flex-wrap gap-2 justify-center md:justify-start">
                                {equipmentCategories.map((equipment) => (
                                    <Button
                                    key={equipment}
                                    type="button"
                                    size="sm"
                                    variant={data.equipmentCategory === equipment ? "default" : "outline"}
                                    onClick={() => { setData({ ...data, equipmentCategory: equipment }) }}
                                    className="px-3 box-border border-2 border-transparent "
                                    >
                                    {equipmentCategoriesLabels[equipment]}
                                    </Button>
                                ))}
                            </div>
                        </div>
                    </div> 
                </CardContent>
                <CardFooter className='pt-4'>
                    <Button type="submit" className={buttonStyles.primaryButton}>
                        Dodaj wyposażenie
                    </Button>
                </CardFooter>
            </form>
        </Card>
    )
}

