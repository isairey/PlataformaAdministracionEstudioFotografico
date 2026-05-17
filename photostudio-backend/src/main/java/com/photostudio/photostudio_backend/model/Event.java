package com.photostudio.photostudio_backend.model;

import com.photostudio.photostudio_backend.model.enums.EventStatus;
import com.photostudio.photostudio_backend.model.enums.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    private String description;

    @Setter
    @Column(nullable = false)
    private String location;

    @Setter
    @Column(nullable = false)
    private int numberOfPeopleRequired;

    @Setter
    @Column(nullable = false)
    private int numberOfAssignedPeople;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "assigned_users",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "users_id")
    )
    private List<User> assignedUsers;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modifier_user_id", nullable = false)
    private User lastModifier;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType type;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;



    public Event(LocalDateTime dateTime, String name, String description, String location, int numberOfPeopleRequired,
                 User creator, User lastModifier, EventType type, EventStatus status) {
        this.dateTime = dateTime;
        this.name = name;
        this.description = description;
        this.location = location;
        this.numberOfPeopleRequired = numberOfPeopleRequired;
        this.numberOfAssignedPeople = 0;
        this.creator = creator;
        this.lastModifier = lastModifier;
        this.type = type;
        this.status = status;
        this.assignedUsers = new ArrayList<>();{
        };
    }
}
