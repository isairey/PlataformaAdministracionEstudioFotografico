package com.photostudio.photostudio_backend.dto;

import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EquipmentDTOValidationTest {

    static private Validator validator;

    @BeforeAll
    static void setUpBeforeClass() {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validEquipmentTest() {
        EquipmentInputDTO equipmentDTO = new EquipmentInputDTO(
                "Canon 5d Mark III",
                false,
                true,
                EquipmentCategory.CAMERA
        );
        Set<ConstraintViolation<EquipmentInputDTO>> constraintViolations = validator.validate(equipmentDTO);
        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    void invalidNameTest() {
        EquipmentInputDTO equipmentDTO1 = new EquipmentInputDTO(
                "",
                false,
                true,
                EquipmentCategory.CAMERA
        );
        EquipmentInputDTO equipmentDTO2 = new EquipmentInputDTO(
                "Invalid name sign #",
                false,
                true,
                EquipmentCategory.CAMERA
        );
        EquipmentInputDTO equipmentDTO3 = new EquipmentInputDTO(
                "to long to long to long to long to long to long to long longer than 30",
                false,
                true,
                EquipmentCategory.CAMERA
        );
        Set<ConstraintViolation<EquipmentInputDTO>> constraintViolations1 = validator.validate(equipmentDTO1);
        Set<ConstraintViolation<EquipmentInputDTO>> constraintViolations2 = validator.validate(equipmentDTO2);
        Set<ConstraintViolation<EquipmentInputDTO>> constraintViolations3 = validator.validate(equipmentDTO3);
        assertFalse(constraintViolations1.isEmpty());
        assertFalse(constraintViolations2.isEmpty());
        assertFalse(constraintViolations3.isEmpty());
    }
}
