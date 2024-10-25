package com.ovah.inventoryservice.controller;


import com.ovah.inventoryservice.exception.VehicleNotFoundException;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.service.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/inventory-service")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable Long id) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(id);
            return ResponseEntity.ok(vehicle);
        } catch (VehicleNotFoundException e) {


            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/vehicles")
    public void createVehicle(@RequestBody Vehicle vehicle) {
        vehicleService.createVehicle(vehicle);
    }
}
