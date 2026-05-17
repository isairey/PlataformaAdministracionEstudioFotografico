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
import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.repository.EventRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final MailNotificationService mailNotificationService;
    private final UserService userService;

    public EventService(
            EventRepository eventRepository,
            MailNotificationService mailNotificationService,
            UserService userService) {
        this.eventRepository = eventRepository;
        this.mailNotificationService = mailNotificationService;
        this.userService = userService;
    }

    // In order to create new event em a package or as a singular event
    // user have to be at least moderator
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public void createEvent(@Valid InputEventDTO eventDTO) {
        User creator = userService.getLoggedUser();

        LocalDateTime dateTime = convertDateAndTimeToLocalDateTime(eventDTO.date(), eventDTO.time());
        validateDate(dateTime);

        Event event = new Event(
                dateTime,
                eventDTO.name(),
                eventDTO.description(),
                eventDTO.location(),
                eventDTO.numberOfPeopleRequired(),
                creator,
                creator,
                eventDTO.type(),
                EventStatus.PLANNED
        );
        eventRepository.save(event);
        log.info("Successfully created event: {} (ID: {}) by user: {}", event.getName(), event.getId(), creator.getUsername());
    }

    // delete event end notify by email all participants
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    protected void deleteEvent(Long eventId) {
        User lastModifier = userService.getLoggedUser();

        Event event = findEventByIdOrThrowException(eventId);
        OutputEventDTO eventDTO = convertToDTO(event);

        List<UserFullOutputDTO> users = getUsersAssignedToEventFull(event.getId());
        List<String> usersMail = users.stream().map(UserFullOutputDTO::email).toList();

        for (String userMail : usersMail) {
            mailNotificationService.sendEventCancellationMail(userMail, eventDTO.name(), eventDTO.date());
        }
        event.setStatus(EventStatus.CANCELLED);
        event.setLastModifier(lastModifier);
        eventRepository.save(event);
        log.info("Event cancelled: {} (ID: {}) by user: {}", event.getName(), eventId, lastModifier.getUsername());
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public void completeEvent(Long eventId) {
        User lastModifier = userService.getLoggedUser();
        Event event = findEventByIdOrThrowException(eventId);

        LocalDateTime now = LocalDateTime.now();
        if (event.getDateTime().isAfter(now)) {
            throw new ValidationException("Cannot complete future event");
        }
        event.setStatus(EventStatus.COMPLETED);
        event.setLastModifier(lastModifier);
        eventRepository.save(event);
        log.info("Event completed: {} (ID: {}) by user: {}", event.getName(), eventId, lastModifier.getUsername());
    }

    // update event information and assure that they are correct, send email to all participants
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public void updateEvent(Long eventId, @Valid InputEventDTO eventDTO) {
        User lastModifier = userService.getLoggedUser();
        LocalDateTime dateTime = convertDateAndTimeToLocalDateTime(eventDTO.date(), eventDTO.time());
        Event event = findEventByIdOrThrowException(eventId);

        if(!event.getStatus().equals(EventStatus.PLANNED)) {
            log.warn("Cannot update event {} (ID: {}) with status {}. Requested by: {}", event.getName(), eventId, event.getStatus(), lastModifier.getUsername());
            throw new BusinessException("Cannot update event with status " + event.getStatus());
        }

        if(event.getNumberOfAssignedPeople() > eventDTO.numberOfPeopleRequired()) {
            throw new ValidationException("Cannot update event with number of people required higher than current number of people assigned");
        }

        validateDate(dateTime);
        OutputEventDTO oldEventDTO = convertToDTO(event);
        List<UserFullOutputDTO> users = getUsersAssignedToEventFull(event.getId());
        List<String> usersMail = users.stream().map(UserFullOutputDTO::email).toList();

        event.setName(eventDTO.name());
        event.setDescription(eventDTO.description());
        event.setLocation(eventDTO.location());
        event.setNumberOfPeopleRequired(eventDTO.numberOfPeopleRequired());
        event.setType(eventDTO.type());
        event.setDateTime(dateTime);
        event.setLastModifier(lastModifier);
        eventRepository.save(event);
        log.info("Event updated: {} (ID: {}) by user: {}", event.getName(), eventId, lastModifier.getUsername());

        for (String userMail : usersMail) {
            mailNotificationService.sendEventModifiedMail(userMail, oldEventDTO.name(), oldEventDTO.date(),
                    eventDTO.name(), eventDTO.location(), eventDTO.date());
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    protected void addUserToEvent(Long eventId, Long userId) {
        Event event = findEventByIdOrThrowExceptionWithLock(eventId);
        User user = getUserOrThrowException(userId);

        boolean userAlreadyExists = event.getAssignedUsers().stream()
                .anyMatch(u -> u.getId().equals(userId));
        
        if (userAlreadyExists) {
            log.warn("User {} (ID: {}) already assigned to event {} (ID: {}).", user.getUsername(), userId, event.getName(), eventId);
            throw new BusinessException("User already assigned to this event");
        }
        if (!event.getStatus().equals(EventStatus.PLANNED)) {
            log.warn("Cannot add user to event {} (ID: {}) with status {}.", event.getName(), eventId, event.getStatus());
            throw new BusinessException("Cannot add/remove user to an event with status " + event.getStatus());
        }
        if (event.getNumberOfPeopleRequired() <= event.getNumberOfAssignedPeople()) {
            log.warn("Cannot add user {} (ID: {}) to full event {} (ID: {}).", user.getUsername(), userId, event.getName(), eventId);
            throw new BusinessException("Cannot add another user to this event");
        }
        event.getAssignedUsers().add(user);
        event.setNumberOfAssignedPeople(event.getNumberOfAssignedPeople() + 1);
        eventRepository.save(event);
        log.info("Added user {} (ID: {}) to event {} (ID: {}).", user.getUsername(), userId, event.getName(), eventId);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    protected void removeUserFromEvent(Long eventId, Long userId) {
        Event event = findEventByIdOrThrowExceptionWithLock(eventId);
        User user = getUserOrThrowException(userId);

        boolean userExists = event.getAssignedUsers().stream()
                .anyMatch(u -> u.getId().equals(userId));
        
        if (!userExists) {
            log.warn("User {} (ID: {}) not found in event {} (ID: {}). Cannot remove.", user.getUsername(), userId, event.getName(), eventId);
            throw new BusinessException("User doesn't participate in this event");
        }
        if (!event.getStatus().equals(EventStatus.PLANNED)) {
            log.warn("Cannot remove user from event {} (ID: {}) with status {}.", event.getName(), eventId, event.getStatus());
            throw new BusinessException("Cannot add/remove user to an event with status " + event.getStatus());
        }
        event.getAssignedUsers().removeIf(u -> u.getId().equals(userId));
        event.setNumberOfAssignedPeople(event.getNumberOfAssignedPeople() - 1);
        eventRepository.save(event);
        log.info("Removed user {} (ID: {}) from event {} (ID: {}).", user.getUsername(), userId, event.getName(), eventId);
    }

    @Transactional
    protected void removeUserFromAllEvents(Long userId) {
        User user = getUserOrThrowException(userId);

        List<Event> events = eventRepository.findByAssignedUsersContainingAndDateTimeBetween(user, LocalDateTime.now(), LocalDateTime.MAX);
        events.forEach(event -> {
            event.getAssignedUsers().remove(user);
            event.setNumberOfAssignedPeople(event.getNumberOfAssignedPeople() - 1);
            eventRepository.save(event);
        });
        log.info("Removed user (ID: {}) from all future events.", userId);
    }

    // GET INFORMATION REGARDING EVENTS

    @Transactional(readOnly = true)
    public Page<OutputEventDTO> getFilteredEvents(InputEventFilterDTO filter) {

        Pageable pageable = PageRequest.of(
                filter.page(),
                filter.pageSize(),
                Sort.by("dateTime").ascending()
        );

        LocalDateTime fromDate = filter.dateFrom() != null
                ? filter.dateFrom().atStartOfDay()
                : null;

        LocalDateTime toDate = filter.dateTo() != null
                ? filter.dateTo().atTime(LocalTime.MAX)
                : null;

        if (fromDate != null && toDate != null) {
            if (toDate.isBefore(fromDate)) {
                throw new ValidationException("Date is after start date");
            }
        }

        String search = "%";
        if (filter.search() != null) {
            search = '%' + filter.search().toLowerCase() + '%';
        }
        String location = "%";
        if (filter.location() != null) {
            location = '%' + filter.location().toLowerCase() + '%';
        }

        return eventRepository.findFilteredEvents(
                search,
                location,
                filter.status(),
                filter.type(),
                filter.onlyWithFreeSpots(),
                fromDate,
                toDate,
                pageable
        ).map(this::convertToDTO);
    }

    // Returns list of users assigned to event
    @Transactional(readOnly = true)
    public List<UserBasicOutputDTO> getUsersAssignedToEvent(Long eventId) {
        Event event = findEventByIdOrThrowException(eventId);
        return event.getAssignedUsers().stream()
                .filter(user -> !user.isDeleted())
                .filter(User::isEnabled)
                .map(userService::convertUserToBasicOutputDTO)
                .collect(Collectors.toList());
    }


    // METHODS TO VERIFY CORRECTNESS OF DATA

    private List<UserFullOutputDTO> getUsersAssignedToEventFull(Long eventId) {
        Event event = findEventByIdOrThrowException(eventId);
        return event.getAssignedUsers().stream()
                .filter(user -> !user.isDeleted())
                .filter(User::isEnabled)
                .map(userService::convertUserToOutputDTO)
                .collect(Collectors.toList());
    }
    protected Event findEventByIdOrThrowException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found by id: {}", eventId);
                    return new EntityException("Event not found by id: " + eventId);
                });
    }

    private void validateDate(LocalDateTime dateTime) {
        if(LocalDateTime.now().isAfter(dateTime)) {
            log.warn("Validation failed: Event date is in the past: {}", dateTime);
            throw new ValidationException("Event date is in the past");
        }
    }

    public OutputEventDTO convertToDTO(Event event) {
        LocalDate date = event.getDateTime().toLocalDate();
        LocalTime time = event.getDateTime().toLocalTime();

        return new OutputEventDTO(
                event.getId(),
                date,
                time,
                event.getName(),
                event.getDescription(),
                event.getLocation(),
                event.getNumberOfPeopleRequired(),
                event.getNumberOfAssignedPeople(),
                event.getType(),
                event.getStatus()
        );
    }

    private User getUserOrThrowException(Long id) {
        return userService.getUserOrThrowException(id);
    }

    private LocalDateTime convertDateAndTimeToLocalDateTime(LocalDate date, LocalTime time) {
        LocalTime finalTime = (time == null) ? LocalTime.MIN : time;
        return LocalDateTime.of(date, finalTime);
    }
    private Event findEventByIdOrThrowExceptionWithLock(Long eventId) {
        return eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found by id (with lock): {}", eventId);
                    return new EntityException("Event not found by id: " + eventId);
                });
    }

}