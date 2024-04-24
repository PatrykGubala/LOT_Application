package org.example.repository;

import org.example.model.Passenger;
import java.util.List;
import java.util.Optional;

public interface PassengerRepository {
    void save(Passenger passenger);
    void update(long id, Passenger passenger);
    void delete(long id);
    List<Passenger> findAll();
    Optional<Passenger> findById(long id);
}