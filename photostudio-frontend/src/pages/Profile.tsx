"use client";

import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Edit } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"

import { Button } from "@/components/ui/button"
import { baseColors, loaderStyles } from "@/styles/common-styles";


import { updateUser, getMe } from "@/api/ApiUserController";
import {type User} from "@/api/api.types"

function Profile() {
  const [userData, setUserData] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  async function handleSaveUser() {
    if (!isEditing) return;
    try {
      if (userData != null){
      await updateUser({
        name: userData?.name,
        surname: userData?.surname,
        phoneNumber: userData?.phoneNumber,
      });
      toast.success("Pomyślnie zaktualizowano użytkownika.", {
        style: { color: baseColors.successColor },
      });
    } else {
      throw new Error("User data is null");
    }
    } catch (err) {
      console.error("Failed to edit user:", err);
      toast.error(
        "Nie udało się zaktualizować użytkownika. Spróbuj ponownie później.",
        { style: { color: baseColors.failureColor } },
      );
    }
  }

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const user = await getMe();
        setUserData(user);
      } catch (error) {
        console.error("Failed to fetch user data:", error);
        toast.error("Nie udało się załadować danych.", {
          style: { color: baseColors.failureColor },
        });
      } finally {
        setIsLoading(false);
      }
    };

    fetchUser();
  }, []);

  if (isLoading) {
    return (
      <div>
        <div className="h-screen flex items-center justify-center">
          <Loader2 className={loaderStyles.mediumLoader} />
        </div>
      </div>
    );
  }
  const roleLabels: Record<string, string> = {
    ADMIN: "Administrator",
    USER: "Użytkownik",
    MODERATOR: "Moderator",
  };
  return (
    <div>
      <div className="flex flex-col p-8">
        <div className="max-w-4xl mx-auto w-full flex flex-col">
          <div className="space-y-8">
            {userData != null && (
              <div className="max-w-xl mx-auto p-6 bg-white rounded-lg shadow">
                <div className="w-full max-w-[520px] mx-auto">
                  <h2 className="text-xl font-semibold mb-6 text-center">Twoje dane</h2>
                  <div className="grid grid-cols-2 gap-y-5 gap-x-4 text-sm">
                    <span className="font-medium text-gray-600">Imię</span>
                    <span className="break-words">{userData.name}</span>
                    <span className="font-medium text-gray-600">Nazwisko</span>
                    <span className="break-words">{userData.surname}</span>
                    <span className="font-medium text-gray-600">Email</span>
                    <span className="break-words">{userData.email}</span>
                    <span className="font-medium text-gray-600">Nazwa</span>
                    <span className="break-words">{userData.username}</span>
                    <span className="font-medium text-gray-600">
                      Numer Telefonu
                    </span>
                    <span>{userData.phoneNumber}</span>
                    <span className="font-medium text-gray-600">Rola</span>
                    <span className="text-black">{roleLabels[userData.role] ?? userData.role}</span>
                  </div>
                  <Button className="w-full mt-6 gap-2" onClick={() => setIsEditing(true)}>
                    <Edit className="h-4 w-4" />
                    <span>Edytuj</span>
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
      {isEditing && userData && (
        <Dialog open={isEditing} onOpenChange={(open) => setIsEditing(open)}>
          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Edycja użytkownika</DialogTitle>
              <DialogDescription>
                Edycja: {userData.name} {userData.surname} ({userData.username})
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4 max-h-[60vh] overflow-y-auto">
              <div className="grid gap-2">
                <Label>Imię</Label>
                <Input
                  value={userData.name}
                  onChange={(e) => setUserData({ ...userData, name: e.target.value })}
                />
              </div>
              <div className="grid gap-2">
                <Label>Nazwisko</Label>
                <Input
                  value={userData.surname}
                  onChange={(e) => setUserData({ ...userData, surname: e.target.value })}
                />
              </div>
              <div className="grid gap-2">
                <Label>Email</Label>
                <Input value={userData.email} disabled />
              </div>
              <div className="grid gap-2">
                <Label>Nazwa</Label>
                <Input value={userData.username} disabled />
              </div>
              <div className="grid gap-2">
                <Label>Numer Telefonu</Label>
                <Input
                  value={userData.phoneNumber}
                  onChange={(e) => setUserData({ ...userData, phoneNumber: e.target.value })}
                />
              </div>
            </div>
              <Button variant="outline" onClick={() => setIsEditing(false)}>
                Anuluj
              </Button>
              <Button onClick={() => {
                // Save logic here
                setIsEditing(false);
                handleSaveUser();
                
              }}>
                Zapisz
              </Button>
          </DialogContent>
        </Dialog>
      )}
    </div>
    
  );
}

export default Profile;
