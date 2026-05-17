package com.photostudio.photostudio_backend.controller;

import com.photostudio.photostudio_backend.service.EventManagementFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/event")
public class EventManagementFacadeController {

    private final EventManagementFacade eventManagementFacade;

    public EventManagementFacadeController(EventManagementFacade eventManagementFacade) {
        this.eventManagementFacade = eventManagementFacade;
    }
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> deleteEventWithRequests(@PathVariable Long eventId) {
        eventManagementFacade.deleteEventWithRequests(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> addUserToEvent(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        eventManagementFacade.addUserToEvent(eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/request/{eventRequestId}/user/{userId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> approveUserToEvent(
            @PathVariable Long eventId,
            @PathVariable Long eventRequestId,
            @PathVariable Long userId) {
        eventManagementFacade.approveUserToEvent(eventRequestId, eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventId}/request/{eventRequestId}/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> removeUserFromEvent(
            @PathVariable Long eventId,
            @PathVariable Long eventRequestId,
            @PathVariable Long userId) {
        eventManagementFacade.removeUserFromEvent(eventId, eventRequestId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/request/{eventRequestId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER')")
    public ResponseEntity<Void> cancelOwnEventRequest(@PathVariable Long eventRequestId) {
        eventManagementFacade.cancelOwnEventRequest(eventRequestId);
        return ResponseEntity.noContent().build();
    }
}
