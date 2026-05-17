import apiClient from './ApiClient';

export const checkUserStatus = async () => {

    try {
        const response = await apiClient.get('/api/user/me');
            return response.data;
    } catch (error) {
        throw error;
    }
};