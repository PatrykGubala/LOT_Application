package org.example.service;

import org.example.model.Flight;
import org.example.model.Passenger;
import org.example.repository.FlightRepositoryImpl;
import org.example.repository.PassengerRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

class ServiceTests {

    private Connection connection;
    private FlightServiceImpl flightService;
    private PassengerServiceImpl passengerService;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        flightService = new FlightServiceImpl(new FlightRepositoryImpl(connection));
        passengerService = new PassengerServiceImpl(new PassengerRepositoryImpl(connection));
        createTables();
    }
    void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS flights (" +
                            "flightNumber BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "route VARCHAR(255), " +
                            "departureDate DATE, " +
                            "departureTime TIME, " +
                            "availableSeats INT)"
            );
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS passengers (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "firstName VARCHAR(255), " +
                            "lastName VARCHAR(255), " +
                            "phoneNumber VARCHAR(20))"
            );
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS seat_assignments (" +
                            "flightNumber BIGINT, " +
                            "seatNumber INT, " +
                            "passengerId BIGINT, " +
                            "PRIMARY KEY (flightNumber, seatNumber), " +
                            "FOREIGN KEY (flightNumber) REFERENCES flights(flightNumber), " +
                            "FOREIGN KEY (passengerId) REFERENCES passengers(id))"
            );
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        connection.close();
    }

    @Test
    void testAddFlight() throws SQLException {
        Flight flight = new Flight(null, "Warszawa-Kielce", LocalDate.now(), LocalTime.now(), 150, null);
        flightService.addFlight(flight);
        Flight retrieved = flightService.getFlightByFlightNumber(flight.getFlightNumber()).orElse(null);
        Assertions.assertNotNull(retrieved, "Flight should be added and retrievable");
        System.out.println("Retrieved Flight: " + retrieved);
    }
    @Test
    void testAddPassenger() throws SQLException {
        Passenger passenger = new Passenger(null, "Patryk", "Gubala", "323232332");
        passengerService.addPassenger(passenger);
        Passenger retrieved = passengerService.getPassengerById(passenger.getId()).orElse(null);
        Assertions.assertNotNull(retrieved, "Passenger should be added and retrievable");
        System.out.println("Retrieved Passenger: " + retrieved);
    }
    @Test
    void testAssignPassengerToSeat() throws SQLException {
        Flight flight = new Flight(null, "Warszawa-Krakow", LocalDate.now(), LocalTime.now(), 10, null);
        flightService.addFlight(flight);

        Passenger passenger = new Passenger(null, "Jan", "Kowalski", "123456789");
        passengerService.addPassenger(passenger);

        int seatNumber = 5;
        boolean assigned = flightService.assignPassengerToFlight(flight.getFlightNumber(), seatNumber, passenger.getId());

        Flight updatedFlight = flightService.getFlightByFlightNumber(flight.getFlightNumber()).orElse(null);

        Assertions.assertTrue(assigned, "Passenger should be successfully assigned to the flight");
        Assertions.assertNotNull(updatedFlight, "Updated flight should be retrieved");
        Assertions.assertTrue(updatedFlight.getSeatMap().containsKey(seatNumber), "Seat map should contain the assigned seat number");
        Assertions.assertEquals(passenger.getId(), updatedFlight.getSeatMap().get(seatNumber), "Seat should be assigned to the correct passenger");

        System.out.println("Updated Seat Map: " + updatedFlight.getSeatMap());
        System.out.println("Retrieved Flight after updating seats: " + updatedFlight);

    }

    @Test
    void testUpdateFlight() throws SQLException {
        Flight flight = new Flight(null, "Route A", LocalDate.now(), LocalTime.now(), 100, null);
        flightService.addFlight(flight);
        flight.setRoute("Updated Route");
        flightService.updateFlight(flight.getFlightNumber(), flight);
        Flight updatedFlight = flightService.getFlightByFlightNumber(flight.getFlightNumber()).orElse(null);
        Assertions.assertEquals("Updated Route", updatedFlight.getRoute(), "Flight route should be updated.");
        System.out.println("Updated Flight: " + updatedFlight);
    }

    @Test
    void testDeleteFlight() throws SQLException {
        Flight flight = new Flight(null, "Route B", LocalDate.now(), LocalTime.now(), 50, null);
        flightService.addFlight(flight);
        flightService.deleteFlight(flight.getFlightNumber());
        Optional<Flight> deletedFlight = flightService.getFlightByFlightNumber(flight.getFlightNumber());
        Assertions.assertFalse(deletedFlight.isPresent(), "Flight should be deleted.");
    }

    @Test
    void testAssignPassengerToFullFlight() throws SQLException {
        Flight flight = new Flight(null, "Route C", LocalDate.now(), LocalTime.now(), 1, null);
        flightService.addFlight(flight);

        Passenger passenger1 = new Passenger(null, "First", "Passenger", "111222333");
        passengerService.addPassenger(passenger1);
        flightService.assignPassengerToFlight(flight.getFlightNumber(), 1, passenger1.getId());

        Passenger passenger2 = new Passenger(null, "Second", "Passenger", "444555666");
        passengerService.addPassenger(passenger2);
        boolean result = flightService.assignPassengerToFlight(flight.getFlightNumber(), 2, passenger2.getId());

        Assertions.assertFalse(result, "No seats should be available for the second passenger.");
    }

    @Test
    void testUnassignNonExistentPassenger() throws SQLException {
        Flight flight = new Flight(null, "Route D", LocalDate.now(), LocalTime.now(), 5, null);
        flightService.addFlight(flight);
        boolean result = flightService.unassignPassengerFromFlight(flight.getFlightNumber(), 1);
        Assertions.assertFalse(result, "Should not unassign a non-existent passenger.");
    }

    @Test
    void testAddPassengerWithInvalidData() throws SQLException {
        Passenger passenger = new Passenger(null, "", "", "");
        Assertions.assertThrows(Exception.class, () -> {
            passengerService.addPassenger(passenger);
        }, "Should throw an exception due to invalid data.");
    }
}
