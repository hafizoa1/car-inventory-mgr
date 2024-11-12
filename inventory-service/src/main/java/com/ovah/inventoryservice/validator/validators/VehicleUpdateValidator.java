package com.ovah.inventoryservice.validator.validators;

import com.ovah.inventoryservice.exception.ValidationException;
import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.validator.BaseValidator;
import com.ovah.inventoryservice.validator.ValidationError;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class VehicleUpdateValidator extends BaseValidator<Vehicle> {
    private static final Pattern VIN_PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");

    @Override
    protected void doValidate(Vehicle vehicle) throws ValidationException {
        clearErrors();

        if (vehicle == null) {
            throw ValidationException.requiredFieldMissing("vehicle", null);
        }

        validateVin(vehicle);
        // Only validate fields that are required for updates
        // (e.g., no need to validate make, model, year, price, status, syncStatus)

        returnErrors();
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
}