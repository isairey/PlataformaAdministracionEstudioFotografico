package com.photostudio.photostudio_backend.repository;


import com.photostudio.photostudio_backend.dto.equipmentReservation.EquipmentCount;
import com.photostudio.photostudio_backend.model.Equipment;
import com.photostudio.photostudio_backend.model.EquipmentReservation;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.EquipmentReservationStatus;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EquipmentReservationRepository extends JpaRepository<EquipmentReservation, Long> {
    @Query("""
        SELECT COUNT(r) > 0
        FROM EquipmentReservation r
        JOIN r.equipmentReservationItems eqItem
        WHERE r.status != :cancelledEquipmentReservationStatus
        AND r.startDate < :end
        AND r.endDate > :start
        AND eqItem.equipment.id = :equipmentId
        AND eqItem.status != :cancelledStatus
        AND eqItem.status != :rejectedStatus
    """)
    boolean existsOverlappingReservation(
            long equipmentId,
            LocalDateTime start,
            LocalDateTime end,
            ReservationStatus cancelledStatus,
            ReservationStatus rejectedStatus,
            EquipmentReservationStatus cancelledEquipmentReservationStatus
    );

    @Query("""
    SELECT DISTINCT e
    FROM Equipment e
    WHERE
        (e.statutoryEvent = false OR :statutory = true)
    AND
        (e.activeMembers = false OR :activeMember = true)
    AND
        (e.deleted = false)
    AND NOT EXISTS (
        SELECT r
        FROM EquipmentReservation r
        JOIN r.equipmentReservationItems eqItem
        WHERE eqItem.equipment = e
          AND r.status != :cancelledEquipmentReservationStatus
          AND r.startDate < :end
          AND r.endDate > :start
          AND eqItem.status != :cancelledStatus
          AND eqItem.status != :rejectedStatus
    )
""")
    List<Equipment> getAvailableEquipment(
            LocalDateTime start,
            LocalDateTime end,
            ReservationStatus cancelledStatus,
            ReservationStatus rejectedStatus,
            EquipmentReservationStatus cancelledEquipmentReservationStatus,
            Boolean statutory,
            Boolean activeMember
    );

    List<EquipmentReservation> findByCreator(User creator);

    List<EquipmentReservation> findByStatusIs(EquipmentReservationStatus equipmentReservationStatus);

    List<EquipmentReservation> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    List<EquipmentReservation> findByCreator_Id(long creatorId);

    List<EquipmentReservation> findByStartDateBetweenAndCreator_Id(LocalDateTime start, LocalDateTime end, long creatorId);

    @Query("""
        SELECT r FROM EquipmentReservation r
        LEFT JOIN r.eventRequest er
        LEFT JOIN er.event e
        WHERE (:creatorFullName = '' OR LOWER(r.creator.name || ' ' || r.creator.surname) LIKE LOWER('%' || :creatorFullName || '%'))
        AND (:eventName = '' OR LOWER(e.name) LIKE LOWER('%' || :eventName || '%'))
        AND (:status IS NULL OR r.status = :status)
        AND (CAST(:startDate AS TIMESTAMP) IS NULL OR r.startDate >= :startDate)
        AND (CAST(:endDate AS TIMESTAMP) IS NULL OR r.endDate <= :endDate)
        ORDER BY r.startDate DESC
    """)
    Page<EquipmentReservation> findByFilters(
            @Param("creatorFullName") String creatorFullName,
            @Param("eventName") String eventName,
            @Param("status") EquipmentReservationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
        SELECT r FROM EquipmentReservation r
        LEFT JOIN r.eventRequest er
        LEFT JOIN er.event e
        WHERE r.creator.id = :creatorId
        AND (:creatorFullName = '' OR LOWER(r.creator.name || ' ' || r.creator.surname) LIKE LOWER('%' || :creatorFullName || '%'))
        AND (:eventName = '' OR LOWER(e.name) LIKE LOWER('%' || :eventName || '%'))
        AND (:status IS NULL OR r.status = :status)
        AND (CAST(:startDate AS TIMESTAMP) IS NULL OR r.startDate >= :startDate)
        AND (CAST(:endDate AS TIMESTAMP) IS NULL OR r.endDate <= :endDate)
        ORDER BY r.startDate DESC
    """)
    Page<EquipmentReservation> findByFiltersAndCreator(
            @Param("creatorId") long creatorId,
            @Param("creatorFullName") String creatorFullName,
            @Param("eventName") String eventName,
            @Param("status") EquipmentReservationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""

    SELECT new com.photostudio.photostudio_backend.dto.equipmentReservation.EquipmentCount(
          i.equipment.equipmentCategory,
          COUNT(i)
      )
    FROM EquipmentReservation er
    JOIN er.equipmentReservationItems i
    WHERE er.creator.id = :userId
    AND er.startDate < :endDate 
    AND er.endDate > :startDate
    AND i.status NOT IN (:rejectedStatus, :cancelledStatus)
    AND er.status != :cancelledReservationStatus
    GROUP BY i.equipment.equipmentCategory
    """)
    List<EquipmentCount> getLimits(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            EquipmentReservationStatus cancelledReservationStatus,
            ReservationStatus rejectedStatus,
            ReservationStatus cancelledStatus);


    @Query("""
    SELECT i.equipment.name,
           (CASE WHEN i.status = :approvedStatus THEN true ELSE false END)
    FROM EquipmentReservation r
    JOIN r.equipmentReservationItems i
    WHERE r.id = :reservationId
    """)
    List<Object[]> getItemStatuses(
            Long reservationId,
            ReservationStatus approvedStatus
    );

    @Query("""
        SELECT r FROM EquipmentReservation r
        LEFT JOIN FETCH r.equipmentReservationItems
        WHERE r.eventRequest.event.id = :eventId
    """)
    List<EquipmentReservation> findAllWithItemsByEventId(Long eventId);

    @Query("""
        SELECT r FROM EquipmentReservation r
        LEFT JOIN FETCH r.equipmentReservationItems
        WHERE r.creator.id = :userId
    """)
    List<EquipmentReservation> findAllWithItemsByUserId(Long userId);


    @Query("""
        SELECT r FROM EquipmentReservation r
        LEFT JOIN FETCH r.equipmentReservationItems
        WHERE r.eventRequest.id = :eventRequestId
    """)
    List<EquipmentReservation> findAllWithItemsByEventRequestId(Long eventRequestId);
}

