import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { baseColors } from "@/styles/common-styles"
import { toast } from "sonner"

interface Field {
  name: string
  type: string
  placeholder: string
}

interface AuthCardProps {
  onSubmit?: (formData: Record<string, string>) => void
  error?: string
  message?: string
  fields: Field[]
  title: string
  submitText: string
}

export function AuthCard({ onSubmit, error, message, fields, title, submitText }: AuthCardProps) {
  const [formData, setFormData] = useState<Record<string, string>>(() =>
    fields.reduce<Record<string, string>>((acc, field) => {
      acc[field.name] = ""
      return acc
    }, {})
  )

  useEffect(() => {
    if (error && error.length > 0) {
      toast.error(error, {
        style: { color: baseColors.failureColor }
      })
    }
  }, [error])

  useEffect(() => {
    if (message && message.length > 0) {
      toast.success(message, {
        style: { color: baseColors.successColor }
      })
    }
  }, [message])

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    onSubmit?.(formData)
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  return (
    <div className="w-[350px]">
      <Card>
        <CardHeader>
          <CardTitle className="text-m text-center font-normal">{title}</CardTitle>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="flex flex-col gap-4">
            {fields.map((field) => (
              <Input
                key={field.name}
                type={field.type}
                name={field.name}
                placeholder={field.placeholder}
                value={formData[field.name]}
                onChange={handleChange}
              />
            ))}
          </CardContent>
          <CardFooter className="pt-4">
            <Button type="submit" className='bg-main-site text-white w-full'>
              {submitText}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}