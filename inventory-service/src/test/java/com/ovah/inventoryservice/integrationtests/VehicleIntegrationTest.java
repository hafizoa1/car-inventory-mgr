package com.ovah.inventoryservice.integrationtests;

import com.ovah.inventoryservice.InventoryServiceApplication;
import com.ovah.inventoryservice.base.BaseIntegrationTest;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static com.ovah.inventoryservice.model.VehicleStatus.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = InventoryServiceApplication.class  // Add your main application class here
)
@ActiveProfiles("test")
public class VehicleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void whenPostNewVehicle_thenVehicleIsCreatedSuccessfully() {
        // Given: A vehicle request
        Vehicle vehicleRequest = Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .year(2023)
                .vin("1HGCM82633A123456")
                .status(AVAILABLE)
                .price(BigDecimal.valueOf(25000.00))
                .build();

        // When: We make a POST request to create the vehicle
        ResponseEntity<Vehicle> response = restTemplate.postForEntity(
                createURLWithPort("/api/v1/inventory-service/vehicles"), // Fixed endpoint
                vehicleRequest,
                Vehicle.class
        );

        // Then: The response is successful and the vehicle is in the database
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And: The stored vehicle matches our request
        Vehicle savedVehicle = vehicleRepository.findById(1) //change this from 1 to something else
                .orElseThrow(() -> new AssertionError("Vehicle not found in database"));

        assertThat(savedVehicle)
                .satisfies(vehicle -> {
                    assertThat(vehicle.getMake()).isEqualTo("Toyota");
                    assertThat(vehicle.getModel()).isEqualTo("Camry");
                    assertThat(vehicle.getYear()).isEqualTo(2023);
                    assertThat(vehicle.getVin()).isEqualTo("1HGCM82633A123456");
                    assertThat(vehicle.getStatus()).isEqualTo(AVAILABLE);
                    assertThat(vehicle.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(25000.00));
                });
    }
}

