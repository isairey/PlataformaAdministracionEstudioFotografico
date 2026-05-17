package com.photostudio.photostudio_backend.repository;

import com.photostudio.photostudio_backend.model.EquipmentReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentReservationItemRepository extends JpaRepository<EquipmentReservationItem, Long> {
    List<EquipmentReservationItem> findItemsByEquipmentReservation_id(long id);
}
