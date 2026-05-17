package com.photostudio.photostudio_backend.repository;

import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndDeletedIsFalseAndEnabledIsTrue(String username);

    List<User> findAllByDeletedIsFalseAndEnabledIsTrue();

    Optional<User> findByIdAndDeletedIsFalseAndEnabledIsTrue(Long id);

    Optional<User> findByEmailAndDeletedIsFalseAndEnabledIsTrue(String email);

    List<User> findByRoleAndDeletedIsFalseAndEnabledIsTrue(UserRole role);

    boolean existsByUsernameAndDeletedIsFalse(String username);

    boolean existsByEmailAndDeletedIsFalse(String email);

    boolean existsByPhoneNumberAndDeletedIsFalse(String phone);

}
