package com.photostudio.photostudio_backend.service;

import com.photostudio.photostudio_backend.dto.EquipmentInputDTO;
import com.photostudio.photostudio_backend.dto.EquipmentOutputDTO;
import com.photostudio.photostudio_backend.exception.EntityException;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.Equipment;
import com.photostudio.photostudio_backend.model.enums.EquipmentCategory;
import com.photostudio.photostudio_backend.repository.EquipmentRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    EquipmentRepository equipmentRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentService.class);

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void createEquipment(@Valid EquipmentInputDTO equipmentDTO) {
        LOGGER.info("Attempting to create new equipment with name: {}", equipmentDTO.name());

        Equipment equipment = new Equipment(
                equipmentDTO.name(),
                equipmentDTO.activeMembers(),
                equipmentDTO.statutoryEvent(),
                equipmentDTO.equipmentCategory()
        );
        equipmentRepository.save(equipment);
        LOGGER.info("Successfully created equipment '{}' with ID {}", equipment.getName(), equipment.getId());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteEquipment(Long equipmentId) {
        LOGGER.warn("Attempting to mark equipment with ID {} as deleted", equipmentId);
        Equipment equipment = getEquipmentIdOrThrow(equipmentId);
        equipment.setDeleted(true);
        equipmentRepository.save(equipment);
        LOGGER.info("Successfully marked equipment ID {} as deleted", equipmentId);
        // ALL FUTURE RESERVATIONS MUST BE CANCELLED FOR THIS EQUIPMENT
        LOGGER.warn("Business logic pending: Future reservations for equipment ID {} must be cancelled.", equipmentId);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void updateEquipment(Long id,@Valid EquipmentInputDTO equipmentDTO) {
        LOGGER.info("Attempting to update equipment with ID {}", id);
        Equipment equipment = getEquipmentIdOrThrow(id);

        if(!equipmentDTO.equipmentCategory().equals(equipment.getEquipmentCategory())) {
            LOGGER.info("Changing equipment category for ID {} from {} to {}",
                    id, equipment.getEquipmentCategory(), equipmentDTO.equipmentCategory());
            equipment.setEquipmentCategory(equipmentDTO.equipmentCategory());
        }
        equipment.setName(equipmentDTO.name());
        equipment.setActiveMembers(equipmentDTO.activeMembers());
        equipment.setStatutoryEvent(equipmentDTO.statutoryEvent());
        equipmentRepository.save(equipment);
        LOGGER.info("Successfully updated equipment ID {}", id);
    }

    @Transactional(readOnly = true)
    public List<EquipmentOutputDTO> getAllEquipmentsByActiveMemberAndStatutory(boolean active, boolean statutory) {
        return equipmentRepository.findByActiveMembersEqualsAndStatutoryEventEqualsAndDeletedIsFalse(active, statutory).stream()
                .map(this::convertToEquipmentDTO)
                .sorted(Comparator.comparing(EquipmentOutputDTO::equipmentCategory))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipmentOutputDTO getEquipmentById(Long id) {
        Equipment equipment = getEquipmentIdOrThrow(id);
        return convertToEquipmentDTO(equipment);
    }

    @Transactional(readOnly = true)
    public List<EquipmentOutputDTO> getEquipmentByName(String name) {
        return equipmentRepository.findByNameAndDeletedIsFalse(name).stream()
                .map(this::convertToEquipmentDTO)
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    protected Equipment getEquipmentIdOrThrow(Long equipmentId) {
        return equipmentRepository.findByIdAndDeletedIsFalse(equipmentId)
                .orElseThrow(() -> {
                    LOGGER.warn("Equipment not found by ID: {}. Throwing EntityException.", equipmentId);
                    return new EntityException("Equipment does not exist by id: " + equipmentId);
                });
    }

    protected EquipmentOutputDTO convertToEquipmentDTO(Equipment equipment) {
        return new EquipmentOutputDTO(
                equipment.getId(),
                equipment.getName(),
                equipment.isActiveMembers(),
                equipment.isStatutoryEvent(),
                equipment.getEquipmentCategory()
        );
    }

    @Transactional
    protected Equipment getEquipmentIdOrThrowWithLock(Long id) {
        return equipmentRepository.findByIdWithPessimisticWriteLock(id)
                .orElseThrow(() -> new EntityException("Equipment not found"));
    }

    @Transactional(readOnly = true)
    public Page<EquipmentOutputDTO> getEquipmentPageByFilters(boolean active, boolean statutory, String name, String category, int pageNo, int size) {
        EquipmentCategory equipmentCategory = null;
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
            try {
                equipmentCategory = EquipmentCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid equipment category: " + category);
            }
        }

        PageRequest pageable = PageRequest.of(pageNo, size, Sort.by("name").ascending());
        String nameFilter = (name == null) ? "" : name.trim();

        return equipmentRepository
                .findByFilters(active, statutory, nameFilter, equipmentCategory, pageable)
                .map(this::convertToEquipmentDTO);
    }
}