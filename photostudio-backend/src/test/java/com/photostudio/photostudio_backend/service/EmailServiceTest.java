package com.photostudio.photostudio_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    public class AsyncMailSenderTest {

        @Mock
        JavaMailSender mailSender;

        @InjectMocks
        AsyncMailSender asyncMailSender;

        @Mock
        private MimeMessage mimeMessage;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(asyncMailSender, "fromAddress", "test@example.com");
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        }

        @Test
        void sendHtmlMail_shouldSendEmailSuccessfully() {
            String to = "recipient@example.com";
            String subject = "Test Subject";
            String html = "<h1>Test</h1>";

            asyncMailSender.sendHtmlMail(to, subject, html);
            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        void sendHtmlMail_shouldHandleMessagingException() {
            String to = "recipient@example.com";
            String subject = "Test Subject";
            String html = "<h1>Test</h1>";

            doThrow(new MailSendException("Failed to send")).when(mailSender).send(mimeMessage);

            assertDoesNotThrow(() -> asyncMailSender.sendHtmlMail(to, subject, html));
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }

        @Test
        void sendHtmlMail_shouldHandleExceptionDuringMessageCreation() throws MessagingException {
            String to = "recipient@example.com";
            String subject = "Test Subject";
            String html = "<h1>Test</h1>";

            MimeMessage realMimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);

            doThrow(new MailSendException("Invalid from address"))
                    .when(realMimeMessage).setFrom(anyString());

            asyncMailSender.sendHtmlMail(to, subject, html);
            assertDoesNotThrow(() -> asyncMailSender.sendHtmlMail(to, subject, html));
            verify(mailSender, never()).send(any(MimeMessage.class));
        }
    }
    @Nested
    @ExtendWith(MockitoExtension.class)
    public class MailNotificationServiceTest {

        @Mock
        AsyncMailSender asyncMailSender;

        @Mock
        TemplateEngine templateEngine;

        @InjectMocks
        MailNotificationService mailNotificationService;

        @Test
        void sendEventCancellationEmailTest() {
            String to = "recipient@example.com";
            String eventName = "Test Event";
            LocalDate date = LocalDate.of(2025, 11, 5);

            when(templateEngine.process(eq("eventCancelledMail"), any(Context.class))).thenReturn("<h1>Test</h1>");

            mailNotificationService.sendEventCancellationMail(to, eventName, date);

            verify(templateEngine).process(eq("eventCancelledMail"), any(Context.class));
            verify(asyncMailSender).sendHtmlMail(eq(to), eq("Anulowane wydarzenie: " + eventName), eq("<h1>Test</h1>"));
        }

        @Test
        void sendEventModifiedTest() {
            String to = "recipient@example.com";
            String eventName = "Test Event";
            LocalDate date = LocalDate.of(2025, 11, 5);
            String newName = "New Name";
            String newLocation = "New Location";
            LocalDate newDate = LocalDate.of(2025, 11, 6);

            when(templateEngine.process(eq("eventModifiedMail"), any(Context.class))).thenReturn("<h1>Test</h1>");

            mailNotificationService.sendEventModifiedMail(to, eventName, date, newName, newLocation, newDate);

            verify(templateEngine).process(eq("eventModifiedMail"), any(Context.class));
            verify(asyncMailSender).sendHtmlMail(eq(to), eq("Edytowano wydarzenie: " + eventName), eq("<h1>Test</h1>"));
        }
        @Test
        void sendEquipmentReservationResolvedMailTest() {
            String to = "recipient@example.com";
            String eventName = "Test Event";
            LocalDate date = LocalDate.of(2025, 11, 5);
            Map<String, Boolean> items = Map.of("Camera", true, "Lens", false);

            when(templateEngine.process(eq("equipmentReservationResolvedMail"), any(Context.class)))
                    .thenReturn("<h1>Resolved</h1>");

            mailNotificationService.sendEquipmentReservationResolved(to, eventName, date, items);

            verify(templateEngine).process(eq("equipmentReservationResolvedMail"), any(Context.class));
            verify(asyncMailSender).sendHtmlMail(
                    eq(to),
                    eq("Rezerwacja sprzętu rozpatrzona"),
                    eq("<h1>Resolved</h1>")
            );
        }
        @Test
        void sendEquipmentReservationModifiedMailTest() {
            String to = "recipient@example.com";
            String eventName = "Test Event";
            LocalDate date = LocalDate.of(2025, 11, 5);
            Map<String, Boolean> items = Map.of("Camera", true, "Lens", false);

            when(templateEngine.process(eq("equipmentReservationModifiedMail"), any(Context.class)))
                    .thenReturn("<h1>Modified</h1>");

            mailNotificationService.sendEquipmentReservationModified(to, eventName, date, items);

            verify(templateEngine).process(eq("equipmentReservationModifiedMail"), any(Context.class));
            verify(asyncMailSender).sendHtmlMail(
                    eq(to),
                    eq("Rezerwacja sprzętu zmodyfikowana"),
                    eq("<h1>Modified</h1>")
            );
        }
        @Test
        void sendMailVerificationMailTest() {
            String to = "recipient@example.com";
            String url = "http://example.com/verify";

            when(templateEngine.process(eq("userMailConfirmation"), any(Context.class)))
                    .thenReturn("<h1>Verify</h1>");

            mailNotificationService.sendMailVerificationMail(to, url);

            verify(templateEngine).process(eq("userMailConfirmation"), any(Context.class));
            verify(asyncMailSender).sendHtmlMail(
                    eq(to),
                    eq("Potwierdzenie adresu email"),
                    eq("<h1>Verify</h1>")
            );
        }
        @Test
        void sendPasswordResetMailTest() {
            String to = "recipient@example.com";
            String url = "http://example.com/reset";

            when(templateEngine.process(eq("passwordResetMail"), any(Context.class)))
                    .thenReturn("<h1>Reset</h1>");

            mailNotificationService.sendPasswordResetMail(to, url);

            verify(templateEngine).process(eq("passwordResetMail"), any(Context.class));
            verify(asyncMailSender).sendHtmlMail(
                    eq(to),
                    eq("Reset hasła"),
                    eq("<h1>Reset</h1>")
            );
        }

    }
}
