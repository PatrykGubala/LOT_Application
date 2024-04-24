package org.example.service;

import org.example.model.Flight;
import org.example.repository.FlightRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlightServiceImpl implements FlightService {
    private final FlightRepository flightRepository;

    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public void addFlight(Flight flight) {
        flightRepository.save(flight);
    }

    public void updateFlight(long flightNumber, Flight updatedFlight) {
        flightRepository.update(flightNumber, updatedFlight);
    }

    public void deleteFlight(long flightNumber) {
        flightRepository.delete(flightNumber);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Optional<Flight> getFlightByFlightNumber(long flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }

    public List<Flight> searchFlights(Map<String, String> criteria) {
        return getAllFlights().stream()
                .filter(flight -> criteria.keySet().stream()
                        .allMatch(key -> matchesCriteria(flight, key, criteria.get(key))))
                .collect(Collectors.toList());
    }

    private boolean matchesCriteria(Flight flight, String key, String value) {
        switch (key) {
            case "route":
                return flight.getRoute().equalsIgnoreCase(value);
            case "departureDate":
                return flight.getDepartureDate().toString().equals(value);
            case "departureTime":
                return flight.getDepartureTime().toString().equals(value);
            case "minAvailableSeats":
                return flight.getAvailableSeats() >= Integer.parseInt(value);
            case "maxAvailableSeats":
                return flight.getAvailableSeats() <= Integer.parseInt(value);
            default:
                return true;
        }
    }

    public boolean assignPassengerToFlight(long flightNumber, int seatNumber, long passengerId) {
        Optional<Flight> flightOpt = flightRepository.findByFlightNumber(flightNumber);
        if (flightOpt.isPresent()) {
            Flight flight = flightOpt.get();
            if (!flight.getSeatMap().containsKey(seatNumber) && flight.getAvailableSeats() > 0) {
                flight.getSeatMap().put(seatNumber, passengerId);
                try {
                    flightRepository.updateSeatAssignments(flightNumber, flight.getSeatMap());
                    return true;
                } catch (Exception e) {
                }
            }
        }
        return false;
    }
    public boolean unassignPassengerFromFlight(long flightNumber, int seatNumber) {
        Optional<Flight> flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight.isPresent()) {
            Flight currentFlight = flight.get();
            if (currentFlight.unassignPassengerFromSeat(seatNumber)) {
                try {
                    flightRepository.updateSeatAssignments(flightNumber, currentFlight.getSeatMap());
                    return true;
                } catch (SQLException e) {
                    System.err.println("SQL Exception during unassigning passenger: " + e.getMessage());
                }
            }
        }
        return false;
    }
}
