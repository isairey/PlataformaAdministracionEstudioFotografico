package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.exception.BusinessException;
import com.photostudio.photostudio_backend.exception.EntityException;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.Event;
import com.photostudio.photostudio_backend.model.EventRequest;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.enums.EventType;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import com.photostudio.photostudio_backend.repository.EventRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventRequestServiceTest {

    @Mock
    EventRequestRepository eventRequestRepository;

    @Mock
    UserService userService;

    @Mock
    EventService eventService;

    @InjectMocks
    EventRequestService eventRequestService;

    @Captor
    private ArgumentCaptor<EventRequest> eventRequestArgumentCaptor;

    private User user;
    private Event correctEvent;
    private Event pastEvent;
    private Event canceledEvent;
    private Event fullEvent;


    @BeforeEach
    void setUp() {
        correctEvent = new Event(
            LocalDateTime.now().plusDays(5),
            "Concert music stuff",
            "music people, yeah",
            "Music club",
            2,
            null,
            null,
            EventType.KWF,
            EventStatus.PLANNED
        );

        pastEvent = new Event(
                LocalDateTime.now().minusDays(5),
                "Concert music stuff",
                "music people, yeah",
                "Music club",
                2,
                null,
                null,
                EventType.KWF,
                EventStatus.COMPLETED
        );

        canceledEvent = new Event(
                LocalDateTime.now().plusDays(5),
                "Concert music stuff",
                "music people, yeah",
                "Music club",
                2,
                null,
                null,
                EventType.KWF,
                EventStatus.CANCELLED
        );

        fullEvent = new Event(
                LocalDateTime.now().plusDays(5),
                "Concert music stuff",
                "music people, yeah",
                "Music club",
                2,
                null,
                null,
                EventType.KWF,
                EventStatus.PLANNED
        );
        fullEvent.setNumberOfAssignedPeople(2);

        user = new User(
                "Firstname",
                "surname",
                "email@email.com",
                "###############",
                "login",
                "111222333",
                UserRole.USER
        );
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

    @Nested
    public class CreateOwnEventRequestTest {

        @Test
        void createOwnEventRequestCorrectlyTest() {

            when(userService.getLoggedUser()).thenReturn(user);
            when(eventService.findEventByIdOrThrowException(1L)).thenReturn(correctEvent);

            eventRequestService.createOwnEventRequest(1L);

            verify(eventRequestRepository).save(eventRequestArgumentCaptor.capture());
            EventRequest eventRequest = eventRequestArgumentCaptor.getValue();

            assertEquals(correctEvent, eventRequest.getEvent());
            assertEquals(user, eventRequest.getUser());
            assertEquals(ReservationStatus.PENDING, eventRequest.getStatus());
        }

        @Test
        void createOwnEventRequestPastEventTest() {

            when(userService.getLoggedUser()).thenReturn(user);
            when(eventService.findEventByIdOrThrowException(1L)).thenReturn(pastEvent);

            ValidationException exception = assertThrows(ValidationException.class, () -> eventRequestService.createOwnEventRequest(1L));
            verify(eventRequestRepository, never()).save(any());
            assertEquals("Cannot request for past events", exception.getMessage());
        }

        @Test
        void createOwnEventRequestEventNotPendingTest() {

            when(userService.getLoggedUser()).thenReturn(user);
            when(eventService.findEventByIdOrThrowException(1L)).thenReturn(canceledEvent);

            ValidationException exception = assertThrows(ValidationException.class, () -> eventRequestService.createOwnEventRequest(1L));
            verify(eventRequestRepository, never()).save(any());
            assertEquals("Event is not pending", exception.getMessage());
        }

        @Test
        void createOwnEventRequestAlreadyAppliedTest() {

            when(eventRequestRepository.existsByUser_IdAndEvent_Id_AndStatus(user.getId(), 1L, ReservationStatus.PENDING)).thenReturn(Boolean.TRUE);
            when(userService.getLoggedUser()).thenReturn(user);
            when(eventService.findEventByIdOrThrowException(1L)).thenReturn(correctEvent);

            ValidationException exception = assertThrows(ValidationException.class, () -> eventRequestService.createOwnEventRequest(1L));
            verify(eventRequestRepository, never()).save(any());
            assertEquals("User already applied to this event", exception.getMessage());
        }

        @Test
        void createOwnEventRequestFullEventTest() {

            when(userService.getLoggedUser()).thenReturn(user);
            when(eventService.findEventByIdOrThrowException(1L)).thenReturn(fullEvent);

            ValidationException exception = assertThrows(ValidationException.class, () -> eventRequestService.createOwnEventRequest(1L));
            verify(eventRequestRepository, never()).save(any());
            assertEquals("Event is fully covered", exception.getMessage());
        }
    }
    @Nested
    public class rejectAllEventsRequestTest {

        private EventRequest request1;
        private EventRequest request2;
        private EventRequest request3;

        @BeforeEach
        void setUp() {

            request1 = new EventRequest(
                    correctEvent,
                    null
            );
            request2 = new EventRequest(
                    correctEvent,
                    null
            );
            request3 = new EventRequest(
                    correctEvent,
                    null
            );
        }

        @Test
        void rejectAllEventsRequestCorrectlyTest() {
            when(eventRequestRepository.findByEvent_IdAndStatus(1L, ReservationStatus.PENDING))
                    .thenReturn(List.of(request1, request2));

            eventRequestService.rejectAllRequestsForEvent(1L);

            verify(eventRequestRepository, times(1))
                    .findByEvent_IdAndStatus(1L, ReservationStatus.PENDING);
            verify(eventRequestRepository, times(1))
                    .saveAll(argThat(requests -> {
                        List<EventRequest> list = new ArrayList<>();
                        requests.forEach(list::add);
                        return list.size() == 2 &&
                                list.stream().allMatch(r -> r.getStatus() == ReservationStatus.REJECTED);
                    }));

            assertEquals(ReservationStatus.REJECTED, request1.getStatus());
            assertEquals(ReservationStatus.REJECTED, request2.getStatus());
        }
        @Test
        void rejectAllEventsRequest_WithNoRequests_ShouldNotSaveAnything() {
            // Given
            when(eventRequestRepository.findByEvent_IdAndStatus(1L, ReservationStatus.PENDING))
                    .thenReturn(List.of());
            when(eventRequestRepository.findByEvent_IdAndStatus(1L, ReservationStatus.APPROVED))
                    .thenReturn(List.of());

            // When
            eventRequestService.rejectAllRequestsForEvent(1L);

            // Then
            verify(eventRequestRepository, times(1))
                    .findByEvent_IdAndStatus(1L, ReservationStatus.PENDING);
            verify(eventRequestRepository, times(1))
                    .findByEvent_IdAndStatus(1L, ReservationStatus.APPROVED);
            verify(eventRequestRepository, times(2)).saveAll(Collections.emptyList());
        }
        @Test
        void rejectAllEventsRequest_ShouldOnlyAffectPendingAndApprovedRequests() {
            // Given
            request3.setStatus(ReservationStatus.APPROVED);
            request2.setStatus(ReservationStatus.CANCELLED);

            when(eventRequestRepository.findByEvent_IdAndStatus(1L, ReservationStatus.PENDING))
                    .thenReturn(List.of(request1));

            when(eventRequestRepository.findByEvent_IdAndStatus(1L, ReservationStatus.APPROVED))
                    .thenReturn(List.of(request3));

            // When
            eventRequestService.rejectAllRequestsForEvent(1L);

            // Then
            verify(eventRequestRepository, times(1))
                    .findByEvent_IdAndStatus(1L, ReservationStatus.PENDING);
            verify(eventRequestRepository, times(1))
                    .findByEvent_IdAndStatus(1L, ReservationStatus.APPROVED);

            assertEquals(ReservationStatus.REJECTED, request1.getStatus());
            assertEquals(ReservationStatus.CANCELLED, request2.getStatus());
            assertEquals(ReservationStatus.REJECTED, request3.getStatus());
        }
    }
    @Nested
    public class cancelOwnEventsRequestTest {

        private EventRequest request1;
        private EventRequest request2;
        private User user;
        private User user2;

        @BeforeEach
        void setUp() {
            user = new User();
            setId(user, 2L);

            user2 = new User();
            setId(user2, 3L);

            request1 = new EventRequest(
                    correctEvent,
                    user
            );
            request2 = new EventRequest(
                    correctEvent,
                    user2
            );
        }

        @Test
        void correctlyCancelOwnEventsRequestCorrectlyTest() {
            when(userService.getLoggedUser()).thenReturn(user);
            when(eventRequestRepository.findById(1L)).thenReturn(Optional.of(request1));

            eventRequestService.cancelOwnEventRequest(1L);

            verify(eventRequestRepository, times(1))
            .findById(1L);
            verify(eventRequestRepository, times(1))
                    .save(request1);
            assertEquals(ReservationStatus.CANCELLED, request1.getStatus());
        }

        @Test
        void notUsersEventRequestTest() {
            when(userService.getLoggedUser()).thenReturn(user);
            when(eventRequestRepository.findById(1L)).thenReturn(Optional.of(request2));

            BusinessException exception = assertThrows(BusinessException.class, () -> eventRequestService.cancelOwnEventRequest(1L));
            verify(eventRequestRepository, times(1))
            .findById(1L);
            verify(eventRequestRepository, never()).save(any());
            assertEquals("Cannot reject event request as it does not belong to user", exception.getMessage());
        }

        @Test
        void notPendingEventRequestTest() {
            request1.setStatus(ReservationStatus.REJECTED);
            when(userService.getLoggedUser()).thenReturn(user);
            when(eventRequestRepository.findById(1L)).thenReturn(Optional.of(request1));

            BusinessException exception = assertThrows(BusinessException.class, () -> eventRequestService.cancelOwnEventRequest(1L));
            verify(eventRequestRepository, times(1))
            .findById(1L);
            verify(eventRequestRepository, never()).save(any());
            assertEquals("Cannot cancel event request as its status is not PENDING", exception.getMessage());
        }
    }
    @Nested
    public class approveEventRequestTest {

        private EventRequest request1;

        @BeforeEach
        void setUp() {
            request1 = new EventRequest(
                    correctEvent,
                    null
            );
        }

        @Test
        void approveCorrectPendingEventRequestTest() {
            when(eventRequestRepository.findById(1L)).thenReturn(Optional.of(request1));

            eventRequestService.approveEventRequest(1L);

            verify(eventRequestRepository, times(1))
            .findById(1L);
            verify(eventRequestRepository, times(1))
            .save(request1);

            assertEquals(ReservationStatus.APPROVED, request1.getStatus());
        }
        @Test
        void approveNotExistingEventRequestTest() {
            when(eventRequestRepository.findById(1L)).thenReturn(Optional.empty());

            EntityException exception = assertThrows(EntityException.class, () -> eventRequestService.approveEventRequest(1L));
            verify(eventRequestRepository, times(1))
            .findById(1L);
            verify(eventRequestRepository, never()).save(any());
            assertEquals("Event request not found", exception.getMessage());
        }
        @Test
        void approveCorrectlyRejectedEventRequestTest() {
            request1.setStatus(ReservationStatus.REJECTED);
            when(eventRequestRepository.findById(1L)).thenReturn(Optional.of(request1));

            eventRequestService.approveEventRequest(1L);

            verify(eventRequestRepository, times(1))
                    .findById(1L);
            verify(eventRequestRepository, times(1))
                    .save(request1);

            assertEquals(ReservationStatus.APPROVED, request1.getStatus());
        }

        @Test
        void approveWrongStatusEventRequestTest() {
            request1.setStatus(ReservationStatus.APPROVED);
            when(eventRequestRepository.findById(1L)).thenReturn(Optional.of(request1));

            BusinessException exception = assertThrows(BusinessException.class, () -> eventRequestService.approveEventRequest(1L));
            verify(eventRequestRepository, times(1))
                    .findById(1L);
            verify(eventRequestRepository, never()).save(any());
            assertEquals("Cannot approve event request as its status is not PENDING or REJECTED", exception.getMessage());
        }

    }
    @Nested
    public class cancelAllUserRequestsTest {

        private EventRequest request1;
        private EventRequest request2;
        private User user;
        private User user2;

        @BeforeEach
        void setUp() {
            user = new User();
            setId(user, 2L);

            user2 = new User();
            setId(user2, 3L);

            request1 = new EventRequest(
                    correctEvent,
                    user
            );
            request2 = new EventRequest(
                    correctEvent,
                    user
            );
        }

        @Test
        void correctlyCancelAllUserRequestsTest() {
            when(userService.getUserOrThrowException(1L)).thenReturn(user);
            when(eventRequestRepository.findByUserAndStatus(user, ReservationStatus.PENDING)).thenReturn(List.of(request1, request2));

            eventRequestService.cancelAllUserRequests(1L);

            verify(userService, times(1)).getUserOrThrowException(1L);
            verify(eventRequestRepository, times(1))
                    .findByUserAndStatus(user, ReservationStatus.PENDING);
            verify(eventRequestRepository, times(1))
                    .saveAll(argThat(requests -> {
                        List<EventRequest> list = new ArrayList<>();
                        requests.forEach(list::add);
                        return list.size() == 2 &&
                                list.stream().allMatch(r -> r.getStatus() == ReservationStatus.CANCELLED);
                    }));

            assertEquals(ReservationStatus.CANCELLED, request1.getStatus());
            assertEquals(ReservationStatus.CANCELLED, request2.getStatus());
        }
        @Test
        void cancelAllUserRequests_WithNoRequests_ShouldSaveEmptyList() {
            // Given
            when(userService.getUserOrThrowException(1L)).thenReturn(user);
            when(eventRequestRepository.findByUserAndStatus(user, ReservationStatus.PENDING))
                    .thenReturn(List.of());

            // When
            eventRequestService.cancelAllUserRequests(1L);

            // Then
            verify(userService, times(1)).getUserOrThrowException(1L);
            verify(eventRequestRepository, times(1))
                    .findByUserAndStatus(user, ReservationStatus.PENDING);
            verify(eventRequestRepository, times(1))
                    .saveAll(argThat(requests -> {
                        List<EventRequest> list = new ArrayList<>();
                        requests.forEach(list::add);
                        return list.isEmpty();
                    }));
        }
    }
    @Nested
    public class rejectEventRequestTest {

        User user;
        Event event;
        EventRequest request;

        @BeforeEach
        void setUp() {
            user = new User();
            setId(user, 3L);

            event = new Event();
            setId(event, 4L);

            request = new EventRequest(event, user);
            setId(request, 5L);
        }

        @Test
        void correctlyRejectEventRequestTest() {
            when(eventRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            eventRequestService.rejectEventRequest(5L);

            verify(eventRequestRepository, times(1)).save(request);
            assertEquals(ReservationStatus.REJECTED, request.getStatus());
        }

        @Test
        void rejectNotExistingEventRequestTest() {
            when(eventRequestRepository.findById(5L)).thenReturn(Optional.empty());

            EntityException exception = assertThrows(EntityException.class, () -> eventRequestService.rejectEventRequest(5L));
            verify(eventRequestRepository, times(1)).findById(5L);
            assertEquals("Event request not found", exception.getMessage());
        }

        @Test
        void notRejectCancelledEventRequestTest() {
            when(eventRequestRepository.findById(5L)).thenReturn(Optional.of(request));
            request.setStatus(ReservationStatus.CANCELLED);

            BusinessException exception = assertThrows(BusinessException.class, () -> eventRequestService.rejectEventRequest(5L));
            verify(eventRequestRepository, times(1)).findById(5L);
            assertEquals("Cannot reject event request as its status is CANCELLED", exception.getMessage());
        }
    }
    @Nested
    public class doesEventRequestAlreadyExistTest {

        User user;
        EventRequest request;

        @BeforeEach
        void setUp() {
            user = new User();
            request = new EventRequest();
        }

        @Test
        void correctlyReturnTrueTest() {
            List<ReservationStatus> existing = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);
            when(userService.getLoggedUser()).thenReturn(user);
            when(eventRequestRepository.existsByEvent_IdAndUserAndStatusIn(1L, user, existing)).thenReturn(true);

            Boolean result = eventRequestService.doesEventRequestAlreadyExist(1L);

            verify(eventRequestRepository, times(1)).existsByEvent_IdAndUserAndStatusIn(1L, user, existing);
            assertEquals(true, result);
        }

        @Test
        void correctlyReturnFalseTest() {
            List<ReservationStatus> existing = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);
            when(userService.getLoggedUser()).thenReturn(user);
            when(eventRequestRepository.existsByEvent_IdAndUserAndStatusIn(1L, user, existing)).thenReturn(false);

            Boolean result = eventRequestService.doesEventRequestAlreadyExist(1L);

            verify(eventRequestRepository, times(1)).existsByEvent_IdAndUserAndStatusIn(1L, user, existing);
            assertEquals(false, result);
        }
    }
}
