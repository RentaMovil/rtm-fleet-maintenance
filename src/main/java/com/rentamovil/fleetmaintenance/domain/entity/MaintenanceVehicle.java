package com.rentamovil.fleetmaintenance.domain.entity;

// The bare minimum of the vehicle to show a maintenance in the history without another request
public record MaintenanceVehicle(Integer id, String plate, String brand, String model) {
}
