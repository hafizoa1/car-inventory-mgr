package com.ovah.inventoryservice.service.autotrader;

import com.ovah.inventoryservice.model.Vehicle;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockAutoTraderService {

    public String createListing(Vehicle vehicle) {
        return "AT-111";
    }

    public String updateListing(String listingId, Vehicle vehicle) {
        return "AT-111";
    }

}
