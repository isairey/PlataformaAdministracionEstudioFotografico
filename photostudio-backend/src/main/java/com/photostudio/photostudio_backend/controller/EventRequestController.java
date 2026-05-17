package com.photostudio.photostudio_backend.controller;

import com.photostudio.photostudio_backend.dto.eventRequest.AllEventRequestFilterDTO;
import com.photostudio.photostudio_backend.dto.eventRequest.EventRequestDTO;
import com.photostudio.photostudio_backend.dto.eventRequest.EventRequestFilterDTO;
import com.photostudio.photostudio_backend.service.EventRequestService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/event/request")
public class EventRequestController {

    private final EventRequestService eventRequestService;

    public EventRequestController(EventRequestService eventRequestService) {
        this.eventRequestService = eventRequestService;
    }

    @PostMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER')")
    public ResponseEntity<Void> createOwnEventRequest(@PathVariable Long eventId) {
        eventRequestService.createOwnEventRequest(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/exist")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<Boolean> requestAlreadyExist(Long eventId) {
        return ResponseEntity.ok(eventRequestService.doesEventRequestAlreadyExist(eventId));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<Page<EventRequestDTO>> findOwnFilteredRequests(EventRequestFilterDTO dto) {
        return ResponseEntity.ok(eventRequestService.getFilteredRequests(dto));
    }

    @GetMapping("/filter/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<Page<EventRequestDTO>> findFilteredRequests(AllEventRequestFilterDTO dto) {
        return ResponseEntity.ok(eventRequestService.getAllFilteredRequests(dto));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<List<EventRequestDTO>> findActiveRequests() {
        return ResponseEntity.ok(eventRequestService.getAllFutureActiveEventRequests());
    }

}
