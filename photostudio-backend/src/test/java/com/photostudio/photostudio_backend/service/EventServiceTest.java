package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.InputEventFilterDTO;
import com.photostudio.photostudio_backend.dto.event.InputEventDTO;
import com.photostudio.photostudio_backend.dto.event.OutputEventDTO;
import com.photostudio.photostudio_backend.dto.user.UserBasicOutputDTO;
import com.photostudio.photostudio_backend.dto.user.UserFullOutputDTO;
import com.photostudio.photostudio_backend.exception.BusinessException;
import com.photostudio.photostudio_backend.exception.EntityException;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.Event;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.enums.EventType;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import com.photostudio.photostudio_backend.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @Mock
    private MailNotificationService mailNotificationService;

    @Captor
    private ArgumentCaptor<Event> eventArgumentCaptor;

    @InjectMocks
    private EventService eventService;

    private Event existingEvent;
    private Event pastEvent;
    private InputEventDTO validEventDTO;
    private User testCreator;
    private UserFullOutputDTO testParticipantDTO;
    private User testParticipant;
    private User testSecondParticipant;

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        testCreator = new User();
        testCreator.setUsername("testCreator");
        testCreator.setRole(UserRole.MODERATOR);
        testCreator.setEmail("testCreator@email.com");

        validEventDTO = new InputEventDTO(
                LocalDate.now().plusDays(7),
                LocalTime.of(10, 0),
                "Film festival",
                "",
                "Studio A",
                2,
                EventType.KWF
        );

        testParticipantDTO = new UserFullOutputDTO(
                100L,
                "Parti",
                "Cipant",
                "participant@email.com",
                "testParticipant",
                false,
                "123456789",
                UserRole.USER);

        testParticipant = new User();
        setId(testParticipant, 100L);
        testParticipant.setEmail("participant@email.com");
        testParticipant.setUsername("testParticipant");
        testParticipant.setDeleted(false);
        testParticipant.setEnabled(true);

        testSecondParticipant = new User();
        setId(testSecondParticipant, 101L);
        testSecondParticipant.setEmail("participant2@email.com");
        testSecondParticipant.setUsername("testParticipantsecond");
        testSecondParticipant.setDeleted(false);
        testSecondParticipant.setEnabled(true);

        existingEvent = new Event(
                LocalDateTime.now().plusDays(10),
                "Existing Event",
                "",
                "Studio B",
                3,
                testCreator,
                testCreator,
                EventType.AKRE,
                EventStatus.PLANNED
        );

        pastEvent = new Event(
                LocalDateTime.now().minusDays(10),
                "Existing Event",
                "",
                "Studio B",
                3,
                testCreator,
                testCreator,
                EventType.AKRE,
                EventStatus.PLANNED
        );

        setId(existingEvent, 1L);
        setId(pastEvent, 2L);
    }


    @Nested
    public class createEventTest {

        @Test
        void correctlyCreatesEvent() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            LocalDate date = LocalDate.of(2027, 1, 1);
            LocalTime time = LocalTime.of(1, 1, 1);

            InputEventDTO validEvent = new InputEventDTO(
                    date,
                    time,
                    "Event name",
                    "",
                    "Studio Club",
                    2,
                    EventType.KWF
            );
            eventService.createEvent(validEvent);

            verify(eventRepository).save(eventArgumentCaptor.capture());
            Event capturedEvent = eventArgumentCaptor.getValue();

            assertEquals(testCreator, capturedEvent.getCreator());
            assertEquals(testCreator, capturedEvent.getLastModifier());
        }

        @Test
        void createEvent_PastDate_ThrowsValidationException() {
            // Given
            when(userService.getLoggedUser()).thenReturn(testCreator);
            InputEventDTO pastEventDTO = new InputEventDTO(
                    LocalDate.now().minusDays(7),
                    LocalTime.of(10, 0),
                    "Film festival",
                    null,
                    "Studio A",
                    2,
                    EventType.KWF
            );
            assertThrows(ValidationException.class, () -> eventService.createEvent(pastEventDTO));
            verify(eventRepository, never()).save(any());
        }
    }
    @Nested
    public class deleteEventTest {

        @Test
        void correctlyDeleteEventAndNotifyUsers() {
            // Given
            existingEvent.getAssignedUsers().add(testParticipant);
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
            when(userService.convertUserToOutputDTO(any(User.class))).thenReturn(testParticipantDTO);
            eventService.deleteEvent(1L);


            verify(mailNotificationService).sendEventCancellationMail(
                    eq(testParticipant.getEmail()),
                    eq(existingEvent.getName()),
                    eq(existingEvent.getDateTime().toLocalDate())
            );

            verify(eventRepository).save(eventArgumentCaptor.capture());
            Event capturedEvent = eventArgumentCaptor.getValue();

            assertEquals(EventStatus.CANCELLED, capturedEvent.getStatus());
            assertEquals(testCreator, capturedEvent.getLastModifier());
        }
        @Test
        void incorrectEventIdTest() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityException.class, () -> eventService.deleteEvent(1L));
            verify(eventRepository, never()).save(any());
        }

        @Test
        void noUsersAssignedToEventTest() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
            eventService.deleteEvent(1L);

            verify(mailNotificationService, never()).sendEventCancellationMail(any(), any(), any());
            verify(eventRepository).save(eventArgumentCaptor.capture());
            Event capturedEvent = eventArgumentCaptor.getValue();

            assertEquals(EventStatus.CANCELLED, capturedEvent.getStatus());
            assertEquals(testCreator, capturedEvent.getLastModifier());
        }
    }
    @Nested
    public class completeEventTest {

        @Test
        void correctlyRejectFutureEventTest() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

            assertThrows(ValidationException.class, () -> eventService.completeEvent(1L));
            verify(eventRepository, never()).save(any());
        }

        @Test
        void correctlyCompleteEvent() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(2L)).thenReturn(Optional.of(pastEvent));

            eventService.completeEvent(2L);

            verify(eventRepository).save(eventArgumentCaptor.capture());
            Event capturedEvent = eventArgumentCaptor.getValue();
            assertEquals(EventStatus.COMPLETED, capturedEvent.getStatus());
            assertEquals(testCreator, capturedEvent.getLastModifier());
        }
        @Test
        void incorrectEventIdTest() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityException.class, () -> eventService.completeEvent(1L));
            verify(eventRepository, never()).save(any());
        }
    }
    @Nested
    public class updateEventTest {

        @Test
        void correctlyUpdateEventAndNotifyUsers() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.getAssignedUsers().add(testParticipant);
            when(userService.convertUserToOutputDTO(any(User.class))).thenReturn(testParticipantDTO);

            eventService.updateEvent(1L, validEventDTO);

            verify(mailNotificationService).sendEventModifiedMail(
                    eq(testParticipant.getEmail()),
                    any(),
                    any(),
                    eq(validEventDTO.name()),
                    eq(validEventDTO.location()),
                    eq(validEventDTO.date())
            );

            verify(eventRepository).save(eventArgumentCaptor.capture());
            Event capturedEvent = eventArgumentCaptor.getValue();

            assertEquals(validEventDTO.type(), existingEvent.getType());
            assertEquals(testCreator, capturedEvent.getLastModifier());
            assertEquals(validEventDTO.name(), capturedEvent.getName());
            assertEquals(validEventDTO.date(), capturedEvent.getDateTime().toLocalDate());
            assertEquals(validEventDTO.time(), capturedEvent.getDateTime().toLocalTime());
            assertEquals(validEventDTO.location(), capturedEvent.getLocation());
            assertEquals(validEventDTO.numberOfPeopleRequired(), capturedEvent.getNumberOfPeopleRequired());
        }

        @Test
        void incorrectEventIdTest() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityException.class, () -> eventService.updateEvent(1L, validEventDTO));
            verify(eventRepository, never()).save(any());
        }

        @Test
        void invalidEventStatusTest() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            existingEvent.setStatus(EventStatus.CANCELLED);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

            assertThrows(BusinessException.class, () -> eventService.updateEvent(1L, validEventDTO));
            verify(eventRepository, never()).save(any());
        }
        @Test
        void invalidDateTest() {
            when(userService.getLoggedUser()).thenReturn(testCreator);

            InputEventDTO invalidEventDTO = new InputEventDTO(
                LocalDate.now().minusDays(7),
                LocalTime.of(11, 10),
                "Event name",
                null,
                "Studio Club",
                2,
                EventType.KWF
            );
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
            assertThrows(ValidationException.class, () -> eventService.updateEvent(1L, invalidEventDTO));
            verify(eventRepository, never()).save(any());
        }
        @Test
        void noUsersAssignedToEvent_doesNotSendEmails() {
            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

            existingEvent.getAssignedUsers().clear();

            eventService.updateEvent(1L, validEventDTO);

            verify(mailNotificationService, never()).sendEventModifiedMail(any(), any(), any(), any(), any(), any());
            verify(eventRepository).save(any(Event.class));
        }

        @Test
        void invalidNumberOfPeopleRequiredTest() {

            when(userService.getLoggedUser()).thenReturn(testCreator);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.setNumberOfPeopleRequired(3);
            existingEvent.setNumberOfAssignedPeople(3);

            ValidationException validationException = assertThrows(ValidationException.class, () -> eventService.updateEvent(1L, validEventDTO));
            assertEquals("Cannot update event with number of people required higher than current number of people assigned", validationException.getMessage());
        }

    }
    @Nested
    public class addUserToEventTest {

        @Test
        void correctlyAddUserToEventTest() {
            existingEvent.setNumberOfAssignedPeople(1);
            int initialPeopleAssigned = existingEvent.getNumberOfAssignedPeople();
            when(userService.getUserOrThrowException(100L)).thenReturn(testParticipant);
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.getAssignedUsers().clear();

            eventService.addUserToEvent(1L, testParticipant.getId());

            verify(eventRepository).save(eventArgumentCaptor.capture());
            Event capturedEvent = eventArgumentCaptor.getValue();

            assertTrue(capturedEvent.getAssignedUsers().contains(testParticipant));
            verify(eventRepository).save(eventArgumentCaptor.capture());
            assertEquals(initialPeopleAssigned + 1, capturedEvent.getNumberOfAssignedPeople());
        }
        @Test
        void userAlreadyAddedToEventTest() {
            when(userService.getUserOrThrowException(100L)).thenReturn(testParticipant);
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.getAssignedUsers().add(testParticipant);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> eventService.addUserToEvent(existingEvent.getId(), testParticipant.getId()));
            assertEquals("User already assigned to this event", exception.getMessage());
            verify(eventRepository, never()).save(any());
        }
        @Test
        void eventStatusNotPlannedTest() {
            when(userService.getUserOrThrowException(100L)).thenReturn(testParticipant);
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.setStatus(EventStatus.COMPLETED);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> eventService.addUserToEvent(existingEvent.getId(), testParticipant.getId()));
            assertEquals("Cannot add/remove user to an event with status " + existingEvent.getStatus(), exception.getMessage());
            verify(eventRepository, never()).save(any());
        }
        @Test
        void insufficientNumberOfPeopleRequiredTest() {
            when(userService.getUserOrThrowException(100L)).thenReturn(testParticipant);
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.setNumberOfPeopleRequired(0);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> eventService.addUserToEvent(existingEvent.getId(), testParticipant.getId()));
            assertEquals("Cannot add another user to this event", exception.getMessage());
            verify(eventRepository, never()).save(any());
        }
        @Test
        void notExistingEventIdTest() {
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

            assertThrows( EntityException.class, () -> eventService.addUserToEvent(existingEvent.getId(), testParticipant.getId()));
            verify(eventRepository, never()).save(any());
        }
        @Test
        void notExistingUserTest() {
            when(userService.getUserOrThrowException(100L)).thenThrow(new EntityException("Event not found"));
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));

            assertThrows( EntityException.class, () -> eventService.addUserToEvent(existingEvent.getId(), testParticipant.getId()));
            verify(eventRepository, never()).save(any());
            verify(eventRepository, never()).save(any());
        }

    }
    @Nested
    public class removeUserFromEventTest {

        @Test
        void correctlyRemoveUserFromEventTest() {
            existingEvent.setNumberOfAssignedPeople(5);
            int initialPeopleAssigned = existingEvent.getNumberOfAssignedPeople();
            existingEvent.getAssignedUsers().clear();
            existingEvent.getAssignedUsers().add(testParticipant);

            when(userService.getUserOrThrowException(100L)).thenReturn(testParticipant);
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));
            eventService.removeUserFromEvent(existingEvent.getId(), testParticipant.getId());

            verify(eventRepository).save(eventArgumentCaptor.capture());
            Event capturedEvent = eventArgumentCaptor.getValue();

            assertFalse(capturedEvent.getAssignedUsers().contains(testParticipant));
            assertEquals(initialPeopleAssigned - 1, capturedEvent.getNumberOfAssignedPeople());
        }

        @Test
        void userNotAssignedToEventTest() {
            when(userService.getUserOrThrowException(100L)).thenReturn(testParticipant);
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.getAssignedUsers().clear();

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> eventService.removeUserFromEvent(existingEvent.getId(), testParticipant.getId()));
            assertEquals("User doesn't participate in this event", exception.getMessage());
            verify(eventRepository, never()).save(any());
        }

        @Test
        void eventStatusNotPlannedTest() {
            existingEvent.getAssignedUsers().clear();
            existingEvent.getAssignedUsers().add(testParticipant);

            when(userService.getUserOrThrowException(100L)).thenReturn(testParticipant);
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));
            existingEvent.setStatus(EventStatus.COMPLETED);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> eventService.removeUserFromEvent(existingEvent.getId(), testParticipant.getId()));
            assertEquals("Cannot add/remove user to an event with status " + existingEvent.getStatus(), exception.getMessage());
            verify(eventRepository, never()).save(any());
        }

        @Test
        void notExistingEventIdTest() {
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

            assertThrows(EntityException.class,
                    () -> eventService.removeUserFromEvent(existingEvent.getId(), testParticipant.getId()));
            verify(eventRepository, never()).save(any());
        }

        @Test
        void notExistingUserTest() {
            when(userService.getUserOrThrowException(100L)).thenThrow(new EntityException("Event not found"));
            when(eventRepository.findByIdWithLock(1L)).thenReturn(Optional.of(existingEvent));

            assertThrows(EntityException.class,
                    () -> eventService.removeUserFromEvent(existingEvent.getId(), testParticipant.getId()));
            verify(eventRepository, never()).save(any());
        }
    }
    @Nested
    public class getInformationTest {

        Event event1;
        Event event2;
        Event event3;
        Event event4;

        OutputEventDTO eventOutput1;
        OutputEventDTO eventOutput2;
        OutputEventDTO eventOutput3;
        OutputEventDTO eventOutput4;

        @BeforeEach
        void setUp() {
            event1 = new Event(
                    LocalDateTime.now().plusDays(7),
                    "First event",
                    null,
                    "Concert hall",
                    1,
                    testCreator,
                    testCreator,
                    EventType.AKT,
                    EventStatus.PLANNED
            );
            event2 = new Event (
                    LocalDateTime.now().plusDays(10),
                    "Second event",
                    null,
                    "Concert hall",
                    1,
                    testCreator,
                    testCreator,
                    EventType.KWF,
                    EventStatus.PLANNED
            );
            event3 = new Event (
                    LocalDateTime.now().plusDays(3),
                    "Third event",
                    null,
                    "Local mall",
                    1,
                    testCreator,
                    testCreator,
                    EventType.AKT,
                    EventStatus.CANCELLED
            );
            event4 = new Event (
                    LocalDateTime.now().minusDays(2),
                    "Fourth event",
                    null,
                    "Concert hall",
                    0,
                    testCreator,
                    testCreator,
                    EventType.AKT,
                    EventStatus.COMPLETED
            );
            setId(event1, 10L);
            setId(event2, 11L);
            setId(event3, 12L);
            setId(event4, 13L);

            eventOutput1 = new OutputEventDTO(
                    event1.getId(),
                    LocalDate.from(event1.getDateTime()),
                    LocalTime.from(event1.getDateTime()),
                    event1.getName(),
                    event1.getDescription(),
                    event1.getLocation(),
                    event1.getNumberOfPeopleRequired(),
                    event1.getNumberOfAssignedPeople(),
                    event1.getType(),
                    event1.getStatus()
            );
            eventOutput2 = new OutputEventDTO(
                    event2.getId(),
                    LocalDate.from(event2.getDateTime()),
                    LocalTime.from(event2.getDateTime()),
                    event2.getName(),
                    event2.getDescription(),
                    event2.getLocation(),
                    event2.getNumberOfPeopleRequired(),
                    event2.getNumberOfAssignedPeople(),
                    event2.getType(),
                    event2.getStatus()
            );
            eventOutput3 = new OutputEventDTO(
                    event3.getId(),
                    LocalDate.from(event3.getDateTime()),
                    LocalTime.from(event3.getDateTime()),
                    event3.getName(),
                    event3.getDescription(),
                    event3.getLocation(),
                    event3.getNumberOfPeopleRequired(),
                    event3.getNumberOfAssignedPeople(),
                    event3.getType(),
                    event3.getStatus()
            );
            eventOutput4 = new OutputEventDTO(
                    event4.getId(),
                    LocalDate.from(event4.getDateTime()),
                    LocalTime.from(event4.getDateTime()),
                    event4.getName(),
                    event4.getDescription(),
                    event4.getLocation(),
                    event4.getNumberOfPeopleRequired(),
                    event4.getNumberOfAssignedPeople(),
                    event4.getType(),
                    event4.getStatus()
            );
        }

        @Test
        void getUsersAssignedToEventTest() {
            UserBasicOutputDTO firstBasicDTO = new UserBasicOutputDTO(
                    1L,
                    "Al",
                    "Pacino",
                    "alic"
            );
            UserBasicOutputDTO secondBasicDTO = new UserBasicOutputDTO(
                    2L,
                    "Git",
                    "Bash",
                    "gitbash"
            );

            event1.getAssignedUsers().add(testParticipant);
            event1.getAssignedUsers().add(testSecondParticipant);

            when(eventRepository.findById(event1.getId())).thenReturn(Optional.of(event1));
            when(userService.convertUserToBasicOutputDTO(testParticipant)).thenReturn(firstBasicDTO);
            when(userService.convertUserToBasicOutputDTO(testSecondParticipant)).thenReturn(secondBasicDTO);

            List<UserBasicOutputDTO> users = eventService.getUsersAssignedToEvent(event1.getId());

            assertEquals(List.of(firstBasicDTO, secondBasicDTO), users);
        }
    }
    @Nested
    public class removeUserFromAllEventsTest {

        @Test
        void correctlyRemoveUserFromAllFutureEventsTest() {
            // Given
            Event futureEvent1 = new Event(
                    LocalDateTime.now().plusDays(5),
                    "Future Event 1",
                    "",
                    "Studio A",
                    3,
                    testCreator,
                    testCreator,
                    EventType.KWF,
                    EventStatus.PLANNED
            );
            setId(futureEvent1, 20L);
            futureEvent1.setNumberOfAssignedPeople(2);
            futureEvent1.getAssignedUsers().add(testParticipant);
            futureEvent1.getAssignedUsers().add(testSecondParticipant);

            Event futureEvent2 = new Event(
                    LocalDateTime.now().plusDays(15),
                    "Future Event 2",
                    "",
                    "Studio B",
                    2,
                    testCreator,
                    testCreator,
                    EventType.AKRE,
                    EventStatus.PLANNED
            );
            setId(futureEvent2, 21L);
            futureEvent2.setNumberOfAssignedPeople(1);
            futureEvent2.getAssignedUsers().add(testParticipant);

            List<Event> futureEvents = List.of(futureEvent1, futureEvent2);

            when(userService.getUserOrThrowException(testParticipant.getId())).thenReturn(testParticipant);
            when(eventRepository.findByAssignedUsersContainingAndDateTimeBetween(
                    eq(testParticipant),
                    any(LocalDateTime.class),
                    eq(LocalDateTime.MAX)
            )).thenReturn(futureEvents);

            // When
            eventService.removeUserFromAllEvents(testParticipant.getId());

            // Then
            verify(eventRepository, times(2)).save(eventArgumentCaptor.capture());
            List<Event> capturedEvents = eventArgumentCaptor.getAllValues();

            // Verify first event
            Event capturedEvent1 = capturedEvents.get(0);
            assertFalse(capturedEvent1.getAssignedUsers().contains(testParticipant));
            assertEquals(1, capturedEvent1.getNumberOfAssignedPeople());
            assertTrue(capturedEvent1.getAssignedUsers().contains(testSecondParticipant));

            // Verify second event
            Event capturedEvent2 = capturedEvents.get(1);
            assertFalse(capturedEvent2.getAssignedUsers().contains(testParticipant));
            assertEquals(0, capturedEvent2.getNumberOfAssignedPeople());
        }

        @Test
        void userNotAssignedToAnyFutureEventsTest() {
            // Given
            when(userService.getUserOrThrowException(testParticipant.getId())).thenReturn(testParticipant);
            when(eventRepository.findByAssignedUsersContainingAndDateTimeBetween(
                    eq(testParticipant),
                    any(LocalDateTime.class),
                    eq(LocalDateTime.MAX)
            )).thenReturn(new ArrayList<>());

            // When
            eventService.removeUserFromAllEvents(testParticipant.getId());

            // Then
            verify(eventRepository, never()).save(any());
        }

        @Test
        void nonExistingUserTest() {
            // Given
            when(userService.getUserOrThrowException(999L))
                    .thenThrow(new EntityException("User not found"));

            // When & Then
            assertThrows(EntityException.class,
                    () -> eventService.removeUserFromAllEvents(999L));
            verify(eventRepository, never()).save(any());
        }

        @Test
        void doesNotRemovePastEventsTest() {
            // Given
            Event pastEvent = new Event(
                    LocalDateTime.now().minusDays(5),
                    "Past Event",
                    "",
                    "Studio D",
                    1,
                    testCreator,
                    testCreator,
                    EventType.KWF,
                    EventStatus.COMPLETED
            );
            setId(pastEvent, 30L);
            pastEvent.setNumberOfAssignedPeople(1);
            pastEvent.getAssignedUsers().add(testParticipant);

            when(userService.getUserOrThrowException(testParticipant.getId())).thenReturn(testParticipant);

            when(eventRepository.findByAssignedUsersContainingAndDateTimeBetween(
                    eq(testParticipant),
                    any(LocalDateTime.class),
                    eq(LocalDateTime.MAX)
            )).thenReturn(new ArrayList<>());

            // When
            eventService.removeUserFromAllEvents(testParticipant.getId());

            // Then
            verify(eventRepository, never()).save(any());
        }
    }
    @Nested
    public class getFilteredEventsTest {

        InputEventFilterDTO filter1;
        InputEventFilterDTO filter2;
        InputEventFilterDTO filter3;
        Event event1;

        @BeforeEach
        void setUp() {
            event1 = new Event(
                    LocalDateTime.now().minusDays(5),
                    "Past Event",
                    "",
                    "Studio D",
                    1,
                    testCreator,
                    testCreator,
                    EventType.KWF,
                    EventStatus.COMPLETED
            );

            filter1 = new InputEventFilterDTO(
                    1,
                    10,
                    "dog",
                    "dog",
                    EventStatus.PLANNED,
                    EventType.KWF,
                    true,
                    LocalDate.now(),
                    LocalDate.now().minusDays(2)
            );

            filter2 = new InputEventFilterDTO(
                    1,
                    10,
                    "dog",
                    "dog",
                    EventStatus.PLANNED,
                    EventType.KWF,
                    true,
                    LocalDate.now(),
                    LocalDate.now().plusDays(2)
            );

            filter3 = new InputEventFilterDTO(
                    1,
                    10,
                     null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null
            );
        }

        @Test
        void shouldThrowExceptionWhenDatesAreInvalid() {

            assertThrows(ValidationException.class, () -> eventService.getFilteredEvents(filter1));
        }

        @Test
        void shouldReturnMappedPageWhenFilterIsValid() {
            Page<Event> eventPage = new PageImpl<>(List.of(event1));

            when(eventRepository.findFilteredEvents(
                    eq("%dog%"), eq("%dog%"), any(), any(), eq(true),
                    any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)
            )).thenReturn(eventPage);


            // When
            Page<OutputEventDTO> result = eventService.getFilteredEvents(filter2);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(eventRepository).findFilteredEvents(
                    eq("%dog%"),
                    eq("%dog%"),
                    eq(EventStatus.PLANNED),
                    eq(EventType.KWF),
                    eq(true),
                    eq(filter2.dateFrom().atStartOfDay()),
                    eq(filter2.dateTo().atTime(LocalTime.MAX)),
                    any(Pageable.class)
            );
        }

        @Test
        void shouldHandleNullFilters() {

            when(eventRepository.findFilteredEvents(
                    eq("%"), eq("%"), any(), any(), anyBoolean(),
                    isNull(), isNull(), any(Pageable.class)
            )).thenReturn(Page.empty());

            // When
            eventService.getFilteredEvents(filter3);

            // Then
            verify(eventRepository).findFilteredEvents(
                    eq("%"), eq("%"), isNull(), isNull(), eq(false), isNull(), isNull(), any(Pageable.class)
            );
        }
    }
}
