package org.example.service;

import org.example.model.Passenger;
import java.util.List;
import java.util.Optional;

public interface PassengerService {
    void addPassenger(Passenger passenger);
    void updatePassenger(long id, Passenger passenger);
    void deletePassenger(long id);
    List<Passenger> getAllPassengers();
    Optional<Passenger> getPassengerById(long id);
}
