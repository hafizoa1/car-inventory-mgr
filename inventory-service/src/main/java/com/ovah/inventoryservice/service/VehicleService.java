package com.ovah.inventoryservice.service;

import com.ovah.inventoryservice.exception.VehicleNotFoundException;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Vehicle getVehicleById(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(String.valueOf(id)));
    }

    public void createVehicle(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }
}

