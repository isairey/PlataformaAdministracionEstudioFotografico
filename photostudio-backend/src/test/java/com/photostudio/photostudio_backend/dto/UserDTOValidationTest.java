package com.photostudio.photostudio_backend.dto;


import com.photostudio.photostudio_backend.dto.user.UserInputDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldPassValidationTest() {
        UserInputDTO validUserDTO = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        Set<ConstraintViolation<UserInputDTO>> violations = validator.validate(validUserDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailNameValidationTest() {
        UserInputDTO firstNotValidUser = new UserInputDTO(
                1L,
                "John1",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        UserInputDTO secondNotValidUser = new UserInputDTO(
                1L,
                "J",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        Set<ConstraintViolation<UserInputDTO>> violations1 = validator.validate(firstNotValidUser);
        Set<ConstraintViolation<UserInputDTO>> violations2 = validator.validate(secondNotValidUser);
        assertFalse(violations1.isEmpty());
        assertFalse(violations2.isEmpty());
    }

    @Test
    void shouldFailSurnameValidationTest() {
        UserInputDTO firstNotValidUser = new UserInputDTO(
                1L,
                "John",
                "",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        UserInputDTO secondNotValidUser = new UserInputDTO(
                1L,
                "J",
                "Snow123",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        Set<ConstraintViolation<UserInputDTO>> violations1 = validator.validate(firstNotValidUser);
        Set<ConstraintViolation<UserInputDTO>> violations2 = validator.validate(secondNotValidUser);
        assertFalse(violations1.isEmpty());
        assertFalse(violations2.isEmpty());
    }

    @Test
    void shouldFailEmailValidationTest() {
        UserInputDTO firstNotValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john?snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        UserInputDTO secondNotValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snowksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        UserInputDTO thirdNotValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@gmail.com",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        Set<ConstraintViolation<UserInputDTO>> violations1 = validator.validate(firstNotValidUser);
        Set<ConstraintViolation<UserInputDTO>> violations2 = validator.validate(secondNotValidUser);
        Set<ConstraintViolation<UserInputDTO>> violations3 = validator.validate(thirdNotValidUser);
        assertFalse(violations1.isEmpty());
        assertFalse(violations2.isEmpty());
        assertFalse(violations3.isEmpty());
    }

    @Test
    void shouldPassEmailValidationTest() {
        UserInputDTO validUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        Set<ConstraintViolation<UserInputDTO>> violations1 = validator.validate(validUser);
        assertTrue(violations1.isEmpty());
    }

    @Test
    void shouldFailPhoneValidationTest() {
        UserInputDTO firstNotValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "1 23456789"
        );
        UserInputDTO secondNotValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "1123456789"
        );
        UserInputDTO thirdNotValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "+49123456789"
        );
        Set<ConstraintViolation<UserInputDTO>> violations1 = validator.validate(firstNotValidUser);
        Set<ConstraintViolation<UserInputDTO>> violations2 = validator.validate(secondNotValidUser);
        Set<ConstraintViolation<UserInputDTO>> violations3 = validator.validate(thirdNotValidUser);
        assertFalse(violations1.isEmpty());
        assertFalse(violations2.isEmpty());
        assertFalse(violations3.isEmpty());
    }

    @Test
    void shouldPassPhoneValidationTest() {
        UserInputDTO firstValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "+48123456789"
        );
        UserInputDTO secondValidUser = new UserInputDTO(
                1L,
                "John",
                "Snow",
                "john.snow@ksaf.pl",
                "passWORD1!",
                "passWORD1!",
                "johnsnow123",
                "123456789"
        );
        Set<ConstraintViolation<UserInputDTO>> violations1 = validator.validate(firstValidUser);
        Set<ConstraintViolation<UserInputDTO>> violations2 = validator.validate(secondValidUser);
        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
    }




}
