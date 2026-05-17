package com.photostudio.photostudio_backend.model;

import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable=false)
    private String name;

    @Setter
    @Column(nullable=false)
    private boolean activeMembers;

    @Setter
    @Column(nullable = false)
    private boolean statutoryEvent;

    @Setter
    @Column(nullable = false)
    private EquipmentCategory equipmentCategory;

    @Setter
    @Column(nullable = false)
    private boolean deleted;

    public Equipment(String name, boolean activeMembers, boolean statutoryEvent, EquipmentCategory equipmentCategory) {
        this.name = name;
        this.activeMembers = activeMembers;
        this.statutoryEvent = statutoryEvent;
        this.equipmentCategory = equipmentCategory;
        this.deleted = false;
    }
}
