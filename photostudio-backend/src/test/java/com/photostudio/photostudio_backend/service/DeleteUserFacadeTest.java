package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private EventRequestService eventRequestService;

    @InjectMocks
    private DeleteUserFacade deleteUserFacade;

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldDeleteYourAccountWithCorrectOrder() {
        // Given
        User mockUser = new User();
        setId(mockUser, 1L);
        when(userService.getLoggedUser()).thenReturn(mockUser);

        // When
        deleteUserFacade.deleteYourAccount();

        InOrder inOrder = inOrder(eventRequestService, eventService, userService);
        inOrder.verify(eventRequestService).cancelAllUserRequests(1L);
        inOrder.verify(eventService).removeUserFromAllEvents(1L);
        inOrder.verify(userService).deleteYourAccount();
    }

    @Test
    void shouldDeleteUserByAdminWithCorrectOrder() {
        // Given
        Long userId = 1L;

        // When
        deleteUserFacade.deleteUser(userId);

        // Then
        InOrder inOrder = inOrder(eventRequestService, eventService, userService);
        inOrder.verify(eventRequestService).cancelAllUserRequests(userId);
        inOrder.verify(eventService).removeUserFromAllEvents(userId);
        inOrder.verify(userService).deleteUser(userId);
    }
}
