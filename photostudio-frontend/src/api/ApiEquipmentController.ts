import type { Equipment } from "./api.types";
import type { EquipmentCategory } from '@/lib/constants';
import apiClient from "./ApiClient";
const API_BASE = "api/equipment";

export interface EquipmentPage {
  content: Equipment[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

  export const getEquipmentPage = async (
    active: boolean,
    statutory: boolean,
    name: string,
    category: string,
    pageNo: number,
    size: number,
  ) => {
    const params = new URLSearchParams({
      active: active.toString(),
      statutory: statutory.toString(),
      name,
      category,
      pageNo: pageNo.toString(),
      size: size.toString(),
    });
    const response = await apiClient.get(`${API_BASE}?${params}`);
    return response.data;
  }

  export const getEquipmentById = async (id: string) => {
    try {
      const response = await apiClient.get(`${API_BASE}/${id}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const modifyEquipment = async (
    id: string,
    name: string,
    activeMembers: boolean,
    statutoryEvent: boolean,
    equipmentCategory: string,
  ) => {
    try {
      const response = await apiClient.put(`${API_BASE}/${id}`, {
        name,
        activeMembers,
        statutoryEvent,
        equipmentCategory,
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const deleteEquipment = async (id: string) => {
    try {
      await apiClient.delete(`${API_BASE}/${id}`);
    } catch (error) {
      throw error;
    }
  }
  export const createEquipment = async (
    name: string,
    activeMembers: boolean,
    statutoryEvent: boolean,
    equipmentCategory: EquipmentCategory,
  ) => {
    try {
      await apiClient.post("/api/equipment", {
        name,
        activeMembers,
        statutoryEvent,
        equipmentCategory,
      });
    } catch (error) {
      throw error;
    }
  }
