package com.ovah.inventoryservice.integrationtests;

import com.ovah.inventoryservice.InventoryServiceApplication;
import com.ovah.inventoryservice.base.BaseIntegrationTest;
import com.ovah.inventoryservice.helper.VehicleTestHelper;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.VehicleStatus;
import com.ovah.inventoryservice.repository.VehicleRepository;
import com.ovah.inventoryservice.service.autotrader.MockAutoTraderService;
import com.ovah.inventoryservice.model.sync.SyncStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.awaitility.Awaitility.await;
import static com.ovah.inventoryservice.model.VehicleStatus.AVAILABLE;

public class VehicleIntegrationTest extends VehicleTestHelper {

    @Autowired
    private VehicleRepository vehicleRepository;

    @MockBean
    private MockAutoTraderService autoTraderService;

    @Test
    void successfullyCreateVehicle() {
        // Given: A vehicle request
        Vehicle vehicleRequest = givenValidVehicle();

        // When: We make a POST request to create the vehicle
        ResponseEntity<Vehicle> response = whenPostRequestIsMade(vehicleRequest);

        // Then: The response is successful and the vehicle is in the database
        thenResponseIsOk(response);

        //thenVehicleExistsInDatabase

        // And: The stored vehicle matches our request
        Vehicle savedVehicle = vehicleRepository.findById(response.getBody().getId())
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

    @Test
    void whenVehicleIsCreated_thenItIsSavedLocallyAndSyncedWithAutoTrader() {
        // Given
        Vehicle vehicleRequest = givenValidVehicle();

        // Mock AutoTrader response
        when(autoTraderService.createListing(any(Vehicle.class)))
                .thenReturn("AT-123456");

        // When
        ResponseEntity<Vehicle> response = whenPostRequestIsMade(vehicleRequest);

        UUID vehicleId = response.getBody().getId();

        // Then
        thenResponseIsOk(response);

        // Wait for async sync to complete and verify final state
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
        Vehicle initialVehicle = givenValidVehicle();

        when(autoTraderService.createListing(any(Vehicle.class)))
                .thenReturn("AT-789013");

        ResponseEntity<Vehicle> createResponse = whenPostRequestIsMade(initialVehicle);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        UUID vehicleId = createResponse.getBody().getId();

        // Wait for initial sync
        await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    Vehicle saved = vehicleRepository.findById(vehicleId).orElseThrow();
                    return SyncStatus.SYNCED.equals(saved.getSyncStatus());
                });

        // When: Update vehicle status
        Vehicle updateRequest = vehicleRepository.findById(vehicleId).orElseThrow();
        updateRequest.setStatus(VehicleStatus.SOLD);

        when(autoTraderService.updateListing(eq("AT-789013"), any(Vehicle.class)))
                .thenReturn("AT-789013");

        ResponseEntity<Vehicle> updateResponse = restTemplate.exchange(
                createURLWithPort("/api/v1/inventory-service/vehicles/" + vehicleId),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Vehicle.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getStatus()).isEqualTo(VehicleStatus.SOLD);

        // Wait for sync and verify
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    Vehicle updated = vehicleRepository.findById(vehicleId).orElseThrow();
                    return SyncStatus.SYNCED.equals(updated.getSyncStatus()) &&
                            VehicleStatus.SOLD.equals(updated.getStatus());
                });

        Vehicle finalVehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        assertThat(finalVehicle)
                .satisfies(vehicle -> {
                    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.SOLD);
                    assertThat(vehicle.getSyncStatus()).isEqualTo(SyncStatus.SYNCED);
                    assertThat(vehicle.getAutoTraderListingId()).isEqualTo("AT-789013");
                    assertThat(vehicle.getLastSyncAttempt()).isNotNull();
                    assertThat(vehicle.getMake()).isEqualTo("Toyota");
                    assertThat(vehicle.getModel()).isEqualTo("Camry");
                    assertThat(vehicle.getYear()).isEqualTo(2023);
                    assertThat(vehicle.getVin()).isEqualTo("1HGCM82633A123456");
                    assertThat(vehicle.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(25000.00));
                });
    }

}