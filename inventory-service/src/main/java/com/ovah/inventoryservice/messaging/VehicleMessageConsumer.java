package com.ovah.inventoryservice.messaging;

import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.sync.SyncStatus;
import com.ovah.inventoryservice.model.sync.VehicleSyncConsumer;
import com.ovah.inventoryservice.repository.VehicleRepository;
import com.ovah.inventoryservice.service.autotrader.MockAutoTraderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleMessageConsumer {

    private final MockAutoTraderService autoTraderService;
    private final VehicleRepository vehicleRepository;

    @RabbitListener(queues = "${rabbitmq.queue.vehicle-sync}")
    public void handleVehicleSync(VehicleSyncConsumer message) {
        log.info("Received sync message for vehicle: {}", message.vehicleId());

        try {
            switch (message.type()) {
                case CREATED -> handleCreate(message.vehicle());
                case UPDATED -> handleUpdate(message.vehicle().getAutoTraderListingId(), message.vehicle());
                case STATUS_CHANGED -> handleStatusChange(message.vehicle());
                case DELETED -> handleDelete(message.vehicle());
            }
        } catch (Exception e) {
            log.error("Failed to sync vehicle: {}", message.vehicleId(), e);
            markSyncFailed(message.vehicle(), e.getMessage());
        }
    }

    private void handleDelete(Vehicle vehicle) {
    }

    private void handleStatusChange(Vehicle vehicle) {
    }

    private void handleUpdate(String listing, Vehicle vehicle) { //you want to change this
        try {
            String listingId = autoTraderService.updateListing(listing, vehicle);

            vehicle.setAutoTraderListingId(listingId);
            vehicle.setSyncStatus(SyncStatus.SYNCED);
            vehicle.setLastSyncAttempt(LocalDateTime.now());

            vehicleRepository.save(vehicle);
            log.info("Successfully synced vehicle {} to AutoTrader", vehicle.getId());
        } catch (Exception e) {
            markSyncFailed(vehicle, e.getMessage());
            throw e;
        }
    }

    private void handleCreate(Vehicle vehicle) {
        try {
            String listingId = autoTraderService.createListing(vehicle);

            vehicle.setAutoTraderListingId(listingId);
            vehicle.setSyncStatus(SyncStatus.SYNCED);
            vehicle.setLastSyncAttempt(LocalDateTime.now());

            vehicleRepository.save(vehicle);
            log.info("Successfully synced vehicle {} to AutoTrader", vehicle.getId());
        } catch (Exception e) {
            markSyncFailed(vehicle, e.getMessage());
            throw e;
        }
    }

    private void markSyncFailed(Vehicle vehicle, String error) {
        vehicle.setSyncStatus(SyncStatus.FAILED);
        vehicle.setLastSyncAttempt(LocalDateTime.now());
        //vehicle.setSyncError(error);
        vehicleRepository.save(vehicle);
    }
}


