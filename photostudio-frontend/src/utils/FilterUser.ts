// for now i am giving any here change to user type after adding type definitions
export const filterUserData = (user: any) => {
    if (!user) return null;

    return {
        id: user.id,
        role: user.role
    };
};