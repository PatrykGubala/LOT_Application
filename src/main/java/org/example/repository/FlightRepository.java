package org.example.repository;

import org.example.model.Flight;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FlightRepository {
    void save(Flight flight);
    void update(long flightNumber, Flight updatedFlight);
    void delete(long flightNumber);
    List<Flight> findAll();
    Optional<Flight> findByFlightNumber(long flightNumber);
    void updateSeatAssignments(long flightNumber, Map<Integer, Long> seatMap) throws SQLException;

    }

