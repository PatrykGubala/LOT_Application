package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.Passenger;
import org.example.service.PassengerService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PassengerController implements HttpHandler {
    private final PassengerService passengerService;
    private final ObjectMapper objectMapper;

    public PassengerController(PassengerService passengerService, ObjectMapper objectMapper) {
        this.passengerService = passengerService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        switch (requestMethod) {
            case "GET":
                handleGetRequest(exchange);
                break;
            case "POST":
                handlePostRequest(exchange);
                break;
            case "PUT":
                handlePutRequest(exchange);
                break;
            case "DELETE":
                handleDeleteRequest(exchange);
                break;
            default:
                sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 3 && pathParts[1].equals("passengers")) {
            long id;
            try {
                id = Long.parseLong(pathParts[2]);
                Optional<Passenger> passenger = passengerService.getPassengerById(id);
                if (passenger.isPresent()) {
                    String jsonResponse = objectMapper.writeValueAsString(passenger.get());
                    sendResponse(exchange, 200, jsonResponse);
                } else {
                    sendResponse(exchange, 404, "Not Found: Passenger not found");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Bad Request: Invalid Passenger ID");
            }
        } else if (pathParts.length == 2 && pathParts[1].equals("passengers")) {
            List<Passenger> passengers = passengerService.getAllPassengers();
            String jsonResponse = objectMapper.writeValueAsString(passengers);
            sendResponse(exchange, 200, jsonResponse);
        } else {
            sendResponse(exchange, 400, "Bad Request: Incorrect URL format");
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 400, "Bad Request: JSON data required");
            return;
        }

        try (InputStream requestBody = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody))) {
            String json = reader.lines().collect(Collectors.joining());
            Passenger passenger = objectMapper.readValue(json, Passenger.class);
            passengerService.addPassenger(passenger);
            sendResponse(exchange, 201, "Passenger added successfully");
        } catch (IOException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid JSON data");
        }
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length != 3) {
            sendResponse(exchange, 400, "Bad Request: URL should contain the passenger ID");
            return;
        }

        long id;
        try {
            id = Long.parseLong(pathParts[2]);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid Passenger ID");
            return;
        }

        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 400, "Bad Request: JSON data required");
            return;
        }

        try (InputStream requestBody = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody))) {
            String json = reader.lines().collect(Collectors.joining());
            Passenger updatedPassenger = objectMapper.readValue(json, Passenger.class);
            passengerService.updatePassenger(id, updatedPassenger);
            sendResponse(exchange, 200, "Passenger updated successfully");
        } catch (IOException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid JSON data");
        }
    }
    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length != 3) {
            sendResponse(exchange, 400, "Bad Request: URL should contain the passenger ID");
            return;
        }

        long id;
        try {
            id = Long.parseLong(pathParts[2]);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid Passenger ID");
            return;
        }

        passengerService.deletePassenger(id);
        sendResponse(exchange, 200, "Passenger deleted successfully");
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
