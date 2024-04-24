package org.example.utility;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class CORSHandler {
    public static void applyCorsPolicy(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
    }

    public static void handlePreflightRequest(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            applyCorsPolicy(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        }
    }
}
