package com.ovah.inventoryservice.service.autotrader;

import com.ovah.inventoryservice.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class MockAutoTraderService {

    public String createListing(Vehicle vehicle) {
        return "AT-111";
    }

}
