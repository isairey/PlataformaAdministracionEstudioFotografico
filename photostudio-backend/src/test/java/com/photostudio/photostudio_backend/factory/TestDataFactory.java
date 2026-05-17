package com.photostudio.photostudio_backend.factory;

import com.photostudio.photostudio_backend.model.*;
import com.photostudio.photostudio_backend.model.enums.*;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static User createAdmin() {
        return new User("Admin", "Admin", "admin@user.com", "adminHash",
                "adminUser", "987654321", UserRole.SUPER_ADMIN);
    }

    public static User createRegularUser(String username, String email) {
        return new User("Tomasz", "Problem", email, "passwordHash",
                username, "123456789", UserRole.USER);
    }

    public static Equipment createCamera(String name) {
        return new Equipment(name, false, false, EquipmentCategory.CAMERA);
    }

    public static Equipment createLens(String name) {
        return new Equipment(name, false, false, EquipmentCategory.LENS);
    }

    public static Event createEvent(String name, User creator, LocalDateTime startTime) {
        return new Event(
                startTime,
                name,
                "Opis wydarzenia " + name,
                "Kraków",
                5,
                creator,
                creator,
                EventType.KWF,
                EventStatus.PLANNED
        );
    }
}