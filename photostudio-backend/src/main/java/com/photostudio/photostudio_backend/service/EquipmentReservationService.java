package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.EquipmentOutputDTO;
import com.photostudio.photostudio_backend.dto.equipmentReservation.*;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.*;
import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.model.enums.EquipmentReservationStatus;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import com.photostudio.photostudio_backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EquipmentReservationService {
    private final EquipmentReservationRepository equipmentReservationRepository;
    private final EquipmentReservationItemRepository equipmentReservationItemRepository;
    private final EventRequestService eventRequestService;
    private final EquipmentService equipmentService;
    private final UserService userService;
    private final MailNotificationService mailNotificationService;

    public EquipmentReservationService(EquipmentReservationRepository equipmentReservationRepository, EquipmentReservationItemRepository equipmentReservationItemRepository, EventRequestService eventRequestService, UserService userService, EquipmentService equipmentService, MailNotificationService mailNotificationService) {
        this.equipmentReservationRepository = equipmentReservationRepository;
        this.equipmentReservationItemRepository = equipmentReservationItemRepository;
        this.eventRequestService = eventRequestService;
        this.equipmentService = equipmentService;
        this.userService = userService;
        this.mailNotificationService = mailNotificationService;
    }


    private Map<EquipmentCategory, Long> getLimitsMap(EquipmentReservation er) {
        Map<EquipmentCategory, Long> map = new EnumMap<>(EquipmentCategory.class);
        for (EquipmentCategory category : EquipmentCategory.values()) {
            map.put(category, 0L);
        }

        List<EquipmentCount> counts = equipmentReservationRepository.getLimits(
                er.getCreator().getId(),
                er.getStartDate(),
                er.getEndDate(),
                EquipmentReservationStatus.CANCELLED,
                ReservationStatus.REJECTED,
                ReservationStatus.CANCELLED
        );

        for (EquipmentCount count : counts) {
            map.put(count.category(), count.count());
        }

        return map;
    }

    private void verifyReservationLimits(EquipmentReservation er, List<Long> newEquipmentIds) {
        Map<EquipmentCategory, Long> currentLimits = getLimitsMap(er);
        Map<EquipmentCategory, Long> maxLimits = er.getCreator().getReservationLimits();

        for (Long id : newEquipmentIds) {
            EquipmentCategory category = equipmentService.getEquipmentIdOrThrow(id)
                    .getEquipmentCategory();

            currentLimits.compute(category, (k, v) -> (v == null ? 0L : v) + 1);
        }
        // 1000 will be the default value for now
        for (EquipmentCategory category : currentLimits.keySet()) {
            Long limit = maxLimits.getOrDefault(category, 1000L);
            if (currentLimits.get(category) > limit) {
                throw new ValidationException(String.format("Maksymalny limit osiągnięty: %d dla kategorii %s", limit, category));
            }
        }
    }

    // TODO: verify and modify tests - you can also optimise this solution
    @Transactional
    public EquipmentReservation createEquipmentReservation(EquipmentReservationDTO dto) {
        User user = userService.getLoggedUser();

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new ValidationException("Użytkownik z uprawnieniami superadministratora nie może tworzyć rezerwacji");
        }
        EquipmentReservation eqRes = new EquipmentReservation(dto, user);

        if (dto.eventRequestId() != 0) {
            EventRequest eventRequest = eventRequestService.findEventRequestById(dto.eventRequestId());

            LocalDateTime eventDateTime = eventRequest.getEvent().getDateTime();
            if (dto.start().isAfter(eventDateTime) || dto.end().isBefore(eventDateTime)) {
                throw new ValidationException("Reservation must cover the event duration");
            }
            if (!user.getId().equals(eventRequest.getUser().getId())) {
                throw new ValidationException("User cannot use someone else's event request");
            }
            eqRes.setEventRequest(eventRequest);
        }
        verifyReservationLimits(eqRes, dto.equipmentIDs());

        EquipmentReservation reservation = equipmentReservationRepository.save(eqRes);
        for (Long equipmentId : dto.equipmentIDs()) {
            EquipmentReservationItemDTO item = new EquipmentReservationItemDTO(equipmentId, reservation.getId());
            addItemToEquipmentReservation(item);
        }

        return reservation;
    }

    @Transactional
    public void deleteEquipmentReservation(Long id) {
        EquipmentReservation equipmentReservation = equipmentReservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Equipment reservation not found with id {}", id);
                    return new EntityNotFoundException("Equipment Reservation not found");
                });
        equipmentReservation.setStatus(EquipmentReservationStatus.CANCELLED);

        equipmentReservation.getEquipmentReservationItems().stream()
                .filter(item -> item.getStatus() == ReservationStatus.PENDING
                        || item.getStatus() == ReservationStatus.APPROVED)
                .forEach(item -> item.setStatus(ReservationStatus.CANCELLED));
    }


    private EquipmentReservation findById(long id) {
        return equipmentReservationRepository.findById(id).orElseThrow(() -> {
                log.error(String.format("No equipment reservation found with id %d", id));
                return new EntityNotFoundException("No equipment reservation found with id " + id);
        });
    }

    private Optional<EquipmentReservationItem> getItemById(long equipmentId, EquipmentReservation reservation) {
        return reservation.getEquipmentReservationItems().stream()
                .filter(item -> item.getEquipment().getId().equals(equipmentId))
                .findFirst();
    }


    @Transactional
    public void addItemToEquipmentReservation(EquipmentReservationItemDTO dto) {
        EquipmentReservation reservation = findById(dto.equipmentReservationId());
        Equipment equipment = equipmentService.getEquipmentIdOrThrow(dto.equipmentId());

        if (reservation.getStatus() == EquipmentReservationStatus.CANCELLED) {
            log.error("Equipment reservation has been cancelled");
            throw new EntityNotFoundException("Reservation cancelled");
        }
        if (reservation.getStatus() == EquipmentReservationStatus.RESOLVED) {
            log.error("Equipment reservation has already been resolved");
            throw new EntityNotFoundException("Reservation resolved");
        }

        if (reservation.isPrivate() && equipment.isStatutoryEvent()) {
            throw new ValidationException("This equipment can only be rented for statutory events");
        }

        if(equipmentReservationRepository.existsOverlappingReservation(
                equipment.getId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                ReservationStatus.CANCELLED,
                ReservationStatus.REJECTED,
                EquipmentReservationStatus.CANCELLED
        )) {
            throw new ValidationException("Selected equipment isn't free in given date");
        }
        Optional<EquipmentReservationItem> presentItem = getItemById(dto.equipmentId(), reservation);
        presentItem.ifPresent(equipmentReservationItem -> equipmentReservationItem.setStatus(ReservationStatus.PENDING));

        EquipmentReservationItem equipmentReservationItem = new EquipmentReservationItem(reservation, equipment);
        reservation.addEquipment(equipmentReservationItem);
        equipmentReservationItemRepository.save(equipmentReservationItem);
    }


    @Transactional
    public void removeItemFromEquipmentReservation(EquipmentReservationItemDTO dto) {
        EquipmentReservation reservation = findById(dto.equipmentReservationId());
        EquipmentReservationItem equipmentReservationItem = getItemById(dto.equipmentId(), reservation)
                .orElseThrow(() -> new EntityNotFoundException("Equipment reservation item not found"));
        equipmentReservationItem.setStatus(ReservationStatus.CANCELLED);
    }

    @Transactional
    public List<EquipmentOutputDTO> getAvailableEquipment(
            LocalDateTime start,
            LocalDateTime end,
            Boolean statutory) {
        User user = userService.getLoggedUser();

        return equipmentReservationRepository.getAvailableEquipment(
            start,
            end,
            ReservationStatus.CANCELLED,
            ReservationStatus.REJECTED,
            EquipmentReservationStatus.CANCELLED,
            statutory,
            user.isActiveMember()
        ).stream()
            .map(equipmentService::convertToEquipmentDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentReservationItemOutDTO> getEquipmentReservationItems(long id) {
        return equipmentReservationItemRepository.findItemsByEquipmentReservation_id(id)
                .stream()
                .map(this::toEquipmentReservationItemOutDTO)
                .toList();
    }

    private EquipmentReservationItemOutDTO toEquipmentReservationItemOutDTO(EquipmentReservationItem equipmentReservationItem) {
        Equipment equipment = equipmentReservationItem.getEquipment();
        return new  EquipmentReservationItemOutDTO(
                equipmentReservationItem.getId(),
                equipment.getName(),
                equipment.isActiveMembers(),
                equipment.isStatutoryEvent(),
                equipment.getEquipmentCategory(),
                equipmentReservationItem.getStatus()
        );
    }



    @Transactional
    public void resolveReservation(long id, Map<Long, Boolean> acceptanceMap) {
        EquipmentReservation equipmentReservation = equipmentReservationRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Equipment reservation not found"));

        for (EquipmentReservationItem item : equipmentReservation.getEquipmentReservationItems()) {
            if (!acceptanceMap.containsKey(item.getId())) {
                throw new ValidationException("Equipment reservation item not found");
            }
            if (acceptanceMap.get(item.getId())) {
                item.setStatus(ReservationStatus.APPROVED);
            } else {
                item.setStatus(ReservationStatus.REJECTED);
            }
        }

        equipmentReservation.setStatus(EquipmentReservationStatus.RESOLVED);
        equipmentReservation.setReviewer(userService.getLoggedUser());

        mailNotificationService.sendEquipmentReservationResolved(
                equipmentReservation.getCreator().getEmail(),
                getEventNameFromReservation(equipmentReservation),
                equipmentReservation.getStartDate().toLocalDate(),
                getItemStatusesByReservationId(equipmentReservation.getId())
        );
    }

    @Transactional
    public void modifyReservation(long id, Map<Long, Boolean> acceptanceMap) {
        EquipmentReservation equipmentReservation = equipmentReservationRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Equipment reservation not found"));

        for (EquipmentReservationItem item : equipmentReservation.getEquipmentReservationItems()) {
            if (acceptanceMap.containsKey(item.getId())) {
                if (acceptanceMap.get(item.getId()) && item.getStatus() == ReservationStatus.REJECTED) {
                    if (equipmentReservationRepository.existsOverlappingReservation(
                            item.getEquipment().getId(),
                            equipmentReservation.getStartDate(),
                            equipmentReservation.getEndDate(),
                            ReservationStatus.CANCELLED,
                            ReservationStatus.REJECTED,
                            EquipmentReservationStatus.CANCELLED
                    )) {
                        throw new ValidationException(item.getEquipment().getName() + " is already reserved in given date.");
                    }
                    item.setStatus(ReservationStatus.APPROVED);
                } else if (!acceptanceMap.get(item.getId())) {
                    item.setStatus(ReservationStatus.REJECTED);
                }
            }
        }

        equipmentReservation.setReviewer(userService.getLoggedUser());
        mailNotificationService.sendEquipmentReservationModified(
                equipmentReservation.getCreator().getEmail(),
                getEventNameFromReservation(equipmentReservation),
                equipmentReservation.getStartDate().toLocalDate(),
                getItemStatusesByReservationId(equipmentReservation.getId())
        );
    }

    @Transactional(readOnly = true)
    public List<EquipmentReservationOutDTO> getUserReservations(long creatorId) {

        return equipmentReservationRepository.findByCreator_Id(creatorId)
                .stream()
                .map(this::toEquipmentReservationOutDTO)
                .toList();
    }

    private EquipmentReservationOutDTO toEquipmentReservationOutDTO(EquipmentReservation equipmentReservation) {
        String eventName = equipmentReservation.isPrivate() ? "Wydarzenie prywatne" : "Nagłe wydarzenie";

        if (equipmentReservation.getEventRequest() != null && equipmentReservation.getEventRequest().getEvent() != null) {
            eventName = equipmentReservation.getEventRequest().getEvent().getName();
        }


        return new EquipmentReservationOutDTO(
                equipmentReservation.getId(),
                equipmentReservation.getStatus(),
                equipmentReservation.getCreator().getFullName(),
                eventName,
            equipmentReservation.getComment(),
                equipmentReservation.getStartDate(),
                equipmentReservation.getEndDate(),
                equipmentReservation.getReviewer()!= null ? equipmentReservation.getReviewer().getFullName() : null
        );
    }

    @Transactional(readOnly = true)
    public List<EquipmentReservationOutDTO> getUserReservationsWithinTimeWindow(long userId,LocalDateTime start, LocalDateTime end) {
        return equipmentReservationRepository.findByStartDateBetweenAndCreator_Id(start, end, userId)
                .stream()
                .map(this::toEquipmentReservationOutDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentReservationOutDTO getEquipmentReservation(Long id) {
        EquipmentReservation reservation = equipmentReservationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Equipment Reservation not found"));
        return  toEquipmentReservationOutDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<EquipmentReservationOutDTO> getAllReservations() {
        return equipmentReservationRepository.findAll()
                .stream()
                .map(this::toEquipmentReservationOutDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentReservationOutDTO> getAllReservationsWithinTimeWindow(LocalDateTime start, LocalDateTime end) {
        return equipmentReservationRepository.findByStartDateBetween(start, end)
                .stream()
                .map(this::toEquipmentReservationOutDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public Page<EquipmentReservationOutDTO> getPageByFilters(String creatorFullName, String eventName, String status, String startDate, String endDate, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        EquipmentReservationStatus filterStatus = (!Objects.equals(status, "")) ? EquipmentReservationStatus.valueOf(status) : null;

        LocalDateTime filterStartDate = (!Objects.equals(startDate, "")) ? LocalDate.parse(startDate).atStartOfDay() : null;

        LocalDateTime filterEndDate = (!Objects.equals(endDate, "")) ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : null;

        String filterUserName = (!Objects.equals(creatorFullName, "")) ? creatorFullName : "";
        String filterEventName = (!Objects.equals(eventName, "")) ? eventName : "";

        return equipmentReservationRepository
                .findByFilters(filterUserName, filterEventName, filterStatus, filterStartDate, filterEndDate, pageable)
                .map(this::toEquipmentReservationOutDTO);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentReservationOutDTO> getPageByFiltersForUser(long creatorId, String creatorFullName, String eventName, String status, String startDate, String endDate, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        EquipmentReservationStatus filterStatus = (!Objects.equals(status, "")) ? EquipmentReservationStatus.valueOf(status) : null;

        LocalDateTime filterStartDate = (!Objects.equals(startDate, "")) ? LocalDate.parse(startDate).atStartOfDay() : null;

        LocalDateTime filterEndDate = (!Objects.equals(endDate, "")) ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : null;

        String filterUserName = (!Objects.equals(creatorFullName, "")) ? creatorFullName : "";
        String filterEventName = (!Objects.equals(eventName, "")) ? eventName : "";

        return equipmentReservationRepository
                .findByFiltersAndCreator(creatorId, filterUserName, filterEventName, filterStatus, filterStartDate, filterEndDate, pageable)
                .map(this::toEquipmentReservationOutDTO);
    }

    private String getEventNameFromReservation(EquipmentReservation reservation) {
        if(reservation.isPrivate()){
            return "Wydarzenie prywatne";
        } if (reservation.isUrgent()) {
            return "Nagłe wydarzenie";
        } else  {
            return reservation.getEventRequest().getEvent().getName();
        }
    }

    private Map<String, Boolean> getItemStatusesByReservationId(Long reservationId) {
        List<Object[]> rawResults = equipmentReservationRepository.getItemStatuses(
                reservationId,
                ReservationStatus.APPROVED
        );

        return rawResults.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Boolean) row[1],
                        (existing, replacement) -> existing
                ));
    }

    private void rejectAll(List<EquipmentReservation> reservations) {
        for (EquipmentReservation reservation : reservations) {
            if(reservation.getStatus()!= EquipmentReservationStatus.CANCELLED){
                reservation.setStatus(EquipmentReservationStatus.RESOLVED);
                for (EquipmentReservationItem reservationItem : reservation.getEquipmentReservationItems()) {
                    if (reservationItem.getStatus() != ReservationStatus.CANCELLED) {
                        reservationItem.setStatus(ReservationStatus.REJECTED);
                    }
                }
            }
        }
    }

    @Transactional
    protected void rejectAllRequestsForEvent(Long eventId) {
        List<EquipmentReservation> reservations = equipmentReservationRepository.findAllWithItemsByEventId(eventId);

        rejectAll(reservations);
    }

    @Transactional
    protected void rejectAllUserRequests(Long userId) {
        List<EquipmentReservation> reservations = equipmentReservationRepository.findAllWithItemsByUserId(userId);

        rejectAll(reservations);
    }

    @Transactional
    protected void rejectAllForEventRequest(Long eventRequestId) {
        List<EquipmentReservation> reservations = equipmentReservationRepository.findAllWithItemsByEventRequestId(eventRequestId);

        rejectAll(reservations);
    }
}

