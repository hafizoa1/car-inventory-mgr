package com.ovah.inventoryservice.service;

import com.ovah.inventoryservice.exception.VehicleNotFoundException;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    public void createVehicle(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }
}

