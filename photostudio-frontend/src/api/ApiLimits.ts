import apiClient from "./ApiClient";
import type { EquipmentCategory } from "@/lib/constants";

export type UserLimitsResponse = Partial<Record<EquipmentCategory, number>>;

export const getUserLimits = async (userId: number) => {
  const response = await apiClient.get<UserLimitsResponse>(`/api/user/limits/${userId}`);
  return response.data;
};

export const changeUserLimit = async (userId: number, category: EquipmentCategory, newLimit: number) => {
  const response = await apiClient.patch(`/api/user/limits/${userId}`, null, {
    params: {
      newLimit,
      category,
    },
  });

  return response.data;
};
