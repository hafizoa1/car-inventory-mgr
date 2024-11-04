package com.ovah.inventoryservice.service;

import com.ovah.inventoryservice.exception.VehicleNotFoundException;
import com.ovah.inventoryservice.messaging.VehicleMessageProducer;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.sync.SyncStatus;
import com.ovah.inventoryservice.repository.VehicleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    private final VehicleMessageProducer vehicleMessageProducer;

    public ResponseEntity<Vehicle> getVehicleById(UUID id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @Transactional
    public ResponseEntity<Vehicle> createVehicle(Vehicle vehicle) {
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Created vehicle with id: {}", savedVehicle.getId());
        vehicleMessageProducer.sendVehicleCreatedMessage(savedVehicle);
        return ResponseEntity.status(201).body(savedVehicle);
    }

    @Transactional
    public ResponseEntity<Vehicle> updateVehicle(UUID id, Vehicle updateRequest) {
        return vehicleRepository.findById(id)
                .map(existingVehicle -> {
                    // Update mutable fields only
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

    public ResponseEntity<Void> deleteVehicle(UUID vehicleId) { //put this in a try catch
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new VehicleNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        try {
            vehicleRepository.deleteById(vehicleId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Handle potential exceptions, such as database errors
            throw new RuntimeException("Failed to delete the vehicle due to: " + e.getMessage(), e);
        }
    }


}

