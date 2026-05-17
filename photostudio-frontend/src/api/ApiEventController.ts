import apiClient from "@/api/ApiClient";
import type { EventStatus, EventType } from "@/lib/constants";

const API_BASE = "/api/event";

interface FilterParams {
  page: number;
  pageSize: number;
  search: string | undefined;
  location: string | undefined;
  status: EventStatus | undefined;
  type: EventType | undefined;
  onlyWithFreeSpots: boolean;
  dateFrom: string | undefined;
  dateTo: string | undefined;
}


  export const getAllUsersAssignedToEvent = async (eventId: string) => {
    try {
      const response = await apiClient.get(`${API_BASE}/${eventId}/users`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
  export const getAllFutureEvents = async () => {
    try {
      const today = new Date();
      const start = today.toISOString().split("T")[0];
      const maxDate = new Date(2999, 11, 31);
      const end = maxDate.toISOString().split("T")[0];

      const response = await apiClient.get(`${API_BASE}/by-date-range`, {
        params: { start, end },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }
  export const getFilteredEvents = async (params: FilterParams) => {
    try {
      const response = await apiClient.get(`${API_BASE}/filter`, { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  }
  export const getAllEvents = async () => {
    try {
      const minDate = new Date(2000, 0, 1);
      const maxDate = new Date(2999, 11, 31);
      const start = minDate.toISOString().split("T")[0];
      const end = maxDate.toISOString().split("T")[0];

      const response = await apiClient.get(`${API_BASE}/by-date-range`, {
        params: { start, end },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }
  export const updateEvent = async (eventId: number, eventData: any) => {
    try {
      await apiClient.put(`${API_BASE}/${eventId}`, eventData);
    } catch (error) {
      throw error;
    }
  }
  export const completeEvent = async (eventId: number) => {
    try {
      await apiClient.patch(`${API_BASE}/${eventId}/complete`);
    } catch (error) {
      throw error;
    }
  }
  export const deleteEvent = async (eventId: number) => {
    try {
      await apiClient.delete(`${API_BASE}/${eventId}`);
    } catch (error) {
      throw error;
    }
  }
  export const echangeEventStatus = async (eventId: number, status: string) => {
    try {
      await apiClient.patch(`${API_BASE}/${eventId}/status`, null, {
        params: { status },
      });
    } catch (error) {
      throw error;
    }
  }

  export const createEvent = async (
    date: string,
    time: string,
    name: string,
    description: string,
    location: string,
    numberOfPeopleRequired: number,
    type: EventType,
  ) => {
    const data = {
      date,
      time,
      name,
      description,
      location,
      numberOfPeopleRequired,
      type: String(type),
    };

    try {
      const formattedTime = data.time ? data.time + ":00" : data.time;

      const response = await apiClient.post("/api/event", {
        ...data,
        time: formattedTime,
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

