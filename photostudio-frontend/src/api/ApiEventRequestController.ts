import apiClient from '@/api/ApiClient';
import type { EventRequestStatus} from '@/lib/constants';

const API_BASE = '/api/event/request';

interface ApiRequestInput {
    search?: string
    status?: EventRequestStatus
    dateFrom?: string
    dateTo?: string
    page: number
    pageSize: number
}

interface ApiRequestForAllInput {
    search?: string
    name?: string
    status?: EventRequestStatus
    dateFrom?: string
    dateTo?: string
    page: number
    pageSize: number
}


    export const createOwnEventRequest = async (eventId: string) => {
        try {
            const response = await apiClient.post(`${API_BASE}/events/${eventId}`);
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    export const approveEventRequest = async (eventId: string, eventRequestId: string, userId: string) => {
    try {
        const response = await apiClient.patch(`/api/event/${eventId}/request/${eventRequestId}/user/${userId}/approve`);
        return response.data;
    } catch (error) {
        throw error;
    }
    }
    export const rejectEventRequest = async (eventId: string, eventRequestId: string, userId: string) => {
        try {
            const response = await apiClient.delete(`/api/event/${eventId}/request/${eventRequestId}/user/${userId}`); 
            return response.data;
        } catch (error) {
            throw error;
        }
    }
    export const cancelOwnEventRequest = async (eventRequestId: string) => {
        try {
            const response = await apiClient.patch(`${API_BASE}/${eventRequestId}/cancel`);
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    export const getFilteredRequests = async (params: ApiRequestInput) => {
        try {
            const response = await apiClient.get(`${API_BASE}/filter`, {params});
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    export const getAllFilteredRequests = async (params: ApiRequestForAllInput) => {
        try {
            const response = await apiClient.get(`${API_BASE}/filter/all`, {params});
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    export const getAllActiveEventRequests = async () => {
        try {
            const response = await apiClient.get(`${API_BASE}/active`);
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    export const userAlreadyRequested = async (eventId: string) => {
        try {
            const response = await apiClient.get(`${API_BASE}/exist` , { params: { eventId } });
            return response.data;
        } catch (error) {
            throw error;
        }
    }


