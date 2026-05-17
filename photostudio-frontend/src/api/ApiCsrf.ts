import apiClient from './ApiClient';

export const initializeCsrf = async () => {
    await apiClient.get('/api/csrf');
};
