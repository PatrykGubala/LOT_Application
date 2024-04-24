package org.example.repository;

import org.example.model.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PassengerRepositoryImpl implements PassengerRepository {
    private final Connection connection;

    public PassengerRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Passenger passenger) {
        String sql = "INSERT INTO passengers (firstName, lastName, phoneNumber) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, passenger.getFirstName());
            stmt.setString(2, passenger.getLastName());
            stmt.setString(3, passenger.getPhoneNumber());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        passenger.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving passenger", e);
        }
    }

    @Override
    public void update(long id, Passenger updatedPassenger) {
        String sql = "UPDATE passengers SET firstName = ?, lastName = ?, phoneNumber = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, updatedPassenger.getFirstName());
            stmt.setString(2, updatedPassenger.getLastName());
            stmt.setString(3, updatedPassenger.getPhoneNumber());
            stmt.setLong(4, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating passenger", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM passengers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting passenger", e);
        }
    }

    @Override
    public List<Passenger> findAll() {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT * FROM passengers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (!rs.next()) {
                System.out.println("No passengers found in the database.");
            } else {
                do {
                    passengers.add(extractPassengerFromResultSet(rs));
                    System.out.println("Passenger found: " + passengers.get(passengers.size() - 1));
                } while (rs.next());
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving passengers: " + e.getMessage());
            e.printStackTrace();
        }
        return passengers;
    }

    @Override
    public Optional<Passenger> findById(long id) {
        String sql = "SELECT * FROM passengers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(extractPassengerFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding passenger by id", e);
        }
        return Optional.empty();
    }

    private Passenger extractPassengerFromResultSet(ResultSet rs) throws SQLException {
        Passenger passenger = new Passenger();
        passenger.setId(rs.getLong("id"));
        passenger.setFirstName(rs.getString("firstName"));
        passenger.setLastName(rs.getString("lastName"));
        passenger.setPhoneNumber(rs.getString("phoneNumber"));
        System.out.println(passenger.toString());
        return passenger;
    }
}
