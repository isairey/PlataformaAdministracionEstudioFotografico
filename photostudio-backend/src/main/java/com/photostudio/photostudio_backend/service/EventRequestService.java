package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.eventRequest.AllEventRequestFilterDTO;
import com.photostudio.photostudio_backend.dto.eventRequest.EventRequestDTO;
import com.photostudio.photostudio_backend.dto.eventRequest.EventRequestFilterDTO;
import com.photostudio.photostudio_backend.exception.BusinessException;
import com.photostudio.photostudio_backend.exception.EntityException;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.Event;
import com.photostudio.photostudio_backend.model.EventRequest;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import com.photostudio.photostudio_backend.repository.EventRequestRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventRequestService {

    private static final Logger log = LoggerFactory.getLogger(EventRequestService.class);

    private final EventRequestRepository eventRequestRepository;
    private final UserService userService;
    private final EventService eventService;

    public EventRequestService(EventRequestRepository eventRequestRepository,
                               UserService userService, EventService eventService) {
        this.eventRequestRepository = eventRequestRepository;
        this.userService = userService;
        this.eventService = eventService;
    }

    // requests created after attempt to sign up for event
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    @Transactional
    public void createOwnEventRequest(Long eventId) {
        User user = userService.getLoggedUser();
        Event event = eventService.findEventByIdOrThrowException(eventId);

        requestValidator(eventId, user, event);
        log.info("Created own event request by id: {}, for event: {}", user.getId(), event.getId());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    @Transactional
    public boolean doesEventRequestAlreadyExist(Long eventId) {
        User user = userService.getLoggedUser();
        List<ReservationStatus> existing = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);
        return eventRequestRepository.existsByEvent_IdAndUserAndStatusIn(eventId, user, existing);
    }

    // requests created automatically after closing concert questionary
    @Transactional
    protected Long createAutomatedEventRequest(Long userId, Long eventId) {
        User user = userService.getUserOrThrowException(userId);
        Event event = eventService.findEventByIdOrThrowException(eventId);

        Long eventRequestId = requestValidator(eventId, user, event);
        log.info("Created automated event request by id: {}, for event: {}", user.getId(), event.getId());

        return eventRequestId;
    }

    private Long requestValidator(Long eventId, User user, Event event) {
        if (event.getDateTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot request for past events");
        }
        if (event.getAssignedUsers().contains(user)) {
            throw new ValidationException("User is already assigned to this event");
        }
        if (!event.getStatus().equals(EventStatus.PLANNED)) {
            throw new ValidationException("Event is not pending");
        }
        if (eventRequestRepository.existsByUser_IdAndEvent_Id_AndStatus(user.getId(), eventId, ReservationStatus.PENDING)) {
            throw new ValidationException("User already applied to this event");
        }
        if (event.getNumberOfAssignedPeople() >= event.getNumberOfPeopleRequired()) {
            throw new ValidationException("Event is fully covered");
        }

        EventRequest eventRequest = new EventRequest(event, user);
        eventRequestRepository.save(eventRequest);
        return eventRequest.getId();
    }

    // reject users request as an admin or moderator
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    protected void rejectAllRequestsForEvent(Long eventId) {
        List<EventRequest> requests_pending = eventRequestRepository
                .findByEvent_IdAndStatus(eventId, ReservationStatus.PENDING);
        List<EventRequest> requests_accepted = eventRequestRepository
                .findByEvent_IdAndStatus(eventId, ReservationStatus.APPROVED);

        requests_pending.forEach(request -> request.setStatus(ReservationStatus.REJECTED));
        eventRequestRepository.saveAll(requests_pending);
        requests_accepted.forEach(request -> request.setStatus(ReservationStatus.REJECTED));
        eventRequestRepository.saveAll(requests_accepted);

        log.info("Canceled requests for event: {}", eventId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    @Transactional
    protected void cancelOwnEventRequest(Long eventRequestId) {
        User user = userService.getLoggedUser();
        EventRequest request = findEventRequestById(eventRequestId);
        if (!user.getId().equals(request.getUser().getId())) {
            throw new BusinessException("Cannot reject event request as it does not belong to user");
        }
        if (request.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("Cannot cancel event request as its status is not PENDING");
        }
        request.setStatus(ReservationStatus.CANCELLED);
        eventRequestRepository.save(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    protected void approveEventRequest(Long eventRequestId) {
        EventRequest request = findEventRequestById(eventRequestId);
        if (request.getStatus() == ReservationStatus.APPROVED || request.getStatus() == ReservationStatus.CANCELLED ) {
            throw new BusinessException("Cannot approve event request as its status is not PENDING or REJECTED");
        }
        request.setStatus(ReservationStatus.APPROVED);
        eventRequestRepository.save(request);
        log.info("Approved event request by id: {}, for event: {}", request.getId(), request.getEvent().getId());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    protected void rejectEventRequest(Long eventRequestId) {
        EventRequest request = findEventRequestById(eventRequestId);
        if (request.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new BusinessException("Cannot reject event request as its status is CANCELLED");
        }
        request.setStatus(ReservationStatus.REJECTED);
        eventRequestRepository.save(request);
        log.info("Rejected event request by id: {}, for event: {}", request.getId(), request.getEvent().getId());
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    protected void cancelAllUserRequests(Long userId) {
        User user = userService.getUserOrThrowException(userId);
        List<EventRequest> requests = eventRequestRepository.findByUserAndStatus(user, ReservationStatus.PENDING);

        requests.forEach(request -> request.setStatus(ReservationStatus.CANCELLED));
        eventRequestRepository.saveAll(requests);
        log.info("Canceled all own requests for user {}", user.getId());
    }

    // INFORMATION REGARDING EVENT REQUESTS

    @Transactional(readOnly =
            true)
    public Page<EventRequestDTO> getFilteredRequests(EventRequestFilterDTO filter) {

        User user = userService.getLoggedUser();

        Pageable pageable = PageRequest.of(
                filter.page(),
                filter.pageSize(),
                Sort.by("event.dateTime").ascending()
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

        return eventRequestRepository.findFilteredRequests(
                user.getId(),
                search,
                filter.status(),
                fromDate,
                toDate,
                pageable
        ).map(this::mapEventRequestToDTO);
    }

    @Transactional(readOnly = true)
    public Page<EventRequestDTO> getAllFilteredRequests(AllEventRequestFilterDTO filter) {

        Pageable pageable = PageRequest.of(
                filter.page(),
                filter.pageSize(),
                Sort.by("event.dateTime").ascending()
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

        String name = "%";
        if (filter.name() != null) {
            name = '%' + filter.name().toLowerCase() + '%';
        }

        return eventRequestRepository.findAllFilteredRequests(
                search,
                name,
                filter.status(),
                fromDate,
                toDate,
                pageable
        ).map(this::mapEventRequestToDTO);
    }

    protected EventRequest findEventRequestById(Long eventRequestId) {
        return eventRequestRepository.findById(eventRequestId)
                .orElseThrow(() -> {
                    log.error("Event request not found by id: {}", eventRequestId);
                    return new EntityException("Event request not found");
                });
    }
    private EventRequestDTO mapEventRequestToDTO(EventRequest eventRequest) {
        return new EventRequestDTO(
                eventRequest.getId(),
                eventRequest.getEvent().getId(),
                eventRequest.getUser().getId(),
                eventRequest.getEvent().getName(),
                eventRequest.getEvent().getDateTime().toLocalDate(),
                eventRequest.getEvent().getLocation(),
                eventRequest.getCreatedDateTime().toLocalDate(),
                eventRequest.getCreatedDateTime().toLocalTime(),
                eventRequest.getComment(),
                eventRequest.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<EventRequestDTO> getAllFutureActiveEventRequests() {
        User user = userService.getLoggedUser();
        LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        List<ReservationStatus> activeStatuses = List.of(
                ReservationStatus.PENDING,
                ReservationStatus.APPROVED
        );

        List<EventRequest> requests = eventRequestRepository.findByUserAndEvent_DateTimeAfterAndStatusIn(
                user,
                today,
                activeStatuses
        );
        return requests.stream()
                .map(this::mapEventRequestToDTO)
                .sorted(Comparator.comparing(EventRequestDTO::eventDate))
                .collect(Collectors.toList());
    }
}
