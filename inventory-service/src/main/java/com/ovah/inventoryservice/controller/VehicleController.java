package com.ovah.inventoryservice.controller;

import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory-service")
@Tag(name = "Vehicle Management", description = "Operations pertaining to vehicles in the Inventory Service")
public class VehicleController {

    /*
TODO: Future API Improvements
1. Additional HTTP Status codes:
   - 400 Bad Request (invalid data formats, validation failures)
   - 500 Internal Server Error (unexpected server issues)
   - 409 Conflict (for potential duplicate entries)

2. Input Validation:
   - @Valid annotations for request bodies
   - Custom validation constraints
   - Better error responses

3. Documentation:
   - More detailed API responses with example payloads
   - Error response schemas
   - Better descriptions of expected inputs
*/

    private final VehicleService vehicleService;

    @GetMapping("/vehicles/{id}")
    @Operation(summary = "Get a vehicle by ID", description = "Provide an ID to retrieve detailed information about a specific vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the vehicle"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found with the provided ID")
    })
    public ResponseEntity<Vehicle> getVehicle(
            @Parameter(description = "ID of the vehicle to retrieve", required = true)
            @PathVariable UUID id) {
        return vehicleService.getVehicleById(id);
    }

    @GetMapping("/vehicles")
    @Operation(summary = "Get all vehicles in the database", description = "Retrieve a list of all vehicles stored in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all vehicles"),
            @ApiResponse(responseCode = "204", description = "No vehicles found")
    })
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @PostMapping("/vehicles")
    @Operation(summary = "Create a new vehicle", description = "Submit vehicle data to create a new vehicle entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation errors for Vehicle")
    })
    public ResponseEntity<Vehicle> createVehicle(
            @Parameter(description = "Vehicle data to be created", required = true)
            @RequestBody Vehicle vehicle) {
        return vehicleService.createVehicle(vehicle);
    }

    @PutMapping("/vehicles/{id}")
    @Operation(summary = "Update an existing vehicle", description = "Provide an ID and vehicle data to update an existing vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found with the provided ID"),
            @ApiResponse(responseCode = "400", description = "Validation errors for Vehicle")
    })
    public ResponseEntity<Vehicle> updateVehicle(
            @Parameter(description = "ID of the vehicle to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated vehicle data", required = true)
            @RequestBody Vehicle updatedVehicle) {
        return vehicleService.updateVehicle(id, updatedVehicle);
    }

    @DeleteMapping("/vehicles/{id}")
    @Operation(summary = "Delete a vehicle", description = "Provide an ID to delete a vehicle from the inventory service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found with the provided ID")
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "ID of the vehicle to delete", required = true)
            @PathVariable UUID id) {
        return vehicleService.deleteVehicle(id);
    }

    @PostMapping("vehicles/{id}/image")
    public ResponseEntity<Void> uploadImage(@PathVariable UUID id, @RequestParam MultipartFile file) {
        return vehicleService.uploadImage(id, file);
    }

    @DeleteMapping("vehicles/{id}/image")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
        return vehicleService.deleteImage(id);
    }


}