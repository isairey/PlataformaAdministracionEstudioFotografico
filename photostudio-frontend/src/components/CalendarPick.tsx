"use client"

import * as React from "react"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { format } from "date-fns"
import { ChevronDownIcon } from "lucide-react"

export function DatePickerTime({ onChange }: { 
  onChange: (date: Date | undefined, time: string) => void 
}) {
  const [open, setOpen] = React.useState(false)
  const [date, setDate] = React.useState<Date | undefined>(undefined)
  const [time, setTime] = React.useState("10:30")

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const handleValuesChange = (newDate: Date | undefined, newTime: string) => {
    onChange(newDate, newTime);
  }

  return (
    <FieldGroup className="flex-row gap-4">
      <Field>
        <FieldLabel>Data</FieldLabel>
        <Popover open={open} onOpenChange={setOpen}>
          <PopoverTrigger asChild>
            <Button variant="outline" className="w-40 justify-between font-normal">
              {date ? format(date, "PPP") : "Wybierz datę"}
              <ChevronDownIcon className="h-4 w-4 opacity-50" />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0" align="start">
            <Calendar
              mode="single"
              selected={date}
              disabled={(d) => d < today}
              onSelect={(d) => {
                setDate(d);
                setOpen(false);
                handleValuesChange(d, time);
              }}
              initialFocus
            />
          </PopoverContent>
        </Popover>
      </Field>
      
      <Field className="w-32">
        <FieldLabel>Godzina</FieldLabel>
        <Input
          type="time"
          value={time}
          onChange={(e) => {
            const newTime = e.target.value;
            setTime(newTime);
            handleValuesChange(date, newTime);
          }}
        />
      </Field>
    </FieldGroup>
  )
}