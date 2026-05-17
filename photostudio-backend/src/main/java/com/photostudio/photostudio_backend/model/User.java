package com.photostudio.photostudio_backend.model;

import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private String surname;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @Column(nullable = false)
    private String passwordHash;

    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Setter
    @Column(nullable = false)
    private boolean activeMember;

    @Setter
    private String phoneNumber;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Setter
    @Column(nullable = false)
    private boolean deleted;

    @Setter
    @Column(nullable = false)
    private boolean enabled;

    @ElementCollection
    @CollectionTable(name = "user_reservation_limits",
            joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "equipment_category")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "max_limit")
    @Setter
    private Map<EquipmentCategory, Long> reservationLimits = new HashMap<>();

    @Version
    private Long version;

    public User(String name, String surname, String email, String passwordHash,
                String username, String phoneNumber, UserRole role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
        this.activeMember = false;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.deleted = false;
        this.enabled = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.deleted;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.deleted;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.deleted;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getFullName() {
        return this.name + " " + this.surname;
    }
}
