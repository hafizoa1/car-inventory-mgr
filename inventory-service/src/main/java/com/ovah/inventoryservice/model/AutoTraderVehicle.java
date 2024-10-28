package com.ovah.inventoryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "autotrader_vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTraderVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", unique = true)
    private String listingId;

    @Column(name = "vehicle_id")
    private Long vehicleId;  // Reference to our local vehicle ID

    // Core vehicle details matching AutoTrader's expected format
    @Column(name = "make", nullable = false)
    private String make;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "vin", nullable = false)
    private String vin;

    // Timestamps for when the listing was created/updated in AutoTrader
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}