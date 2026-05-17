package com.photostudio.photostudio_backend.dto.equipmentReservation;

import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;

public record EquipmentCount(
        EquipmentCategory category,
        Long count
) {
}
