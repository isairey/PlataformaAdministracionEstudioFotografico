package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.model.Event;
import com.photostudio.photostudio_backend.model.EventRequest;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EventManagementFacadeTest {

    @Mock
    private EventService eventService;

    @Mock
    private EventRequestService eventRequestService;

    @InjectMocks
    private EventManagementFacade eventManagementFacade;

    private Long eventId;
    private Long userId;
    private Long requestId;
    private Event event;
    private User user;
    private EventRequest eventRequest;
    private Long eventRequestId;

    @BeforeEach
    void setUp() {
        eventId = 1L;
        userId = 100L;
        requestId = 50L;
        eventRequestId = 100L;

        event = new Event();
        setId(event, eventId);

        user = new User();
        setId(user, userId);

        eventRequest = new EventRequest(event, user);
        setId(eventRequest, requestId);
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void deleteEventWithRequestsCorrectlyTest() {
        // When
        eventManagementFacade.deleteEventWithRequests(eventId);

        // Then
        verify(eventRequestService).rejectAllRequestsForEvent(eventId);
        verify(eventService).deleteEvent(eventId);

        var inOrder = inOrder(eventRequestService, eventService);
        inOrder.verify(eventRequestService).rejectAllRequestsForEvent(eventId);
        inOrder.verify(eventService).deleteEvent(eventId);
    }

    @Test
    void addUserToEventTest() {
        // Given
        when(eventRequestService.createAutomatedEventRequest(userId, eventId))
                .thenReturn(requestId);

        // When
        eventManagementFacade.addUserToEvent(eventId, userId);

        // Then
        verify(eventRequestService).createAutomatedEventRequest(userId, eventId);
        verify(eventService).addUserToEvent(eventId, userId);
        verify(eventRequestService).approveEventRequest(requestId);

        var inOrder = inOrder(eventService, eventRequestService);
        inOrder.verify(eventRequestService).createAutomatedEventRequest(userId, eventId);
        inOrder.verify(eventService).addUserToEvent(eventId, userId);
        inOrder.verify(eventRequestService).approveEventRequest(requestId);
    }

    @Test
    void approveUserToEventTest() {
        // When
        eventManagementFacade.approveUserToEvent(eventRequestId, eventId, userId);

        // Then
        verify(eventService).addUserToEvent(eventId, userId);
        verify(eventRequestService).approveEventRequest(eventRequestId);

        var inOrder = inOrder(eventRequestService, eventService);
        inOrder.verify(eventService).addUserToEvent(eventId, userId);
        inOrder.verify(eventRequestService).approveEventRequest(eventRequestId);
    }

    @Test
    void removeUserFromEventApprovedTest() {
        when(eventRequestService.findEventRequestById(eventRequestId)).thenReturn(eventRequest);
        eventRequest.setStatus(ReservationStatus.APPROVED);
        // When
        eventManagementFacade.removeUserFromEvent(eventId, eventRequestId, userId);

        // Then
        verify(eventRequestService).rejectEventRequest(eventRequestId);
        verify(eventService).removeUserFromEvent(eventId, userId);

        var inOrder = inOrder(eventRequestService, eventService);
        inOrder.verify(eventService).removeUserFromEvent(eventId, userId);
        inOrder.verify(eventRequestService).rejectEventRequest(eventRequestId);
    }

    @Test
    void removeUserFromEventNotApprovedTest() {
        when(eventRequestService.findEventRequestById(eventRequestId)).thenReturn(eventRequest);
        eventRequest.setStatus(ReservationStatus.CANCELLED);
        // When
        eventManagementFacade.removeUserFromEvent(eventId, eventRequestId, userId);

        // Then
        verify(eventRequestService).rejectEventRequest(eventRequestId);
        verify(eventService, never()).removeUserFromEvent(eventId, userId);
    }
}
