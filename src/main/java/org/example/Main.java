package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpServer;
import org.example.controller.FlightController;
import org.example.controller.PassengerController;
import org.example.repository.FlightRepository;
import org.example.repository.FlightRepositoryImpl;
import org.example.repository.PassengerRepository;
import org.example.repository.PassengerRepositoryImpl;
import org.example.service.FlightService;
import org.example.service.FlightServiceImpl;
import org.example.service.PassengerService;
import org.example.service.PassengerServiceImpl;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Main {
    private static final String URL = System.getenv("DATABASE_URL");
    private static final String USER = System.getenv("DATABASE_USER");
    private static final String PASSWORD = System.getenv("DATABASE_PASS");
    private static Connection connection;

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            startServer();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    private static void startServer() throws IOException {
        FlightRepository flightRepository = new FlightRepositoryImpl(connection);
        FlightService flightService = new FlightServiceImpl(flightRepository);

        PassengerRepository passengerRepository = new PassengerRepositoryImpl(connection);
        PassengerService passengerService = new PassengerServiceImpl(passengerRepository);

        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        FlightController flightController = new FlightController(flightService, objectMapper);
        PassengerController passengerController = new PassengerController(passengerService, objectMapper);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/flights", flightController);
        server.createContext("/passengers", passengerController);
        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port 8000");
    }

    public static void stop() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing the connection: " + e.getMessage());
        }
    }
}
