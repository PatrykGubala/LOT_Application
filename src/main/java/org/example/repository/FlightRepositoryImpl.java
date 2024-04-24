package org.example.repository;

import org.example.model.Flight;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class FlightRepositoryImpl implements FlightRepository {
    private Connection connection;

    public FlightRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Flight flight) {
        String sql = "INSERT INTO flights (flightNumber, route, departureDate, departureTime, availableSeats) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, flight.getFlightNumber());
            stmt.setString(2, flight.getRoute());
            stmt.setDate(3, Date.valueOf(flight.getDepartureDate()));
            stmt.setTime(4, Time.valueOf(flight.getDepartureTime()));
            stmt.setInt(5, flight.getAvailableSeats());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating flight failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    flight.setFlightNumber(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating flight failed, no ID obtained.");
                }
            }
            saveSeatAssignments(flight.getFlightNumber(), flight.getSeatMap());
        } catch (SQLException e) {
            throw new RuntimeException("Error saving flight", e);
        }
    }

    @Override
    public void update(long flightNumber, Flight updatedFlight) {
        String sql = "UPDATE flights SET route = ?, departureDate = ?, departureTime = ?, availableSeats = ? WHERE flightNumber = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, updatedFlight.getRoute());
            stmt.setDate(2, Date.valueOf(updatedFlight.getDepartureDate()));
            stmt.setTime(3, Time.valueOf(updatedFlight.getDepartureTime()));
            stmt.setInt(4, updatedFlight.getAvailableSeats());
            stmt.setLong(5, updatedFlight.getFlightNumber());
            stmt.executeUpdate();

            updateSeatAssignments(flightNumber, updatedFlight.getSeatMap());
        } catch (SQLException e) {
            throw new RuntimeException("Error updating flight", e);
        }
    }


    @Override
    public void delete(long flightNumber) {
        String sql = "DELETE FROM flights WHERE flightNumber = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, flightNumber);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalArgumentException("Flight with number " + flightNumber + " does not exist");
            }
            clearSeatAssignments(flightNumber);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting flight", e);
        }
    }

    @Override
    public List<Flight> findAll() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT f.*, a.seatNumber, a.passengerId FROM flights f " +
                "LEFT JOIN seat_assignments a ON f.flightNumber = a.flightNumber";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            Map<Long, Flight> flightsMap = new HashMap<>();
            while (rs.next()) {
                long flightNumber = rs.getLong("flightNumber");
                Flight flight = flightsMap.getOrDefault(flightNumber, extractFlightFromResultSet(rs));
                int seatNumber = rs.getInt("seatNumber");
                if (!rs.wasNull()) {
                    long passengerId = rs.getLong("passengerId");
                    flight.getSeatMap().put(seatNumber, passengerId);
                }
                flightsMap.putIfAbsent(flightNumber, flight);
            }
            flights.addAll(flightsMap.values());
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving flights", e);
        }
        return flights;
    }

    private Flight extractFlightFromResultSet(ResultSet rs) throws SQLException {
        Flight flight = new Flight(
                rs.getLong("flightNumber"),
                rs.getString("route"),
                rs.getDate("departureDate").toLocalDate(),
                rs.getTime("departureTime").toLocalTime(),
                rs.getInt("availableSeats"),
                new HashMap<>()
        );
        return flight;
    }

    @Override
    public Optional<Flight> findByFlightNumber(long flightNumber) {
        String sql = "SELECT * FROM flights WHERE flightNumber = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, flightNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Flight flight = extractFlightFromResultSet(rs);
                    flight.setSeatMap(loadSeatAssignments(flight.getFlightNumber()));
                    return Optional.of(flight);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding flight by flight number", e);
        }
        return Optional.empty();
    }


    private Map<Integer, Long> loadSeatAssignments(long flightNumber) {
        Map<Integer, Long> seatMap = new HashMap<>();
        String sql = "SELECT seatNumber, passengerId FROM seat_assignments WHERE flightNumber = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, flightNumber);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seatMap.put(rs.getInt("seatNumber"), rs.getLong("passengerId"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading seat assignments", e);
        }
        return seatMap;
    }
    @Override
    public void updateSeatAssignments(long flightNumber, Map<Integer, Long> seatMap) throws SQLException {
        clearSeatAssignments(flightNumber);

        String sql = "INSERT INTO seat_assignments (flightNumber, seatNumber, passengerId) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            for (Map.Entry<Integer, Long> entry : seatMap.entrySet()) {
                stmt.setLong(1, flightNumber);
                stmt.setInt(2, entry.getKey());
                stmt.setLong(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException("Error updating seat assignments", e);
        } finally {
            connection.setAutoCommit(true);
        }
    }


    private void clearSeatAssignments(long flightNumber) throws SQLException {
        String sql = "DELETE FROM seat_assignments WHERE flightNumber = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, flightNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing seat assignments", e);
        }
    }

    private void saveSeatAssignments(long flightNumber, Map<Integer, Long> seatMap) throws SQLException {
        String sql = "INSERT INTO seat_assignments (flightNumber, seatNumber, passengerId) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Map.Entry<Integer, Long> entry : seatMap.entrySet()) {
                stmt.setLong(1, flightNumber);
                stmt.setInt(2, entry.getKey());
                stmt.setLong(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving seat assignments", e);
        }
    }

}
