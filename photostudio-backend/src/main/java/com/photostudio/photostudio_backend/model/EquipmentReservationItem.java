package com.photostudio.photostudio_backend.model;

import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "equipment_reservatinon_items")
public class EquipmentReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_reservation_id", nullable = false)
    private EquipmentReservation equipmentReservation;



    public EquipmentReservationItem(EquipmentReservation equipmentReservation,Equipment equipment) {
        this.equipmentReservation = equipmentReservation;
        this.equipment = equipment;
        this.status = ReservationStatus.PENDING;
    }
}
