package com.photostudio.photostudio_backend.initializer;

import com.photostudio.photostudio_backend.model.User;
import com.photostudio.photostudio_backend.model.enums.UserRole;
import com.photostudio.photostudio_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.email}")
    private String superAdminEmail;

    @Value("${app.superadmin.password}")
    private String superAdminPassword;

    @Value("${app.superadmin.username}")
    private String superAdminUsername;

    @Value("${app.superadmin.name}")
    private String superAdminName;

    @Value("${app.superadmin.surname}")
    private String superAdminSurname;

    @Value("${app.superadmin.phone}")
    private String superAdminPhone;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        boolean exists = userRepository.findByEmailAndDeletedIsFalseAndEnabledIsTrue(superAdminEmail).isPresent();
        if (!exists) {
            User superAdmin = new User(
                    superAdminName,
                    superAdminSurname,
                    superAdminEmail,
                    passwordEncoder.encode(superAdminPassword),
                    superAdminUsername,
                    superAdminPhone,
                    UserRole.SUPER_ADMIN
            );
            superAdmin.setEnabled(true);
            superAdmin.setActiveMember(true);

            userRepository.save(superAdmin);
            log.info("Super admin created");
        } else {
            log.info("Super Admin already exists");
        }
    }
}