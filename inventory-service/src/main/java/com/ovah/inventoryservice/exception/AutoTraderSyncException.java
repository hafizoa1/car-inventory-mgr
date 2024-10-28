package com.ovah.inventoryservice.exception;

public class AutoTraderSyncException extends RuntimeException {

    public AutoTraderSyncException(String message) {
        super(message);
    }

    public AutoTraderSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    // Specific static factory methods for common sync errors
    public static AutoTraderSyncException listingNotFound(String listingId) {
        return new AutoTraderSyncException("AutoTrader listing not found: " + listingId);
    }

    public static AutoTraderSyncException syncFailed(Long vehicleId, String reason) {
        return new AutoTraderSyncException(
                String.format("Failed to sync vehicle %d with AutoTrader: %s", vehicleId, reason)
        );
    }
}