package com.photostudio.photostudio_backend.repository;

import com.photostudio.photostudio_backend.model.Equipment;
import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByNameAndDeletedIsFalse(String name);

    Optional<Equipment> findByIdAndDeletedIsFalse(Long id);

    List<Equipment> findByActiveMembersEqualsAndStatutoryEventEqualsAndDeletedIsFalse(boolean activeMembers, boolean statutoryEvent);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdWithPessimisticWriteLock(Long id);

    @Query("SELECT e FROM Equipment e WHERE " +
           "e.deleted = false AND " +
           "(:activeOnly = false OR e.activeMembers = true) AND " +
           "(:statutoryOnly = false OR e.statutoryEvent = true) AND " +
           "(:name = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR e.equipmentCategory = :category)")
    Page<Equipment> findByFilters(
            @Param("activeOnly") boolean activeOnly,
            @Param("statutoryOnly") boolean statutoryOnly,
            @Param("name") String name,
            @Param("category") EquipmentCategory category,
            Pageable pageable
    );
}
