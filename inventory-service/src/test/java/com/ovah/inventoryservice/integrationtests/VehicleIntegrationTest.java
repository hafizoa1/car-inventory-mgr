package com.ovah.inventoryservice.integrationtests;

import com.ovah.inventoryservice.InventoryServiceApplication;
import com.ovah.inventoryservice.base.BaseIntegrationTest;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.VehicleStatus;
import com.ovah.inventoryservice.repository.VehicleRepository;
import com.ovah.inventoryservice.service.autotrader.MockAutoTraderService;
import com.ovah.inventoryservice.model.sync.SyncStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.awaitility.Awaitility.await;
import static com.ovah.inventoryservice.model.VehicleStatus.AVAILABLE;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = InventoryServiceApplication.class
)
@ActiveProfiles("test")
public class VehicleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @MockBean
    private MockAutoTraderService autoTraderService;

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
        /*Vehicle savedVehicle = vehicleRepository.findById(1) //change this from 1 to something else
                .orElseThrow(() -> new AssertionError("Vehicle not found in database"));

        assertThat(savedVehicle)
                .satisfies(vehicle -> {
                    assertThat(vehicle.getMake()).isEqualTo("Toyota");
                    assertThat(vehicle.getModel()).isEqualTo("Camry");
                    assertThat(vehicle.getYear()).isEqualTo(2023);
                    assertThat(vehicle.getVin()).isEqualTo("1HGCM82633A123456");
                    assertThat(vehicle.getStatus()).isEqualTo(AVAILABLE);
                    assertThat(vehicle.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(25000.00));
                });*/
    }

    @Test
    void whenVehicleIsCreated_thenItIsSavedLocallyAndSyncedWithAutoTrader() {
        // Given
        Vehicle vehicleRequest = Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .year(2023)
                .vin("1HGCM82633A123456")
                .price(BigDecimal.valueOf(25000.00))
                .build();

        // Mock AutoTrader response
        when(autoTraderService.createListing(any(Vehicle.class)))
                .thenReturn("AT-123456");

        // When
        ResponseEntity<Vehicle> response = restTemplate.postForEntity(
                createURLWithPort("/api/v1/inventory-service/vehicles"),
                vehicleRequest,
                Vehicle.class
        );

        // Then
        // 1. Verify immediate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getSyncStatus()).isEqualTo(SyncStatus.PENDING);

        UUID vehicleId = response.getBody().getId();

        // 2. Wait for async sync to complete and verify final state
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    Vehicle savedVehicle = vehicleRepository.findById(vehicleId).orElseThrow();
                    return SyncStatus.SYNCED.equals(savedVehicle.getSyncStatus());
                });

        Vehicle syncedVehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        assertThat(syncedVehicle)
                .satisfies(vehicle -> {
                    // Basic vehicle properties
                    assertThat(vehicle.getMake()).isEqualTo("Toyota");
                    assertThat(vehicle.getModel()).isEqualTo("Camry");
                    assertThat(vehicle.getYear()).isEqualTo(2023);
                    assertThat(vehicle.getVin()).isEqualTo("1HGCM82633A123456");
                    assertThat(vehicle.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(25000.00));

                    // Sync properties
                    assertThat(vehicle.getSyncStatus()).isEqualTo(SyncStatus.SYNCED);
                    assertThat(vehicle.getAutoTraderListingId()).isEqualTo("AT-123456");
                    assertThat(vehicle.getLastSyncAttempt()).isNotNull();
                });
    }
    @Test
    void whenVehicleStatusUpdated_thenSyncedWithAutoTrader() {
        // Given: Create and sync initial vehicle
        Vehicle initialVehicle = Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .year(2023)
                .vin("3HGCM82633A789013")  // Unique VIN
                .price(BigDecimal.valueOf(25000.00))
                .status(AVAILABLE)
                .build();

        // Mock initial AutoTrader listing creation
        when(autoTraderService.createListing(any(Vehicle.class)))
                .thenReturn("AT-789013"); //create an autotrader id

        // Save initial vehicle
        ResponseEntity<Vehicle> createResponse = restTemplate.postForEntity(
                createURLWithPort("/api/v1/inventory-service/vehicles"),
                initialVehicle,
                Vehicle.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID vehicleId = createResponse.getBody().getId();

        // Wait for initial sync to complete
        await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    Vehicle saved = vehicleRepository.findById((vehicleId)).orElseThrow();
                    return SyncStatus.SYNCED.equals(saved.getSyncStatus());
                });

        // When: Update the vehicle status to SOLD
        Vehicle updateRequest = vehicleRepository.findById((vehicleId)).orElseThrow();
        updateRequest.setStatus(VehicleStatus.SOLD);

        // Mock AutoTrader status update
        when(autoTraderService.updateListing(eq("AT-789013"), any(Vehicle.class)))
                .thenReturn(true);

        ResponseEntity<Vehicle> updateResponse = restTemplate.exchange(
                createURLWithPort("/api/v1/vehicles/" + vehicleId),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Vehicle.class
        );

        // Then: Verify the update was successful
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getStatus()).isEqualTo(VehicleStatus.SOLD);

        // Wait for sync to complete and verify final state
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    Vehicle updated = vehicleRepository.findById(vehicleId).orElseThrow();
                    return SyncStatus.SYNCED.equals(updated.getSyncStatus()) &&
                            VehicleStatus.SOLD.equals(updated.getStatus());
                });

        // Verify final state
        Vehicle finalVehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        assertThat(finalVehicle)
                .satisfies(vehicle -> {
                    // Status should be updated
                    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.SOLD);

                    // Sync status should be maintained
                    assertThat(vehicle.getSyncStatus()).isEqualTo(SyncStatus.SYNCED);
                    assertThat(vehicle.getAutoTraderListingId()).isEqualTo("AT-789013");
                    assertThat(vehicle.getLastSyncAttempt()).isNotNull();

                    // Other properties should remain unchanged
                    assertThat(vehicle.getMake()).isEqualTo("Toyota");
                    assertThat(vehicle.getModel()).isEqualTo("Camry");
                    assertThat(vehicle.getYear()).isEqualTo(2023);
                    assertThat(vehicle.getVin()).isEqualTo("3HGCM82633A789013");
                    assertThat(vehicle.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(25000.00));
                });
    }
}

