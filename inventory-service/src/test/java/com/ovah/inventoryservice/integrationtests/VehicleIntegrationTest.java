package com.ovah.inventoryservice.integrationtests;

import com.ovah.inventoryservice.helper.VehicleTestHelper;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.VehicleStatus;
import com.ovah.inventoryservice.service.autotrader.MockAutoTraderService;
import com.ovah.inventoryservice.model.sync.SyncStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.awaitility.Awaitility.await;
import static com.ovah.inventoryservice.model.VehicleStatus.AVAILABLE;


public class VehicleIntegrationTest extends VehicleTestHelper {

    @MockBean
    private MockAutoTraderService autoTraderService;


    @Nested
        class VehicleCreationTests {

            @Test
            void successfullyCreateVehicle() {

                Vehicle vehicleRequest = givenValidVehicle();

                ResponseEntity<Vehicle> response = whenPostRequestIsMade(vehicleRequest);

                thenResponseIsCreated(response);

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
            void shouldSuccessfullyCreateAndSyncVehicle() {
                // Given
                Vehicle vehicleRequest = givenValidVehicle();

                when(autoTraderService.createListing(any(Vehicle.class)))
                        .thenReturn("AT-123456");

                // When
                ResponseEntity<Vehicle> response = whenPostRequestIsMade(vehicleRequest);

                // Then
                thenResponseIsCreated(response);

                // Wait for sync and verify
                await()
                        .atMost(Duration.ofSeconds(5))
                        .until(() -> {
                            Vehicle saved = vehicleRepository.findById(response.getBody().getId()).orElseThrow();
                            return SyncStatus.SYNCED.equals(saved.getSyncStatus());
                        });

                Vehicle savedVehicle = vehicleRepository.findById(response.getBody().getId()).orElseThrow();
                assertThat(savedVehicle)
                        .satisfies(vehicle -> {
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
            void unsuccessfulCreationWithMissingFields() {
                Vehicle invalidVehicle = givenInvalidVehicle();

                ResponseEntity<String> response = whenBadPostRequestIsMade(invalidVehicle);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            }

            /*@Test
            void duplicateVinNumber() { // probably rename these tests

            }*/

            @Test
            void InvalidDataTypeInField() {
                Vehicle vehcicleWithInvalidFormat = givenVehicleWithInvalidFormats();
                ResponseEntity<String> response = whenBadPostRequestIsMade(vehcicleWithInvalidFormat);


                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            void shouldNotCreateVehicleWithNegativePrice() {
            // Given a vehicle with an invalid (negative) price
                Vehicle invalidVehicle = givenValidVehicle();
                invalidVehicle.setPrice(BigDecimal.valueOf(-1000));

            // When posting the vehicle
                ResponseEntity<String> response = whenBadPostRequestIsMade(invalidVehicle);

            // Then the response should be BAD_REQUEST
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }


        }

        @Nested
        class VehicleUpdateTests {
            @Test
            void shouldSuccessfullyUpdateAndSyncVehicle() {
                // Given
                Vehicle existingVehicle = givenExistingVehicle();
                existingVehicle.setPrice(BigDecimal.valueOf(26000.00));
                existingVehicle.setStatus(VehicleStatus.SOLD);

                when(autoTraderService.updateListing(eq(existingVehicle.getAutoTraderListingId()), any(Vehicle.class)))
                        .thenReturn(existingVehicle.getAutoTraderListingId());

                // When
                ResponseEntity<Vehicle> response = whenPutRequestIsMade(existingVehicle, existingVehicle.getId());

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                // Wait for sync and verify
                await()
                        .atMost(Duration.ofSeconds(5))
                        .until(() -> {
                            Vehicle updated = vehicleRepository.findById(existingVehicle.getId()).orElseThrow();
                            return SyncStatus.SYNCED.equals(updated.getSyncStatus());
                        });

                Vehicle updatedVehicle = vehicleRepository.findById(existingVehicle.getId()).orElseThrow();
                assertThat(updatedVehicle)
                        .satisfies(vehicle -> {
                            assertThat(vehicle.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(26000.00));
                            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.SOLD);
                            assertThat(vehicle.getSyncStatus()).isEqualTo(SyncStatus.SYNCED);
                        });
            }


            @Test
            void shouldReturnNotFoundWhenUpdatingNonexistentVehicle() {
                // Given a vehicle ID that does not exist
                UUID nonExistentId = UUID.randomUUID();
                Vehicle updateRequest = givenValidVehicle();
                updateRequest.setId(nonExistentId);

                // When attempting to update
                ResponseEntity<String> response = whenBadPutRequestIsMade(updateRequest, nonExistentId);

                // Then ensure the response is NOT_FOUND
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }

        @Nested
        class VehicleRetrievalTests {

            @Test
            void successfullyRetrieveAllVehicles() {
                // Given
                givenMultipleVehiclesInDatabase();


                // When
                ResponseEntity<List<Vehicle>> response = whenGetAllRequestIsMade();

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotEmpty();
                assertThat(response.getBody().size()).isEqualTo(2);
            }

            @Test
            void retrieveNoVehiclesReturnsNoContent() {
                // Given no vehicles are available
                when(vehicleRepository.findAll()).thenReturn(Collections.emptyList());

                // When
                ResponseEntity<List<Vehicle>> response = whenGetAllRequestIsMade();

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(response.getBody()).isNull();
            }
            @Test
            void successfullyRetrieveVehicle() {
                // Given
                Vehicle existingVehicle = givenExistingVehicle();

                // When
                ResponseEntity<Vehicle> response = whenGetRequestIsMade(existingVehicle.getId());

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                        .isNotNull()
                        .satisfies(vehicle -> {
                            assertThat(vehicle.getId()).isEqualTo(existingVehicle.getId());
                            assertThat(vehicle.getVin()).isEqualTo(existingVehicle.getVin());
                        });
            }

            @Test
            void unsuccessfullyRetrieveVehicle() {
                // When

                ResponseEntity<Vehicle> response = whenGetRequestIsMade(UUID.randomUUID());

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }

        @Nested
        class VehicleDeletionTests {
            @Test
            void successfullyDeleteAndSyncVehicle() {
                // Given
                Vehicle existingVehicle = givenExistingVehicle();

                when(autoTraderService.deleteListing(existingVehicle.getAutoTraderListingId()))
                        .thenReturn(true);

                // When
                ResponseEntity<Void> response = whenDeleteRequestIsMade(existingVehicle.getId());

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(vehicleRepository.findById(existingVehicle.getId())).isEmpty();
            }

            @Test
            void shouldReturnNotFoundWhenDeletingNonexistentVehicle() {
                // Given a random UUID that does not correspond to any vehicle
                UUID nonExistentId = UUID.randomUUID();

                // When trying to delete this vehicle
                ResponseEntity<Void> response = whenDeleteRequestIsMade(nonExistentId);

                // Then ensure the response is NOT_FOUND
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }

}

