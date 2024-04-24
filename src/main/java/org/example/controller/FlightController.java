package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.Flight;
import org.example.service.FlightService;
import org.example.utility.CORSHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlightController implements HttpHandler {
    private final FlightService flightService;
    private final ObjectMapper objectMapper;

    public FlightController(FlightService flightService, ObjectMapper objectMapper) {
        this.flightService = flightService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CORSHandler.handlePreflightRequest(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        }
        CORSHandler.applyCorsPolicy(exchange);
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (path.matches("/flights/\\d+/seats/\\d+")) {
            switch (requestMethod) {
                case "PUT":
                    handleAssignPassengerRequest(exchange);
                    break;
                case "DELETE":
                    handleUnassignPassengerRequest(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
                    break;
            }
        } else {
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
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        List<Flight> flights = flightService.searchFlights(params);
        String response = objectMapper.writeValueAsString(flights);
        sendResponse(exchange, 200, response);
    }
    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
        }
        return result;
    }
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        if ("application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            try (InputStream requestBody = exchange.getRequestBody();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody))) {
                String json = reader.lines().collect(java.util.stream.Collectors.joining());
                Flight flight = objectMapper.readValue(json, Flight.class);
                flightService.addFlight(flight);
                sendResponse(exchange, 200, "Flight added successfully");
            } catch (IOException e) {
                sendResponse(exchange, 400, "Bad Request: Invalid JSON data");
            }
        } else {
            sendResponse(exchange, 400, "Bad Request: JSON data required");
        }
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length == 3 && pathParts[1].equals("flights")) {
            long flightNumber = Long.parseLong(pathParts[2]);
            if ("application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
                try (InputStream requestBody = exchange.getRequestBody();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody))) {
                    String json = reader.lines().collect(java.util.stream.Collectors.joining());
                    Flight flight = objectMapper.readValue(json, Flight.class);
                    if (flight.getFlightNumber() == flightNumber) {
                        flightService.updateFlight(flightNumber, flight);
                        sendResponse(exchange, 200, "Flight updated successfully");
                    } else {
                        sendResponse(exchange, 400, "Bad Request: Flight number mismatch between URL and body");
                    }
                } catch (IOException e) {
                    sendResponse(exchange, 400, "Bad Request: Invalid JSON data");
                }
            } else {
                sendResponse(exchange, 400, "Bad Request: Content-Type must be application/json");
            }
        } else {
            sendResponse(exchange, 400, "Bad Request: Invalid URI structure");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length == 3 && pathParts[1].equals("flights")) {
            long flightNumber = Long.parseLong(pathParts[2]);
            flightService.deleteFlight(flightNumber);
            sendResponse(exchange, 200, "Flight deleted successfully");
        } else {
            sendResponse(exchange, 400, "Bad Request: Invalid URI structure");
        }
    }

    private void handleAssignPassengerRequest(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        long flightNumber = Long.parseLong(pathParts[2]);
        int seatNumber = Integer.parseInt(pathParts[4]);

        if (!"application/json".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("Content-Type"))) {
            sendResponse(exchange, 400, "Bad Request: JSON data required");
            return;
        }

        String json = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining());
        try {
            Map<String, Long> requestData = objectMapper.readValue(json, new TypeReference<Map<String, Long>>(){});
            Long passengerId = requestData.get("passengerId");
            if (passengerId == null) {
                sendResponse(exchange, 400, "Bad Request: Missing passengerId");
                return;
            }

            boolean result = flightService.assignPassengerToFlight(flightNumber, seatNumber, passengerId);
            if (result) {
                sendResponse(exchange, 200, "Passenger assigned successfully");
            } else {
                sendResponse(exchange, 409, "Conflict: Seat already assigned or invalid passenger ID");
            }
        } catch (JsonProcessingException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid JSON data");
        }
    }

    private void handleUnassignPassengerRequest(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length != 5 || !pathParts[1].equals("flights") || !pathParts[3].equals("seats")) {
            sendResponse(exchange, 400, "Bad Request: Invalid URI structure");
            return;
        }

        try {
            long flightNumber = Long.parseLong(pathParts[2]);
            int seatNumber = Integer.parseInt(pathParts[4]);

            boolean success = flightService.unassignPassengerFromFlight(flightNumber, seatNumber);
            if (success) {
                sendResponse(exchange, 200, "Passenger unassigned successfully");
            } else {
                sendResponse(exchange, 404, "Not Found: Seat not assigned or invalid number");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid flight or seat number format");
        }
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}