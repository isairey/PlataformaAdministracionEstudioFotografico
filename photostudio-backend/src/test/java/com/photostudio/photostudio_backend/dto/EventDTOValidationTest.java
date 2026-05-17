package com.photostudio.photostudio_backend.dto;

import com.photostudio.photostudio_backend.dto.event.InputEventDTO;
import com.photostudio.photostudio_backend.model.enums.EventType;
import com.photostudio.photostudio_backend.service.EventService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventDTOValidationTest {

    static private Validator validator;

    @BeforeAll
    static void setUpBeforeClass() {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }
    @Mock
    EventService eventService;

    @Test
    void correctEventTest() {
        InputEventDTO eventDTO = new InputEventDTO(
                LocalDate.now(),
                null,
                "Koncert Kult w Klubie Studio",
                "wazne wydarzenie",
                "Klub Studio",
                2,
                EventType.KWF
        );
        InputEventDTO eventDTO2 = new InputEventDTO(
                LocalDate.now(),
                LocalTime.now(),
                "Koncert Kult w Klubie Studio",
                null,
                "Klub Studio",
                2,
                EventType.KWF
        );
        Set<ConstraintViolation<InputEventDTO>> constraintViolations = validator.validate(eventDTO);
        Set<ConstraintViolation<InputEventDTO>> constraintViolations2 = validator.validate(eventDTO2);
        assertTrue(constraintViolations.isEmpty());
        assertTrue(constraintViolations2.isEmpty());
    }

    @Test
    void invalidLocationTest() {
        InputEventDTO eventDTO1 = new InputEventDTO(
                LocalDate.now(),
                LocalTime.now(),
                "Koncert Kult w Klubie Studio",
                "",
                "",
                2,
                EventType.KWF
        );
        InputEventDTO eventDTO2 = new InputEventDTO(
                LocalDate.now(),
                LocalTime.now(),
                "Koncert Kult w Klubie Studio",
                null,
                "text too long text too long text too long text too long text too long text too long" +
                        " text too long text too long text too long text too longtext too long text too long  ",
                2,
                EventType.KWF
        );

        Set<ConstraintViolation<InputEventDTO>> constraintViolations1 = validator.validate(eventDTO1);
        Set<ConstraintViolation<InputEventDTO>> constraintViolations2 = validator.validate(eventDTO2);
        assertFalse(constraintViolations1.isEmpty());
        assertFalse(constraintViolations2.isEmpty());
    }

    @Test
    void invalidNameTest() {
        InputEventDTO eventDTO1 = new InputEventDTO(
                LocalDate.now(),
                LocalTime.now(),
                "",
                null,
                "Klub Studio",
                2,
                EventType.KWF
        );
        InputEventDTO eventDTO3 = new InputEventDTO(
                LocalDate.now(),
                LocalTime.now(),
                "text is going to be too long it has to be shorter than 100 characters" +
                        "text is going to be too long it has to be shorter than 100 characters"+
                        "text is going to be too long it has to be shorter than 100 characters"+
                        "text is going to be too long it has to be shorter than 100 characters"+
                        "text is going to be too long it has to be shorter than 100 characters",
                null,
                "Klub Studio",
                2,
                EventType.KWF
        );
        Set<ConstraintViolation<InputEventDTO>> constraintViolations1 = validator.validate(eventDTO1);
        Set<ConstraintViolation<InputEventDTO>> constraintViolations3 = validator.validate(eventDTO3);
        assertFalse(constraintViolations1.isEmpty());
        assertFalse(constraintViolations3.isEmpty());
    }
    @Test
    void invalidNumberOfPeopleTest() {
        InputEventDTO eventDTO1 = new InputEventDTO(
                LocalDate.now(),
                LocalTime.now(),
                "text text",
                "description 123",
                "Klub Studio",
                21,
                EventType.KWF
        );
        InputEventDTO eventDTO2 = new InputEventDTO(
                LocalDate.now(),
                LocalTime.now(),
                "text text",
                null,
                "Klub Studio",
                -1,
                EventType.KWF
        );
        Set<ConstraintViolation<InputEventDTO>> constraintViolations1 = validator.validate(eventDTO1);
        Set<ConstraintViolation<InputEventDTO>> constraintViolations2 = validator.validate(eventDTO2);
        assertFalse(constraintViolations1.isEmpty());
        assertFalse(constraintViolations2.isEmpty());
    }

}
