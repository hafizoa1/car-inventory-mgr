package com.ovah.inventoryservice.messaging;

import com.ovah.inventoryservice.model.Vehicle;
import com.ovah.inventoryservice.model.sync.VehicleSyncConsumer;
import com.ovah.inventoryservice.model.sync.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.vehicle-sync}")
    private String exchange;

    @Value("${rabbitmq.routing-key.vehicle-sync}")
    private String routingKey;

    public void sendVehicleCreatedMessage(Vehicle vehicle) {
        VehicleSyncConsumer message = new VehicleSyncConsumer(
                vehicle.getId(),
                EventType.CREATED,
                vehicle
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void sendVehicleUpdatedMessage(Vehicle vehicle) {
        VehicleSyncConsumer message = new VehicleSyncConsumer(
                vehicle.getId(),
                EventType.UPDATED,
                vehicle
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void sendVehicleDeletedMessage(Vehicle vehicle) {

        VehicleSyncConsumer message = new VehicleSyncConsumer(
                vehicle.getId(),
                EventType.DELETED,
                vehicle
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, message);

    }

}

