package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.EquipmentOutputDTO;
import com.photostudio.photostudio_backend.dto.equipmentReservation.EquipmentReservationDTO;
import com.photostudio.photostudio_backend.dto.equipmentReservation.EquipmentReservationItemDTO;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.factory.TestDataFactory;
import com.photostudio.photostudio_backend.model.*;
import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.model.enums.EquipmentReservationStatus;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import com.photostudio.photostudio_backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EquipmentReservationServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    EquipmentReservationRepository equipmentReservationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRequestRepository eventRequestRepository;

    @Autowired
    EquipmentRepository equipmentRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EquipmentReservationItemRepository equipmentReservationItemRepository;

    @Autowired
    EquipmentService equipmentService;

    @Autowired
    EquipmentReservationService equipmentReservationService;

    private User admin;
    private User testUser1;
    private User testUser2;
    private Equipment testEquipment1;
    private Equipment testEquipment2;
    private Equipment testEquipment3;
    private Event testEvent1;
    private Event testEvent2;

    @BeforeEach
    public void setUp()
    {
        //clear database
        equipmentReservationItemRepository.deleteAll();
        equipmentReservationRepository.deleteAll();
        eventRequestRepository.deleteAll();
        equipmentRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        admin = TestDataFactory.createAdmin();
        admin.setEnabled(true);
        admin = userRepository.save(admin);

        testUser1 = TestDataFactory.createRegularUser("tomasz", "tomasz@user.com");
        testUser1.setEnabled(true);
        testUser1 = userRepository.save(testUser1);

        testUser2 = TestDataFactory.createRegularUser("MariuszP", "pudzian@user.com");
        testUser2.setEnabled(true);
        testUser2 = userRepository.save(testUser2);

        testEquipment1 = equipmentRepository.save(TestDataFactory.createCamera("Canon 5D Mark III"));
        testEquipment2 = equipmentRepository.save(TestDataFactory.createLens("Canon RF 1500mm"));
        testEquipment3 = equipmentRepository.save(TestDataFactory.createCamera("Canon 5D Mark II"));

        testEvent1 = eventRepository.save(TestDataFactory.createEvent("Koncert", admin,LocalDateTime.now().plusDays(1)));
        testEvent2 = eventRepository.save(TestDataFactory.createEvent("Koncert", admin,LocalDateTime.now().plusDays(2)));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getUsername(), null, List.of())
        );
    };

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }


    private EquipmentReservation createReservationForEvent(
            User user,
            Event event,
            List<Long> equipmentIds,
            LocalDateTime start,
            LocalDateTime end
    ) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of())
        );

        EventRequest eventRequest = eventRequestRepository.save(new EventRequest(event, user));
        EquipmentReservationDTO dto = new EquipmentReservationDTO(
                eventRequest.getId(),
                start,
                end,
                equipmentIds,
                "",
                false,
                false
        );

        return equipmentReservationService.createEquipmentReservation(dto);
    }
    

    
    @Test
    public void getAvailableEquipmentTest() {
        //given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(2);

        //when
        List<EquipmentOutputDTO> availableEquipment = equipmentReservationService.getAvailableEquipment(start, end, true);

        //then
        assertEquals(2, availableEquipment.size());
        assertTrue(availableEquipment
                .stream()
                .anyMatch(item -> item.name().equals(testEquipment1.getName())));

    }

    @Test
    public void getAvailableEventTestExcludes() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser1.getUsername(), null, List.of())
        );
        //given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(2);
        EventRequest eventRequest= eventRequestRepository.save(new EventRequest(testEvent1, testUser1));
        EquipmentReservationDTO dto = new EquipmentReservationDTO(eventRequest.getId(), start, end,List.of(testEquipment1.getId()),"",false,
                false);
        equipmentReservationService.createEquipmentReservation(dto);
        //when
        List<EquipmentOutputDTO> availableEquipment = equipmentReservationService.getAvailableEquipment(start, end,false);

        //then
        assertEquals(1, equipmentReservationService.getAvailableEquipment(start, end, true).size());
        assertFalse(availableEquipment
                .stream()
                .anyMatch(item -> item.name().equals(testEquipment1.getName())));
        assertTrue(availableEquipment
                .stream()
                .anyMatch(item -> item.name().equals(testEquipment2.getName())));

    }

    @Test
    public void throwsWhenCollisionExists() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser1.getUsername(), null, List.of())
        );
        //given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(2);
        EventRequest eventRequest= eventRequestRepository.save(new EventRequest(testEvent1, testUser1));
        EquipmentReservationDTO dto = new EquipmentReservationDTO(eventRequest.getId(), start, end,List.of(testEquipment1.getId()),"",false,
                false);
        equipmentReservationService.createEquipmentReservation(dto);

        //when
        EventRequest eventRequest2= eventRequestRepository.save(new EventRequest(testEvent1, testUser2));
        EquipmentReservationDTO dto2 = new EquipmentReservationDTO(eventRequest2.getId(), start, end,List.of(testEquipment1.getId()),"",false,
                false);


        //then
        assertThrows(ValidationException.class, () -> equipmentReservationService.createEquipmentReservation(dto2));
    }
    @Test
    public void readsEquipmentAfterRejectingReservation() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser1.getUsername(), null, List.of())
        );
        //given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(2);
        EventRequest eventRequest= eventRequestRepository.save(new EventRequest(testEvent1, testUser1));
        EquipmentReservationDTO dto = new EquipmentReservationDTO(eventRequest.getId(), start, end,List.of(testEquipment1.getId()),"",false,
                false);
        EquipmentReservation res = equipmentReservationService.createEquipmentReservation(dto);

        //when
        equipmentReservationService.resolveReservation(res.getId(), Map.of(res.getEquipmentReservationItems().iterator().next().getId(),  false));
        List<EquipmentOutputDTO> availableEquipment = equipmentReservationService.getAvailableEquipment(start, end, true);

        //then
        assertEquals(2, availableEquipment.size());
        assertTrue(availableEquipment
                .stream()
                .anyMatch(item -> item.name().equals(testEquipment1.getName())));

    }

    @Test
    @Transactional
    public void throwsWhenCategoryLimitIsExceeded() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser1.getUsername(), null, List.of())
        );
        //given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(2);
        Equipment secondCamera = equipmentRepository.save(TestDataFactory.createCamera("Nikon D850"));

        userService.changeLimits(testUser1.getId(), EquipmentCategory.CAMERA, 1L);
        assertEquals(1L, userRepository.findById(testUser1.getId()).orElseThrow().getReservationLimits().get(EquipmentCategory.CAMERA));

        EventRequest eventRequest = eventRequestRepository.save(new EventRequest(testEvent1, testUser1));
        EquipmentReservationDTO dto = new EquipmentReservationDTO(
                eventRequest.getId(),
                start,
                end,
                List.of(testEquipment1.getId(), secondCamera.getId()),
                "",
                false,
                false
        );

        //when / then
        assertThrows(ValidationException.class, () -> equipmentReservationService.createEquipmentReservation(dto));
    }

    @Test
    @Transactional
    public void createsReservationWhenLimitsAreIncreased() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser1.getUsername(), null, List.of())
        );
        //given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(2);
        Equipment secondCamera = equipmentRepository.save(TestDataFactory.createCamera("Sony A7 IV"));

        userService.changeLimits(testUser1.getId(), EquipmentCategory.CAMERA, 2L);
        assertEquals(2L, userRepository.findById(testUser1.getId()).orElseThrow().getReservationLimits().get(EquipmentCategory.CAMERA));

        EventRequest eventRequest = eventRequestRepository.save(new EventRequest(testEvent1, testUser1));
        EquipmentReservationDTO dto = new EquipmentReservationDTO(
                eventRequest.getId(),
                start,
                end,
                List.of(testEquipment1.getId(), secondCamera.getId()),
                "",
                false,
                false
        );

        //when
        EquipmentReservation reservation = equipmentReservationService.createEquipmentReservation(dto);

        //then
        assertNotNull(reservation);
        assertEquals(2, reservation.getEquipmentReservationItems().size());
    }

    @Test
    public void rejectAllRequestsForEventRejectsOnlyTargetEventReservations() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        EquipmentReservation targetReservation = createReservationForEvent(
                testUser1,
                testEvent1,
                List.of(testEquipment1.getId(), testEquipment2.getId()),
                start,
                end
        );

        EquipmentReservation cancelledTargetReservation = createReservationForEvent(
                testUser1,
                testEvent1,
                List.of(testEquipment3.getId()),
                start,
                end
        );

        EquipmentReservation otherEventReservation = createReservationForEvent(
                testUser1,
                testEvent2,
                List.of(testEquipment1.getId()),
                start.plusDays(1).plusHours(1),
                end.plusDays(2)
        );

        equipmentReservationService.removeItemFromEquipmentReservation(
                new EquipmentReservationItemDTO(testEquipment2.getId(), targetReservation.getId())
        );

        equipmentReservationService.deleteEquipmentReservation(cancelledTargetReservation.getId());
        EquipmentReservationItem cancelledReservationItem = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(cancelledTargetReservation.getId())
                .get(0);
        cancelledReservationItem.setStatus(ReservationStatus.APPROVED);
        equipmentReservationItemRepository.save(cancelledReservationItem);

        equipmentReservationService.rejectAllRequestsForEvent(testEvent1.getId());

        EquipmentReservation updatedTarget = equipmentReservationRepository.findById(targetReservation.getId()).orElseThrow();
        EquipmentReservation updatedCancelledTarget = equipmentReservationRepository.findById(cancelledTargetReservation.getId()).orElseThrow();
        EquipmentReservation updatedOtherEvent = equipmentReservationRepository.findById(otherEventReservation.getId()).orElseThrow();

        assertEquals(EquipmentReservationStatus.RESOLVED, updatedTarget.getStatus());
        List<EquipmentReservationItem> targetItems = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(updatedTarget.getId());
        assertEquals(1,
                targetItems.stream()
                        .filter(i -> i.getStatus() == ReservationStatus.REJECTED)
                        .count());
        assertEquals(1,
                targetItems.stream()
                        .filter(i -> i.getStatus() == ReservationStatus.CANCELLED)
                        .count());

        assertEquals(EquipmentReservationStatus.CANCELLED, updatedCancelledTarget.getStatus());
        List<EquipmentReservationItem> cancelledTargetItems = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(updatedCancelledTarget.getId());
        assertTrue(cancelledTargetItems.stream()
                .allMatch(i -> i.getStatus() == ReservationStatus.APPROVED));

        assertEquals(EquipmentReservationStatus.NOT_RESOLVED, updatedOtherEvent.getStatus());
        List<EquipmentReservationItem> otherEventItems = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(updatedOtherEvent.getId());
        assertTrue(otherEventItems.stream()
                .allMatch(i -> i.getStatus() == ReservationStatus.PENDING));
    }

    @Test
    public void rejectAllUserRequestsRejectsOnlyTargetUserReservations() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        EquipmentReservation userOneReservation = createReservationForEvent(
                testUser1,
                testEvent1,
                List.of(testEquipment1.getId(), testEquipment2.getId()),
                start,
                end
        );

        EquipmentReservation userTwoReservation = createReservationForEvent(
                testUser2,
                testEvent2,
                List.of(testEquipment1.getId()),
                start.plusDays(1).plusHours(1),
                end.plusDays(2)
        );

        EquipmentReservationItem userOneFirstItem = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(userOneReservation.getId())
                .stream()
                .filter(i -> i.getEquipment().getId().equals(testEquipment1.getId()))
                .findFirst()
                .orElseThrow();
        userOneFirstItem.setStatus(ReservationStatus.APPROVED);
        equipmentReservationItemRepository.save(userOneFirstItem);

        equipmentReservationService.rejectAllUserRequests(testUser1.getId());

        EquipmentReservation updatedUserOneReservation = equipmentReservationRepository.findById(userOneReservation.getId()).orElseThrow();
        EquipmentReservation updatedUserTwoReservation = equipmentReservationRepository.findById(userTwoReservation.getId()).orElseThrow();

        assertEquals(EquipmentReservationStatus.RESOLVED, updatedUserOneReservation.getStatus());
        List<EquipmentReservationItem> userOneItems = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(updatedUserOneReservation.getId());
        assertTrue(userOneItems.stream()
                .allMatch(i -> i.getStatus() == ReservationStatus.REJECTED));

        assertEquals(EquipmentReservationStatus.NOT_RESOLVED, updatedUserTwoReservation.getStatus());
        List<EquipmentReservationItem> userTwoItems = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(updatedUserTwoReservation.getId());
        assertTrue(userTwoItems.stream()
                .allMatch(i -> i.getStatus() == ReservationStatus.PENDING));
    }

    @Test
    public void rejectAllForEventRequestRejectsOnlyMatchingReservation() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser1.getUsername(), null, List.of())
        );

        EventRequest targetedEventRequest = eventRequestRepository.save(new EventRequest(testEvent1, testUser1));
        EquipmentReservation targetedReservation = equipmentReservationService.createEquipmentReservation(
                new EquipmentReservationDTO(
                        targetedEventRequest.getId(),
                        start,
                        end,
                        List.of(testEquipment1.getId()),
                        "",
                        false,
                        false
                )
        );

        EventRequest otherEventRequest = eventRequestRepository.save(new EventRequest(testEvent1, testUser1));
        EquipmentReservation otherReservation = equipmentReservationService.createEquipmentReservation(
                new EquipmentReservationDTO(
                        otherEventRequest.getId(),
                        start.plusHours(3),
                        end.plusHours(3),
                        List.of(testEquipment2.getId()),
                        "",
                        false,
                        false
                )
        );

        equipmentReservationService.rejectAllForEventRequest(targetedEventRequest.getId());

        EquipmentReservation updatedTargetedReservation = equipmentReservationRepository.findById(targetedReservation.getId()).orElseThrow();
        EquipmentReservation updatedOtherReservation = equipmentReservationRepository.findById(otherReservation.getId()).orElseThrow();

        assertEquals(EquipmentReservationStatus.RESOLVED, updatedTargetedReservation.getStatus());
        List<EquipmentReservationItem> targetedItems = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(updatedTargetedReservation.getId());
        assertTrue(targetedItems.stream()
                .allMatch(i -> i.getStatus() == ReservationStatus.REJECTED));

        assertEquals(EquipmentReservationStatus.NOT_RESOLVED, updatedOtherReservation.getStatus());
        List<EquipmentReservationItem> otherItems = equipmentReservationItemRepository
                .findItemsByEquipmentReservation_id(updatedOtherReservation.getId());
        assertTrue(otherItems.stream()
                .allMatch(i -> i.getStatus() == ReservationStatus.PENDING));
    }

    
}


