package com.photostudio.photostudio_backend.controller;

import com.photostudio.photostudio_backend.dto.EquipmentInputDTO;
import com.photostudio.photostudio_backend.dto.EquipmentOutputDTO;
import com.photostudio.photostudio_backend.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<Page<EquipmentOutputDTO>> getAllEquipmentsByFilters(
            @RequestParam boolean active,
            @RequestParam boolean statutory,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(equipmentService.getEquipmentPageByFilters(active, statutory, name, category, pageNo, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<EquipmentOutputDTO> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @GetMapping("/by-name")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<List<EquipmentOutputDTO>> getEquipmentByName(@RequestParam String name) {
        return ResponseEntity.ok(equipmentService.getEquipmentByName(name));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> createEquipment(@Valid @RequestBody EquipmentInputDTO equipmentDTO) {
        equipmentService.createEquipment(equipmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentInputDTO equipmentDTO) {
        equipmentService.updateEquipment(id, equipmentDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}