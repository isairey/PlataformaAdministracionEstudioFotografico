package com.photostudio.photostudio_backend.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
public class MailNotificationService {

    private final TemplateEngine templateEngine;
    private final AsyncMailSender asyncMailSender;

    public MailNotificationService(TemplateEngine templateEngine, AsyncMailSender asyncMailSender) {
        this.templateEngine = templateEngine;
        this.asyncMailSender = asyncMailSender;
    }

    public void sendEventCancellationMail(String to, String eventName, LocalDate date) {
        String formattedDate = dateFormat(date);

        Context context = new Context();
        context.setVariable("eventDate", formattedDate);
        context.setVariable("eventName", eventName);

        String html = templateEngine.process("eventCancelledMail", context);
        String subject = "Anulowane wydarzenie: " + eventName;

        asyncMailSender.sendHtmlMail(to, subject, html);
    }

    public void sendEventModifiedMail(
            String to, String eventName, LocalDate date, String newName, String newLocation, LocalDate newDate) {
        String formattedDate = dateFormat(date);
        String newFormattedDate = dateFormat(newDate);

        Context context = new Context();
        context.setVariable("eventDate", formattedDate);
        context.setVariable("eventName", eventName);
        context.setVariable("newEventDate", newFormattedDate);
        context.setVariable("newEventName", newName);
        context.setVariable("newEventLocation", newLocation);

        String html = templateEngine.process("eventModifiedMail", context);
        String subject = "Edytowano wydarzenie: " + eventName;

        asyncMailSender.sendHtmlMail(to, subject, html);
    }

    public void sendEquipmentReservationModified(String to, String eventName, LocalDate date, Map<String, Boolean> items) {
        String formattedDate = dateFormat(date);
        Context context = new Context();
        context.setVariable("startDate", formattedDate);
        context.setVariable("eventName", eventName);
        context.setVariable("items", items);

        String html = templateEngine.process("equipmentReservationModifiedMail", context);

        String subject = "Rezerwacja sprzętu zmodyfikowana";

        asyncMailSender.sendHtmlMail(to, subject, html);
    }

    public void sendEquipmentReservationResolved(String to, String eventName, LocalDate date, Map<String, Boolean> items) {
        String formattedDate = dateFormat(date);
        Context context = new Context();
        context.setVariable("startDate", formattedDate);
        context.setVariable("eventName", eventName);
        context.setVariable("items", items);

        String html = templateEngine.process("equipmentReservationResolvedMail", context);

        String subject = "Rezerwacja sprzętu rozpatrzona";

        asyncMailSender.sendHtmlMail(to, subject, html);
    }

    public void sendMailVerificationMail(String to, String url) {
        Context context = new Context();
        context.setVariable("CONFIRMATION_URL", url);
        String html = templateEngine.process("userMailConfirmation", context);
        String subject = "Potwierdzenie adresu email";
        asyncMailSender.sendHtmlMail(to, subject, html);
    }

    public void sendPasswordResetMail(String to, String url) {
        Context context = new Context();
        context.setVariable("CONFIRMATION_URL", url);
        String html = templateEngine.process("passwordResetMail", context);
        String subject = "Reset hasła";
        asyncMailSender.sendHtmlMail(to, subject, html);
    }

    private String dateFormat(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("pl"));
        return date.format(formatter);
    }
}
