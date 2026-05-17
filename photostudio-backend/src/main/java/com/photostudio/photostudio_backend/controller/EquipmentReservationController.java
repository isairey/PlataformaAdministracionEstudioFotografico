package com.photostudio.photostudio_backend.controller;

import com.photostudio.photostudio_backend.dto.EquipmentOutputDTO;
import com.photostudio.photostudio_backend.dto.equipmentReservation.*;
import com.photostudio.photostudio_backend.exception.ValidationException;
import com.photostudio.photostudio_backend.model.EquipmentReservation;
import com.photostudio.photostudio_backend.service.EquipmentReservationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("api/equipment/reservation")
public class EquipmentReservationController {
    private final EquipmentReservationService  equipmentReservationService;

    public EquipmentReservationController(EquipmentReservationService equipmentReservationService) {
        this.equipmentReservationService = equipmentReservationService;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER')")
    public ResponseEntity<Long> createEquipmentReservation(@RequestBody EquipmentReservationDTO dto){
        EquipmentReservation reservation =  equipmentReservationService.createEquipmentReservation(dto);
        return ResponseEntity.ok().body(reservation.getId());
    }


    @GetMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER', 'SUPER_ADMIN')")
    public ResponseEntity<List<EquipmentReservationItemOutDTO>> getEquipmentReservationItems(@PathVariable Long id){
        return ResponseEntity.ok().body(equipmentReservationService.getEquipmentReservationItems(id));
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<EquipmentReservationOutDTO>> getAllEquipmentReservations(){
        return  ResponseEntity.ok().body(equipmentReservationService.getAllReservations());
    }

    @GetMapping("/filtered")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<EquipmentReservationOutDTO>> getFilteredEquipmentReservations(
            @RequestParam(defaultValue="") String creatorFullName,
            @RequestParam(defaultValue = "") String eventName,
            @RequestParam(defaultValue="") String status,
            @RequestParam(defaultValue="") String startDate,
            @RequestParam(defaultValue="") String endDate,
            @RequestParam int pageNo,
            @RequestParam int pageSize
    ){
        return ResponseEntity.ok().body(equipmentReservationService.getPageByFilters(
                creatorFullName,
                eventName,
                status,
                startDate,
                endDate,
                pageNo,
                pageSize
        ));
    }

    @GetMapping("/user/{id}/filtered")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<Page<EquipmentReservationOutDTO>> getFilteredEquipmentReservationsForUser(
            @PathVariable long id,
            @RequestParam(defaultValue="") String creatorFullName,
            @RequestParam(defaultValue = "") String eventName,
            @RequestParam(defaultValue="") String status,
            @RequestParam(defaultValue="") String startDate,
            @RequestParam(defaultValue="") String endDate,
            @RequestParam int pageNo,
            @RequestParam int pageSize
    ){
        return ResponseEntity.ok().body(
                equipmentReservationService.getPageByFiltersForUser(id, creatorFullName, eventName, status, startDate, endDate, pageNo, pageSize)
        );
    }

    @GetMapping("/time")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<EquipmentReservationOutDTO>> getAllEquipmentReservationsWithinTimeWindow(@RequestBody TimeWindowDTO dto){
        return  ResponseEntity.ok().body(equipmentReservationService.getAllReservationsWithinTimeWindow(dto.start(), dto.end()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentReservationOutDTO> getEquipmentReservation(@PathVariable Long id){
        try{
            return ResponseEntity.ok().body(equipmentReservationService.getEquipmentReservation(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PatchMapping("/item")
    public ResponseEntity<Void> removeItemFromReservation(@RequestBody EquipmentReservationItemDTO dto){
        try{
            equipmentReservationService.removeItemFromEquipmentReservation(dto);
            return ResponseEntity.ok().build();
        } catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<String> resolveEquipmentReservation(@PathVariable Long id, @RequestBody Map<Long, Boolean> acceptanceMap) {
        try {
            equipmentReservationService.resolveReservation(id, acceptanceMap);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/modify")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<String> modifyEquipmentReservation(@PathVariable Long id, @RequestBody Map<Long, Boolean> acceptanceMap) {
        try {
            equipmentReservationService.modifyReservation(id, acceptanceMap);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelEquipmentReservation(@PathVariable Long id){
        try{
            equipmentReservationService.deleteEquipmentReservation(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR' ,'USER')")
    public ResponseEntity<List<EquipmentOutputDTO>> getAvailableItems(TimeWindowDTO timeWindow, @RequestParam Boolean statutory) {
        return ResponseEntity.ok().body(equipmentReservationService.getAvailableEquipment(timeWindow.start(), timeWindow.end(), statutory));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<EquipmentReservationOutDTO>> getUserReservations(@PathVariable Long id) {
        return ResponseEntity.ok().body(
                equipmentReservationService.getUserReservations(id)
        );
    }

    @GetMapping("/user/{id}/time")
    public ResponseEntity<List<EquipmentReservationOutDTO>> getUserReservationsWithinTime(@PathVariable Long id, @RequestBody TimeWindowDTO dto) {
        return ResponseEntity.ok().body(
                equipmentReservationService.getUserReservationsWithinTimeWindow(id, dto.end(), dto.end())
        );
    }
}
