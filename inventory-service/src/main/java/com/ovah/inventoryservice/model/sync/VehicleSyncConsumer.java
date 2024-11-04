package com.ovah.inventoryservice.model.sync;

import com.ovah.inventoryservice.model.Vehicle;

import java.util.UUID;

public record VehicleSyncConsumer(
        UUID vehicleId,
        EventType type,
        Vehicle vehicle
) {
}
