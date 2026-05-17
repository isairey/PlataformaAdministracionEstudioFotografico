package com.photostudio.photostudio_backend.dto.event;

import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.enums.EventType;

import java.time.LocalDate;
import java.time.LocalTime;

public record OutputEventDTO(

        Long id,

        LocalDate date,

        LocalTime time,

        String name,

        String description,

        String location,

        int numberOfPeopleRequired,

        int numberOfAssignedPeople,


        EventType type,

        EventStatus status
) {
}
