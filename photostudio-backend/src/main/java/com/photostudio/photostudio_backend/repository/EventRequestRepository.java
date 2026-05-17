package com.photostudio.photostudio_backend.repository;

import com.photostudio.photostudio_backend.model.EventRequest;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {

    @EntityGraph(attributePaths = {"event"})
    List<EventRequest> findByEvent_IdAndStatus(Long eventId, ReservationStatus status);

    List<EventRequest> findByUserAndStatus(User user, ReservationStatus status);

    boolean existsByEvent_IdAndUserAndStatusIn(Long eventId, User user, List<ReservationStatus> statuses);

    boolean existsByUser_IdAndEvent_Id_AndStatus(Long userId, Long eventId, ReservationStatus status);

    @Query("""
    SELECT e FROM EventRequest e WHERE
        (:status IS NULL OR :status = e.status)
        AND (CAST(:dateFrom AS timestamp) is NULL OR e.event.dateTime >= :dateFrom)
        AND (CAST(:dateTo AS timestamp) is NULL OR e.event.dateTime <= :dateTo)
        AND lower(e.event.name) LIKE :search
        AND e.user.id = :userID
    """)
    Page<EventRequest> findFilteredRequests(
            @Param("userID") Long userID,
            @Param("search") String search,
            @Param("status") ReservationStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    @Query("""
    SELECT e FROM EventRequest e WHERE
        (:status IS NULL OR :status = e.status)
        AND (CAST(:dateFrom AS timestamp) is NULL OR e.event.dateTime >= :dateFrom)
        AND (CAST(:dateTo AS timestamp) is NULL OR e.event.dateTime <= :dateTo)
        AND lower(e.event.name) LIKE :search
        AND lower(concat(e.user.name, " ", e.user.surname)) LIKE :name
    """)
    Page<EventRequest> findAllFilteredRequests(
            @Param("search") String search,
            @Param("name") String name,
            @Param("status") ReservationStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    List<EventRequest> findByUserAndEvent_DateTimeAfterAndStatusIn(User user, LocalDateTime today, List<ReservationStatus> activeStatuses);
}
