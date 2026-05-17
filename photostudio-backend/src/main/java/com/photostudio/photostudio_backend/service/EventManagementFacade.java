package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.model.EventRequest;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventManagementFacade {

    private static final Logger log = LoggerFactory.getLogger(EventManagementFacade.class);

    private final EventService eventService;
    private final EventRequestService eventRequestService;
    private final EquipmentReservationService equipmentReservationService;

    public EventManagementFacade(EventService eventService,
                                 EventRequestService eventRequestService, EquipmentReservationService equipmentReservationService) {
        this.eventService = eventService;
        this.eventRequestService = eventRequestService;
        this.equipmentReservationService = equipmentReservationService;
    }

    // people that were assigned will still be visible in event list
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public void deleteEventWithRequests(Long eventId) {
        log.info("Deleting event {} with all requests", eventId);

        eventRequestService.rejectAllRequestsForEvent(eventId);
        equipmentReservationService.rejectAllRequestsForEvent(eventId);
        eventService.deleteEvent(eventId);

        log.info("Successfully deleted event {} with all requests", eventId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public void addUserToEvent(Long eventId, Long userId) {

        Long requestId = eventRequestService.createAutomatedEventRequest(userId, eventId);
        eventService.addUserToEvent(eventId, userId);
        eventRequestService.approveEventRequest(requestId);

        log.info("Successfully added user (ID: {}) to event {} with request {}", userId, eventId, requestId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public void approveUserToEvent(Long eventRequestId, Long eventId, Long userId) {

        eventService.addUserToEvent(eventId, userId);
        eventRequestService.approveEventRequest(eventRequestId);

        log.info("Successfully approved user (ID: {}) to event with request {}", userId, eventRequestId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public void removeUserFromEvent(Long eventId, Long eventRequestId, Long userId) {

        EventRequest request = eventRequestService.findEventRequestById(eventRequestId);
        if (request.getStatus().equals(ReservationStatus.APPROVED)) {
            eventService.removeUserFromEvent(eventId, userId);
        }
        eventRequestService.rejectEventRequest(eventRequestId);
        equipmentReservationService.rejectAllUserRequests(userId);

        log.info("Successfully deleted user (ID: {}) from event {}", userId, request.getEvent().getId());
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    @Transactional
    public void cancelOwnEventRequest(Long eventRequestId) {
        eventRequestService.cancelOwnEventRequest(eventRequestId);
        equipmentReservationService.rejectAllForEventRequest(eventRequestId);
        log.info("Cancelled own event request by id: {}", eventRequestId);
    }
}