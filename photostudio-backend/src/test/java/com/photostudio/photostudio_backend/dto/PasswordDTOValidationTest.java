package com.photostudio.photostudio_backend.dto;

import com.photostudio.photostudio_backend.dto.password.PasswordChangeDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordDTOValidationTest {

    static private Validator validator;

    @BeforeAll
    static void setUpBeforeClass() {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validPasswordTest() {
        PasswordChangeDTO change = new PasswordChangeDTO(
                "OldPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );
        Set<ConstraintViolation<PasswordChangeDTO>> constraintViolations = validator.validate(change);
        assertTrue(constraintViolations.isEmpty());
    }
    @Test
    void invalidPasswordTest() {
        PasswordChangeDTO change1 = new PasswordChangeDTO(
                "LackofSpecial123",
                "NewPassword123!",
                "NewPassword123!"
        );
        PasswordChangeDTO change2 = new PasswordChangeDTO(
                "LackofNumber!",
                "NewPassword123!",
                "NewPassword123!"
        );
        PasswordChangeDTO change3 = new PasswordChangeDTO(
                "lackofbigletter123!",
                "NewPassword123!",
                "NewPassword123!"
        );
        PasswordChangeDTO change4 = new PasswordChangeDTO(
                "LACKOFSMALLLETTER123!",
                "NewPassword123!",
                "NewPassword123!"
        );
        PasswordChangeDTO change5 = new PasswordChangeDTO(
                "SHOrt1!",
                "NewPassword123!",
                "NewPassword123!"
        );
        Set<ConstraintViolation<PasswordChangeDTO>> constraintViolations1 = validator.validate(change1);
        Set<ConstraintViolation<PasswordChangeDTO>> constraintViolations2 = validator.validate(change2);
        Set<ConstraintViolation<PasswordChangeDTO>> constraintViolations3 = validator.validate(change3);
        Set<ConstraintViolation<PasswordChangeDTO>> constraintViolations4 = validator.validate(change4);
        Set<ConstraintViolation<PasswordChangeDTO>> constraintViolations5 = validator.validate(change5);
        assertFalse(constraintViolations1.isEmpty());
        assertFalse(constraintViolations2.isEmpty());
        assertFalse(constraintViolations3.isEmpty());
        assertFalse(constraintViolations4.isEmpty());
        assertFalse(constraintViolations5.isEmpty());
    }
}
