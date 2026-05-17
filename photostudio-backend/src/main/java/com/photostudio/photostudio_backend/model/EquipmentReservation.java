package com.photostudio.photostudio_backend.model;

import com.photostudio.photostudio_backend.dto.equipmentReservation.EquipmentReservationDTO;
import com.photostudio.photostudio_backend.model.enums.EquipmentReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "equipment_reservation")
public class EquipmentReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    @Setter
    private LocalDateTime endDate;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="reviewer_user_id")
    private User reviewer;

    @Setter
    @JoinColumn(name="event_request_id")
    @OneToOne(fetch = FetchType.LAZY)
    private EventRequest eventRequest;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentReservationStatus status;

    @Setter
    private String comment;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "equipmentReservation")
    private Set<EquipmentReservationItem> equipmentReservationItems;

    @Getter
    private boolean isPrivate;

    @Getter
    private boolean isUrgent;


    public EquipmentReservation(LocalDateTime startDate, LocalDateTime endDate, User creator, String comment, Boolean isPrivate, Boolean isUrgent) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.creator = creator;
        this.status = EquipmentReservationStatus.NOT_RESOLVED;
        this.equipmentReservationItems = new HashSet<>();
        this.comment = comment;
        this.isPrivate = isPrivate;
        this.isUrgent = isUrgent;
    }

    public EquipmentReservation(EquipmentReservationDTO dto, User user) {
        this(
                dto.start(),
                dto.end(),
                user,
                dto.comment(),
                dto.isPrivate(),
                dto.isUrgent()
        );
    }

    public void addEquipment(EquipmentReservationItem equipmentReservationItem) {
        this.equipmentReservationItems.add(equipmentReservationItem);
    }

    public void removeEquipment(EquipmentReservationItem equipmentReservationItem) {
        this.equipmentReservationItems.remove(equipmentReservationItem);
    }
}
