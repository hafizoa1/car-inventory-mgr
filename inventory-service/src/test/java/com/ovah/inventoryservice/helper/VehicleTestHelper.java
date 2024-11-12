package com.ovah.inventoryservice.helper;

import com.ovah.inventoryservice.base.BaseIntegrationTest;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.VehicleStatus;
import com.ovah.inventoryservice.model.sync.SyncStatus;
import com.ovah.inventoryservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.UUID;

import static com.ovah.inventoryservice.model.VehicleStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;


public abstract class VehicleTestHelper extends BaseIntegrationTest {

    @Autowired
    protected VehicleRepository vehicleRepository;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
    }

    protected Vehicle givenInvalidVehicle() {
        return Vehicle.builder()
                // Missing required fields (nullable = false)
                .make(null)
                .model("")   // Empty string
                .vin(null)   // Missing unique identifier
                .year(-1)    // Invalid year
                .price(BigDecimal.valueOf(-100.00))  // Negative price
                // Missing required status
                .syncStatus(null)  // Invalid sync status
                .build();
    }

    // Or a variant with invalid formats
    protected Vehicle givenVehicleWithInvalidFormats() {
        return Vehicle.builder()
                .make("Toyota@123")  // Special characters
                .model("   ")        // Only whitespace
                .vin("123")         // Too short VIN
                .year(2025)         // Future year
                .price(BigDecimal.valueOf(0.00))  // Zero price
                .status(VehicleStatus.AVAILABLE)
                .syncStatus(SyncStatus.PROCESSING)
                .build();
    }

    public Vehicle givenValidVehicle() {
       return Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .year(2023)
                .vin("1HGCM82633A123456")
                .status(AVAILABLE)
                .price(BigDecimal.valueOf(25000.00))
                .build();
    }


    protected Vehicle givenExistingVehicle() {
        Vehicle vehicle = givenValidVehicle();
        return vehicleRepository.save(vehicle);
    }

    public ResponseEntity<Vehicle> whenPostRequestIsMade(Vehicle vehicleRequest) {
        return restTemplate.postForEntity(
                createURLWithPort("/api/v1/inventory-service/vehicles"),
                vehicleRequest,
                Vehicle.class);
    }

    public ResponseEntity<String> whenBadPostRequestIsMade(Vehicle vehicleRequest) {
        return restTemplate.postForEntity(
                createURLWithPort("/api/v1/inventory-service/vehicles"),
                vehicleRequest,
                String.class);
    }

    public ResponseEntity<Vehicle> whenPutRequestIsMade(Vehicle vehicleRequest, UUID vehicleId) {
        return restTemplate.exchange(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                HttpMethod.PUT,
                new HttpEntity<>(vehicleRequest),
                Vehicle.class);
    }

    public ResponseEntity<String> whenBadPutRequestIsMade(Vehicle vehicleRequest, UUID vehicleId) {
        return restTemplate.exchange(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                HttpMethod.PUT,
                new HttpEntity<>(vehicleRequest),
                String.class);
    }

    public ResponseEntity<Void> whenDeleteRequestIsMade(UUID vehicleId) {
        return restTemplate.exchange(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                HttpMethod.DELETE,
                null,
                Void.class);
    }

    public ResponseEntity<Vehicle> whenGetRequestIsMade(UUID vehicleId) {
        return restTemplate.getForEntity(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                Vehicle.class);
    }

    public void thenResponseIsCreated(ResponseEntity<Vehicle> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
    }

}


