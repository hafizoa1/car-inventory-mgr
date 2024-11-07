package com.ovah.inventoryservice.helper;

import com.ovah.inventoryservice.base.BaseIntegrationTest;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.repository.VehicleRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static com.ovah.inventoryservice.model.VehicleStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

public class VehicleTestHelper extends BaseIntegrationTest {

    VehicleRepository vehicleRepository; 

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

    public ResponseEntity<Void> whenPutRequestIsMade(Vehicle vehicleRequest, Long vehicleId) {
        return restTemplate.exchange(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                HttpMethod.PUT,
                new HttpEntity<>(vehicleRequest),
                Void.class);
    }

    public ResponseEntity<Void> whenDeleteRequestIsMade(Long vehicleId) {
        return restTemplate.exchange(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                HttpMethod.DELETE,
                null,
                Void.class);
    }

    public ResponseEntity<Vehicle> whenGetRequestIsMade(Long vehicleId) {
        return restTemplate.getForEntity(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                Vehicle.class);
    }

    public void thenResponseIsOk(ResponseEntity<Vehicle> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
    }

}
