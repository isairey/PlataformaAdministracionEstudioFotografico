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

export default function RegistrationForm({ onSubmit }: { onSubmit: (formData: Record<string, string>) => void }) {

    const [data, setData] = useState({
        name: '',
        surname: '',
        email: '',
        password: '',
        confirmPassword: '',
        username: '',
        phoneNumber: ''
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
  }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Utwórz nowe konto</CardTitle>
                <CardDescription>Zarejestruj swoje konto w agencji fotograficznej. Wymagane jest użycie domenowego adresu email.</CardDescription>
            </CardHeader>
            <form onSubmit={handleSubmit}>
                <CardContent>
                    <div className="flex flex-col gap-4">
                        <div className="flex gap-2">
                            <div className="grid gap-2">
                                <Label>Imię</Label>
                                <Input
                                    name="name"
                                    type="text"
                                    placeholder="Anna"
                                    value={data.name}
                                    onChange={handleInputChange}    
                                    required
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label>Nazwisko</Label>
                                <Input
                                    name="surname"
                                    type="text"
                                    placeholder="Kowalska"
                                    value={data.surname}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                        </div>
                        <div className="grid gap-2">
                            <Label>Email</Label>
                            <Input
                                name="email"
                                type="email"
                                placeholder="example@pas.pl"
                                value={data.email}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label>Numer telefonu</Label>
                            <Input
                                name="phoneNumber"
                                type="tel"
                                placeholder="+48513679132"
                                value={data.phoneNumber}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label>Nazwa użytkownika</Label>
                            <Input
                                name="username"
                                type="text"
                                placeholder="annKowalska"
                                value={data.username}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="flex gap-2">
                            <div className="grid gap-2">
                                <Label>Hasło</Label>
                                <Input
                                    name="password"
                                    type="password"
                                    placeholder="Hasło"
                                    value={data.password}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label>Potwierdź hasło</Label>
                                <Input
                                    name="confirmPassword"
                                    type="password"
                                    placeholder="Potwierdź hasło"
                                    value={data.confirmPassword}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                        </div>
                    </div>
                </CardContent>
                <CardFooter className='pt-4'>
                    <Button type="submit" className={buttonStyles.primaryButton}>
                        Zarejestruj się
                    </Button>
                </CardFooter>
            </form>
        </Card>
    )
}
