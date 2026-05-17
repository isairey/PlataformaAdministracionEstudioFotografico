package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.EquipmentInputDTO;
import com.photostudio.photostudio_backend.dto.EquipmentOutputDTO;
import com.photostudio.photostudio_backend.exception.EntityException;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.Equipment;
import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EquipmentServiceTest {

    @Mock
    EquipmentRepository equipmentRepository;

    @InjectMocks
    EquipmentService equipmentService;

    @Captor
    ArgumentCaptor<Equipment> equipmentCaptor;

    private EquipmentInputDTO equipmentInputDTO;
    private Equipment equipment;
    private EquipmentInputDTO equipmentInputDTO2;
    private EquipmentInputDTO equipmentInputDTO3;

    @BeforeEach
    public void setUp() {
        equipmentInputDTO = new EquipmentInputDTO(
                "Canon 5D Mark III",
                false,
                false,
                EquipmentCategory.CAMERA
        );
        equipment = new Equipment(
                "Canon 5D Mark III",
                false,
                false,
                EquipmentCategory.CAMERA
        );
        equipmentInputDTO2 = new EquipmentInputDTO(
                "Sony",
                true,
                true,
                EquipmentCategory.CAMERA
        );
        equipmentInputDTO3 = new EquipmentInputDTO(
                "Tamron 70-200mm",
                false,
                false,
                EquipmentCategory.LENS
        );
    }
    @Nested
    public class createNewEquipmentTest {

        @Test
        void createNewEquipment() {

            equipmentService.createEquipment(equipmentInputDTO);
            verify(equipmentRepository).save(equipmentCaptor.capture());
            Equipment equipment = equipmentCaptor.getValue();

            assertEquals("Canon 5D Mark III", equipment.getName());
            assertFalse(equipment.isActiveMembers());
            assertFalse(equipment.isStatutoryEvent());
            assertEquals(EquipmentCategory.CAMERA, equipment.getEquipmentCategory());
        }
    }
    @Nested
    public class deleteEquipmentTest {

        @Test
        void deleteCorrectlyEquipment() {
            when(equipmentRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(equipment));

            equipmentService.deleteEquipment(1L);

            verify(equipmentRepository).save(equipmentCaptor.capture());
            Equipment equipment = equipmentCaptor.getValue();

            assertEquals("Canon 5D Mark III", equipment.getName());
            assertFalse(equipment.isActiveMembers());
            assertFalse(equipment.isStatutoryEvent());
            assertEquals(EquipmentCategory.CAMERA, equipment.getEquipmentCategory());
            assertTrue(equipment.isDeleted());
        }
        @Test
        void deleteNotExistingEquipmentTest() {
            when(equipmentRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.empty());

            EntityException exception = assertThrows(EntityException.class, () -> equipmentService.deleteEquipment(1L));

            assertEquals("Equipment does not exist by id: " + 1L, exception.getMessage());
            verify(equipmentRepository, never()).save(any());
        }
    }
    @Nested
    public class updateEquipmentTest {

        @Test
        void updateEquipmentCorrectlyTest() {
            when(equipmentRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(equipment));

            equipmentService.updateEquipment(1L, equipmentInputDTO2);

            verify(equipmentRepository).save(equipmentCaptor.capture());
            Equipment equipment = equipmentCaptor.getValue();
            assertEquals("Sony", equipment.getName());
            assertTrue(equipment.isActiveMembers());
            assertTrue(equipment.isStatutoryEvent());
            assertEquals(EquipmentCategory.CAMERA, equipment.getEquipmentCategory());
        }
        @Test
        void updateNotExistingEquipmentTest() {
            when(equipmentRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.empty());

            EntityException exception = assertThrows(EntityException.class, () -> equipmentService.updateEquipment(1L, equipmentInputDTO2));
            assertEquals("Equipment does not exist by id: " + 1L, exception.getMessage());
            verify(equipmentRepository, never()).save(any());
        }
        @Test
        void invalidCategoryTest() {
            when(equipmentRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(equipment));

            ValidationException exception = assertThrows(ValidationException.class, () -> equipmentService.updateEquipment(1L, equipmentInputDTO3));
            assertEquals("Invalid equipment category", exception.getMessage());
            verify(equipmentRepository, never()).save(any());
        }
    }
    @Nested
    public class GetEquipmentInfoTest {

        private Equipment equipment2;
        private Equipment equipment3;

        @BeforeEach
        void setUp() {
            equipment2 = new Equipment(
                    "Sony A7 III",
                    true,
                    true,
                    EquipmentCategory.CAMERA
            );
            equipment3 = new Equipment(
                    "Tamron 70-200mm",
                    false,
                    false,
                    EquipmentCategory.LENS
            );
         }

        @Test
        void getEquipmentByIdCorrectlyTest() {
            when(equipmentRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(equipment));

            // When
            EquipmentOutputDTO result = equipmentService.getEquipmentById(1L);

            // Then
            assertNotNull(result);
            assertEquals(equipment.getId(), result.id());
            assertEquals("Canon 5D Mark III", result.name());
            assertFalse(result.activeMembers());
            assertFalse(result.statutoryEvent());
            assertEquals(EquipmentCategory.CAMERA, result.equipmentCategory());
        }

        @Test
        void getEquipmentByIdNotFoundTest() {
            // Given
            when(equipmentRepository.findByIdAndDeletedIsFalse(99L)).thenReturn(Optional.empty());

            // When / Then
            EntityException exception = assertThrows(EntityException.class,
                    () -> equipmentService.getEquipmentById(99L));

            assertEquals("Equipment does not exist by id: 99", exception.getMessage());
        }

        @Test
        void getEquipmentByNameTest() {
            List<Equipment> mockList = List.of(equipment, equipment2);
            when(equipmentRepository.findByNameAndDeletedIsFalse("Canon 5D Mark III"))
                    .thenReturn(mockList);

            // When
            List<EquipmentOutputDTO> results = equipmentService.getEquipmentByName("Canon 5D Mark III");

            // Then
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals("Canon 5D Mark III", results.get(0).name());
            assertEquals("Sony A7 III", results.get(1).name());
        }

        @Test
        void getEquipmentByNameNotFoundTest() {
            // Given
            when(equipmentRepository.findByNameAndDeletedIsFalse("Nikon")).thenReturn(List.of());

            // When
            List<EquipmentOutputDTO> results = equipmentService.getEquipmentByName("Nikon");

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        void getAllEquipmentsByActiveMemberAndStatutoryTest() {
            // Given
            List<Equipment> mockList = List.of(equipment, equipment3);
            when(equipmentRepository.findByActiveMembersEqualsAndStatutoryEventEqualsAndDeletedIsFalse(false, false))
                    .thenReturn(mockList);

            // When
            List<EquipmentOutputDTO> results = equipmentService.getAllEquipmentsByActiveMemberAndStatutory(false, false);

            // Then
            assertNotNull(results);
            assertEquals(2, results.size());

            assertEquals(EquipmentCategory.CAMERA, results.get(0).equipmentCategory());
            assertEquals("Canon 5D Mark III", results.get(0).name());

            assertEquals(EquipmentCategory.LENS, results.get(1).equipmentCategory());
            assertEquals("Tamron 70-200mm", results.get(1).name());
        }

        @Test
        void getAllEquipmentsByActiveMemberAndStatutoryEmptyTest() {
            // Given
            when(equipmentRepository.findByActiveMembersEqualsAndStatutoryEventEqualsAndDeletedIsFalse(true, true))
                    .thenReturn(List.of());

            // When
            List<EquipmentOutputDTO> results = equipmentService.getAllEquipmentsByActiveMemberAndStatutory(true, true);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }
}
