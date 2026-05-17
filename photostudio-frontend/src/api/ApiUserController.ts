import apiClient from "./ApiClient";

const API_BASE = "/api/user";

// Get own full user profile
export const getMe = async () => {
  try {
    const res = await apiClient.get(`${API_BASE}/me`);
    return res.data;
  } catch (error) {
    // Optionally log or process error here
    throw error;
  }
};

// Get basic user by ID
export const getBasicUserById = async (id: string) => {
  try {
    const res = await apiClient.get(`${API_BASE}/${id}`);
    return res.data;
  } catch (error) {
    throw error;
  }
};

// Get all users (admin/mod only)
export const getAllUsers = async () => {
  try {
    const res = await apiClient.get(`${API_BASE}/all`);
    return res.data;
  } catch (error) {
    throw error;
  }
};

// Get users by role
export const getAllUsersByRole = async (role: string) => {
  try {
    const res = await apiClient.get(`${API_BASE}/by-role`, {
      params: { role },
    });
    return res.data;
  } catch (error) {
    throw error;
  }
};

// Get user by username
export const getUserByUsername = async (username: string) => {
  try {
    const res = await apiClient.get(`${API_BASE}/by-username`, {
      params: { username },
    });
    return res.data;
  } catch (error) {
    throw error;
  }
};

// Get user by email
export const getUserByEmail = async (email: string) => {
  try {
    const res = await apiClient.get(`${API_BASE}/by-email`, {
      params: { email },
    });
    return res.data;
  } catch (error) {
    throw error;
  }
};

// Get full user by ID (admin/mod only)
export const getUserFullById = async (id: string) => {
  try {
    const res = await apiClient.get(`${API_BASE}/full/${id}`);
    return res.data;
  } catch (error) {
    throw error;
  }
};

// Change user role (admin only)
export const changeUserRole = async (id: number, role: string) => {
  try {
    await apiClient.patch(`${API_BASE}/${id}/role`, null, {
      params: { role },
    });
  } catch (error) {
    throw error;
  }
};

// Change user active member status (admin/mod)
export const changeUserActiveMember = async (
  id: number,
  activeMember: boolean,
) => {
  try {
    await apiClient.patch(`${API_BASE}/${id}/active-member`, null, {
      params: { activeMember },
    });
  } catch (error) {
    throw error;
  }
};

// Admin update user (name, surname, phone, role, activeMember)
export const adminUpdateUser = async (
  id: number,
  data: {
    name: string;
    surname: string;
    phoneNumber: string;
    role: string;
    activeMember: boolean;
  },
) => {
  try {
    await apiClient.put(`${API_BASE}/${id}`, data);
  } catch (error) {
    throw error;
  }
};

// Update own user profile (name, surname, phoneNumber)
export const updateUser = async (data: {
  name: string;
  surname: string;
  phoneNumber: string;
}) => {
  try {
    const res = await apiClient.put(`${API_BASE}/me`, data);
    return res.data;
  } catch (error) {
    throw error;
  }
};

// Delete user (admin only)
export const deleteUser = async (id: number) => {
  try {
    await apiClient.delete(`${API_BASE}/${id}`);
  } catch (error) {
    throw error;
  }
};

// Confirm account

export const handleConfirmation = async (token: string) => {
  try {
    await apiClient.get("/api/user/confirm", { params: { token } });
  } catch (error) {
    throw error;
  }
};

// Create user from scratch
export const createUser = async (
  name: string,
  surname: string,
  email: string,
  password: string,
  confirmPassword: string,
  username: string,
  phoneNumber: string,
) => {
  const data = {
    name,
    surname,
    email,
    password,
    confirmPassword,
    username,
    phoneNumber,
  };

  try {
    const response = await apiClient.post("/api/user/register", data);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// Api get me equivalent
export const checkUserStatus = async () => {
  try {
    const response = await apiClient.get("/api/user/me");
    return response.data;
  } catch (error) {
    throw error;
  }
};

// Login handler
export const handleLogin = async (username: string, password: string) => {
  const params = new URLSearchParams();
  params.append("username", username);
  params.append("password", password);

  try {
    await apiClient.post("/login", params, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
  } catch (error) {
    throw error;
  }
};

export const handleLogout = async () => {
  try {
    await apiClient.post("/logout");

    console.log("Wylogowano pomyślnie");
  } catch (error) {
    console.error("Błąd podczas wylogowywania:", error);
    throw error;
  }
};

export const resetPasswordApi = async (
  token: string,
  newPassword: string,
  confirmNewPassword: string,
) => {
  const data = {
    newPassword,
    confirmNewPassword,
  };

  try {
    await apiClient.patch("/api/user/reset-password", data, {
      params: { token },
    });
  } catch (error) {
    throw error;
  }
};
export const handleForgotPassword = async (email: string) => {
  const params = new URLSearchParams();
  params.append("email", email);

  try {
    await apiClient.post("/api/user/forgot-password", params, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
  } catch (error) {
    throw error;
  }
};
