package com.ovah.inventoryservice.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleDto {

    private String vin;
    private String make;
    private String model;
    private Integer year;
    private BigDecimal price;
    private VehicleStatus status;

}