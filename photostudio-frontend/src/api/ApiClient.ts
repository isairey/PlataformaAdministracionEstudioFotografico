import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true,
});

function getCookie(name: string): string | null {
    const match = document.cookie
        .split('; ')
        .find(row => row.startsWith(name + '='));
    return match ? decodeURIComponent(match.split('=')[1]) : null;
}

apiClient.interceptors.request.use(config => {
    const method = config.method?.toUpperCase();
    const mutatingMethods = ['POST', 'PUT', 'DELETE', 'PATCH'];

    if (mutatingMethods.includes(method ?? '')) {
        const token = getCookie('XSRF-TOKEN');
        if (token) {
            config.headers['X-XSRF-TOKEN'] = token;
        }
    }

    return config;
});

export default apiClient;