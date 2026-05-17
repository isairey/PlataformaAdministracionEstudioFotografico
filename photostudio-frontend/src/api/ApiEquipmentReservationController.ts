import apiClient from "@/api/ApiClient";
import type { ReservationStatus } from "@/lib/constants";

const API_BASE = 'api/equipment/reservation';


  export const createEquipmentReservation = async (
    eventRequestId: number,
    start: string,
    end: string,
    equipmentIDs: number[],
    comment: string,
    isPrivate: boolean,
    isUrgent: boolean
  ) => {
    const data = {
      eventRequestId,
      start,
      end,
      equipmentIDs,
      comment,
      isPrivate,
      isUrgent

    };
    try {
      const response = await apiClient.post(API_BASE, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const cancelEquipmentReservation = async (reservationId: string) => {
    try {
      const response = await apiClient.patch(
        `${API_BASE}/${reservationId}/cancel`
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const resolveEquipmentReservation = async (reservationId: string, acceptanceList: Record<string, boolean>) => {
    try {
      const response = await apiClient.put(
        `${API_BASE}/${reservationId}/resolve`,
        acceptanceList
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const modifyEquipmentReservation = async (reservationId: string, acceptanceList: Record<string, boolean>) => {
    try {
      const response = await apiClient.patch(
        `${API_BASE}/${reservationId}/modify`,
        acceptanceList
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  //verify if usefull
  export const makeReservationPending = async (reservationId: string) => {
  try {
    const response = await apiClient.patch(
      `${API_BASE}/${reservationId}/pending`
    );
    return response.data;
  } catch (error) {
    throw error;
  } }


  export const approveSingleReservationItem = async (reservationItemId: string) => {
    try {
      const response = await apiClient.patch(
        `${API_BASE}/item/${reservationItemId}/approve`
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const rejectSingleReservationItem = async (reservationItemId: string) => {
    try {
      const response = await apiClient.patch(
        `${API_BASE}/item/${reservationItemId}/reject`
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const cancelSingleReservationItem = async (reservationItemId: string) => {
    try {
      const response = await apiClient.patch(
        `${API_BASE}/item/${reservationItemId}/cancel`
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getReservationItems = async (reservationId: string) => {
    try {
      const response = await apiClient.get(
        `${API_BASE}/${reservationId}/items`
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getAllReservations = async () => {
    try {
      const response = await apiClient.get(`${API_BASE}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getAllReservationsWithinTimeWindow = async (
    startDate: string,
    endDate: string
  ) => {
    try {
      const response = await apiClient.get(`${API_BASE}/time`, {
        params: {
          startDate,
          endDate,
        },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getEquipmentReservationById = async (reservationId: string) => {
    try {
      const response = await apiClient.get(`${API_BASE}/${reservationId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const addItemToReservation = async (reservationId: string, equipmentId: string) => {
    const data = {
      equipmentId,
      reservationId,
    };
    try {
      const response = await apiClient.post(`${API_BASE}/item`, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const removeItemFromReservation =  async (
    reservationId: string,
    equipmentId: string
  ) => {
    const data = {
      equipmentId,
      reservationId,
    };
    try {
      const response = await apiClient.patch(`${API_BASE}/item`, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getAvailableEquipmentForTimeWindow = async (
    start: string,
    end: string,
    statutory: boolean
  ) => {
    try {
      const response = await apiClient.get(`${API_BASE}/available`, {
        params: {
          start,
          end,
          statutory
        },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getUserReservations = async (userId: string) => {
    try {
      const response = await apiClient.get(`${API_BASE}/user/${userId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }



  export const getUserReservationsWithinTimeWindow = async (
    userId: string,
    startDate: string,
    endDate: string
  ) => {
    try {
      const response = await apiClient.get(`${API_BASE}/user/${userId}/time`, {
        params: {
          startDate,
          endDate,
        },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getPageByFilters = async (
    creatorFullName: string,
    eventName: string,
    status: ReservationStatus | '',
    startDate: string,
    endDate: string,
    pageNo: number,
    pageSize: number
  ) => {
    try {
      const response = await apiClient.get(`${API_BASE}/filtered`, {
        params: {
          creatorFullName,
          eventName,
          status,
          startDate,
          endDate,
          pageNo,
          pageSize,
        },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  export const getPageByFiltersForUser = async (
    userId: string,
    creatorFullName: string,
    eventName: string,
    status: ReservationStatus | '',
    startDate: string,
    endDate: string,
    pageNo: number,
    pageSize: number
  ) => {
    try {
      const response = await apiClient.get(`${API_BASE}/user/${userId}/filtered`, {
        params: {
          creatorFullName,
          eventName,
          status,
          startDate,
          endDate,
          pageNo,
          pageSize,
        },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }



