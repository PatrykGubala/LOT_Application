package org.example.service;

import org.example.model.Flight;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FlightService {
    void addFlight(Flight flight);
    void updateFlight(long flightNumber, Flight updatedFlight);
    void deleteFlight(long flightNumber);
    List<Flight> searchFlights(Map<String, String> criteria);
    boolean assignPassengerToFlight(long flightNumber, int seatNumber, long passengerId);
    boolean unassignPassengerFromFlight(long flightNumber, int seatNumber);
    List<Flight> getAllFlights();
    Optional<Flight> getFlightByFlightNumber(long flightNumber);
}
