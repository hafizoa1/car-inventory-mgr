package com.ovah.inventoryservice.validator.validators;


import com.ovah.inventoryservice.exception.ValidationException;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.validator.BaseValidator;
import com.ovah.inventoryservice.validator.ValidationError;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;


@Component
public class VehicleValidator extends BaseValidator<Vehicle> {
    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private static final int MIN_YEAR = 1900;
    private static final Pattern VIN_PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s-]+$");

    @Override
    protected void doValidate(Vehicle vehicle) throws ValidationException {
        if (vehicle == null) {
            throw ValidationException.requiredFieldMissing("vehicle", null);
        }

        validateVin(vehicle);
        validateBasicInfo(vehicle);
        validatePrice(vehicle);
        validateStatus(vehicle);
        validateSyncInfo(vehicle);
    }

    private void validateVin(Vehicle vehicle) {
        String vin = vehicle.getVin();
        if (vin == null || vin.trim().isEmpty()) {
            addError("vin", "VIN is required", ValidationError.ErrorCode.REQUIRED_FIELD, vehicle);
            return;
        }

        if (!VIN_PATTERN.matcher(vin).matches()) {
            addError(
                    "vin",
                    "VIN must be 17 characters and contain only alphanumeric characters (excluding I, O, Q)",
                    ValidationError.ErrorCode.INVALID_FORMAT,
                    vehicle
            );
        }
    }

    private void validateBasicInfo(Vehicle vehicle) {
        // Make validation
        if (vehicle.getMake() == null || vehicle.getMake().trim().isEmpty()) {
            addError("make", "Make is required", ValidationError.ErrorCode.REQUIRED_FIELD, vehicle);
        } else if (!VALID_NAME_PATTERN.matcher(vehicle.getMake()).matches()) {
            throw ValidationException.invalidField(
                    "make",
                    "Make can only contain letters, numbers, spaces, and hyphens",
                    vehicle
            );
        }

        // Model validation
        if (vehicle.getModel() == null || vehicle.getModel().trim().isEmpty()) {
            addError("model", "Model is required", ValidationError.ErrorCode.REQUIRED_FIELD, vehicle);
        } else if (!VALID_NAME_PATTERN.matcher(vehicle.getModel()).matches()) {
            throw ValidationException.invalidField(
                    "model",
                    "Model can only contain letters, numbers, spaces, and hyphens",
                    vehicle
            );
        }

        // Year validation
        Integer year = vehicle.getYear();
        if (year == null) {
            addError("year", "Year is required", ValidationError.ErrorCode.REQUIRED_FIELD, vehicle);
        } else if (year < MIN_YEAR || year > CURRENT_YEAR + 1) {
            addError(
                    "year",
                    String.format("Year must be between %d and %d", MIN_YEAR, CURRENT_YEAR + 1),
                    ValidationError.ErrorCode.OUT_OF_RANGE,
                    vehicle
            );
        }
    }

    private void validatePrice(Vehicle vehicle) {
        BigDecimal price = vehicle.getPrice();
        if (price == null) {
            addError("price", "Price is required", ValidationError.ErrorCode.REQUIRED_FIELD, vehicle);
            return;
        }

        if (price.compareTo(MIN_PRICE) <= 0) {
            addError(
                    "price",
                    "Price must be greater than zero",
                    ValidationError.ErrorCode.OUT_OF_RANGE,
                    vehicle
            );
        }
    }

    private void validateStatus(Vehicle vehicle) {
        if (vehicle.getStatus() == null) {
            addError("status", "Vehicle status is required", ValidationError.ErrorCode.REQUIRED_FIELD, vehicle);
        }
    }

    private void validateSyncInfo(Vehicle vehicle) {
        // Validate sync status
        if (vehicle.getSyncStatus() == null) {
            addError("syncStatus", "Sync status is required", ValidationError.ErrorCode.REQUIRED_FIELD, vehicle);
        }

        // Validate lastSyncAttempt if autoTraderListingId exists
        if (vehicle.getAutoTraderListingId() != null && vehicle.getLastSyncAttempt() == null) {
            addError(
                    "lastSyncAttempt",
                    "Last sync attempt is required when AutoTrader listing ID is present",
                    ValidationError.ErrorCode.BUSINESS_RULE_VIOLATION,
                    vehicle
            );
        }
    }
}