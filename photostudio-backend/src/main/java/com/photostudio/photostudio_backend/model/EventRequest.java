package com.photostudio.photostudio_backend.model;

import com.photostudio.photostudio_backend.model.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "event_requests")
public class EventRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    @Setter
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Setter
    @Size(max = 500)
    private String comment;

    @OneToOne(mappedBy = "eventRequest")
    private EquipmentReservation equipmentReservation;

    public EventRequest(Event event, User user) {
        this.event = event;
        this.user = user;
        this.status = ReservationStatus.PENDING;
    }
}
