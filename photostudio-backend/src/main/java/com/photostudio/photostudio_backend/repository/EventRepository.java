package com.photostudio.photostudio_backend.repository;

import com.photostudio.photostudio_backend.model.Event;
import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.enums.EventType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

   List<Event> findByAssignedUsersContainingAndDateTimeBetween(User user, LocalDateTime dateTime1, LocalDateTime dateTime2);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(Long id);

    @Query("""
    SELECT e FROM Event e WHERE
        (:status IS NULL OR e.status = :status)
        AND (:type IS NULL OR e.type = :type)
        AND (CAST(:dateFrom AS timestamp) IS NULL OR e.dateTime >= :dateFrom)
        AND (CAST(:dateTo AS timestamp) IS NULL OR e.dateTime <= :dateTo)
        AND LOWER(e.name) LIKE :search
        AND LOWER(e.location) LIKE :location
        AND (e.numberOfPeopleRequired - e.numberOfAssignedPeople > 0 OR :onlyFreeSpots = false)
""")
    Page<Event> findFilteredEvents(
            @Param("search") String search,
            @Param("location") String location,
            @Param("status") EventStatus status,
            @Param("type") EventType type,
            @Param("onlyFreeSpots") Boolean onlyFreeSpots,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
