package com.photostudio.photostudio_backend.dto;

import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipmentInputDTO(

    @NotBlank()
    @Size(min = 2, max = 30)
    String name,

    boolean activeMembers,

    boolean statutoryEvent,

    EquipmentCategory equipmentCategory
) {}
