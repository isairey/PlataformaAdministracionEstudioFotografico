package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.model.User;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserFacade {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserFacade.class);

    private final UserService userService;
    private final EventService eventService;
    private final EventRequestService eventRequestService;

    public DeleteUserFacade(UserService userService, EventService eventService, EventRequestService eventRequestService) {
        this.userService = userService;
        this.eventService = eventService;
        this.eventRequestService = eventRequestService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    @Transactional
    public void deleteYourAccount() {
        User user = userService.getLoggedUser();
        // add removal of all equipment reservations
        eventRequestService.cancelAllUserRequests(user.getId());
        eventService.removeUserFromAllEvents(user.getId());
        userService.deleteYourAccount();
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteUser(Long userId) {
        // add removal of all equipment reservations
        eventRequestService.cancelAllUserRequests(userId);
        eventService.removeUserFromAllEvents(userId);
        userService.deleteUser(userId);
    }

}
