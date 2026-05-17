package com.photostudio.photostudio_backend.dto;

import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EquipmentOutputDTO(

        Long id,

        @NotBlank()
        @Size(min = 2, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9-_,. ]+$", message = "Invalid name")
        String name,

        boolean activeMembers,

        boolean statutoryEvent,

        EquipmentCategory equipmentCategory
) {
}
