package com.ovah.inventoryservice.repository;

import com.ovah.inventoryservice.model.AutoTraderVehicle;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockAutoTraderRepository extends JpaRepository<AutoTraderVehicle, Long> {

}
