package com.photostudio.photostudio_backend.controller;

import com.photostudio.photostudio_backend.dto.InputEventFilterDTO;
import com.photostudio.photostudio_backend.dto.event.InputEventDTO;
import com.photostudio.photostudio_backend.dto.event.OutputEventDTO;
import com.photostudio.photostudio_backend.dto.user.UserBasicOutputDTO;
import com.photostudio.photostudio_backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/event")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<Page<OutputEventDTO>> getFilteredEvents(InputEventFilterDTO filter) {
        return ResponseEntity.ok(eventService.getFilteredEvents(filter));
    }

    @GetMapping("/{eventId}/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserBasicOutputDTO>> getUsersAssignedToEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getUsersAssignedToEvent(eventId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> createEvent(@Valid @RequestBody InputEventDTO eventDTO) {
        eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> updateEvent(@PathVariable Long eventId, @Valid @RequestBody InputEventDTO eventDTO) {
        eventService.updateEvent(eventId, eventDTO);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> completeEvent(@PathVariable Long eventId) {
        eventService.completeEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}