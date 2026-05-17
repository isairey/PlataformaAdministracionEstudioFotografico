package com.photostudio.photostudio_backend.dto.equipmentReservation;

import java.time.LocalDateTime;

public record TimeWindowDTO(LocalDateTime start, LocalDateTime end) {
}
