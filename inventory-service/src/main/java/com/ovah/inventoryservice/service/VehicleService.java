package com.ovah.inventoryservice.service;

import com.ovah.inventoryservice.exception.VehicleNotFoundException;
import com.ovah.inventoryservice.messaging.VehicleMessageProducer;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.sync.SyncStatus;
import com.ovah.inventoryservice.repository.VehicleRepository;
import com.ovah.inventoryservice.validator.validators.VehicleCreateValidator;
import com.ovah.inventoryservice.validator.validators.VehicleUpdateValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    private final VehicleMessageProducer vehicleMessageProducer;

    private final VehicleCreateValidator vehicleCreateValidator;

    private final VehicleUpdateValidator vehicleUpdateValidator;

    public ResponseEntity<Vehicle> getVehicleById(UUID id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        if (vehicles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(vehicles);
    }

    @Transactional
    public ResponseEntity<Vehicle> createVehicle(Vehicle vehicle) {
        vehicleCreateValidator.validate(vehicle);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Created vehicle with id: {}", savedVehicle.getId());
        vehicleMessageProducer.sendVehicleCreatedMessage(savedVehicle);
        return ResponseEntity.status(201).body(savedVehicle);
    }

    @Transactional
    public ResponseEntity<Vehicle>  updateVehicle(UUID id, Vehicle updateRequest) {
        vehicleUpdateValidator.validate(updateRequest);
        return vehicleRepository.findById(id)
                .map(existingVehicle -> {
                    // Update mutable fields only
                    existingVehicle.setVin(updateRequest.getVin());
                    existingVehicle.setMake(updateRequest.getMake());
                    existingVehicle.setModel(updateRequest.getModel());
                    existingVehicle.setYear(updateRequest.getYear());
                    existingVehicle.setPrice(updateRequest.getPrice());
                    existingVehicle.setStatus(updateRequest.getStatus());

                    existingVehicle.setSyncStatus(SyncStatus.PENDING);

                    // Save updates
                    Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);

                    // Trigger sync
                    vehicleMessageProducer.sendVehicleUpdatedMessage(updatedVehicle);

                    return ResponseEntity.ok(updatedVehicle);
                })
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id: " + id));
    }

    public ResponseEntity<Void> deleteVehicle(UUID vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new VehicleNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        try {
            vehicleRepository.deleteById(vehicleId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete the vehicle due to: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Void> uploadImage(UUID id, MultipartFile file) {
       try {
           Vehicle vehicle = vehicleRepository.findById(id)
                   .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

           vehicle.setImage(file.getBytes());
           vehicleRepository.save(vehicle);
           return ResponseEntity.noContent().build();
       } catch (IOException e) {
           throw new RuntimeException("Failed to upload image due to: " + e.getMessage(), e);
       }
    }

    public ResponseEntity<Void> deleteImage(UUID id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        vehicle.setImage(null);
        vehicleRepository.save(vehicle);
        return ResponseEntity.noContent().build();

    }
}

