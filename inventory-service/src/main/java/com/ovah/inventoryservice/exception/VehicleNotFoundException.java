package com.ovah.inventoryservice.exception;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Long id) {
        super("Vehicle not found with id: " + id);
    }

    public VehicleNotFoundException(String message) {
        super(message);
    }
}
