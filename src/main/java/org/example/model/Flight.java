package org.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

public class Flight {
    private static final AtomicLong idGenerator = new AtomicLong(1);
    private long flightNumber;
    private String route;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private int initialAvailableSeats;
    private Map<Integer, Long> seatMap;

    public Flight() {
        this.flightNumber = idGenerator.getAndIncrement();
    }

    @JsonCreator
    public Flight(@JsonProperty("flightNumber") Long flightNumber,
                  @JsonProperty("route") String route,
                  @JsonProperty("departureDate") LocalDate departureDate,
                  @JsonProperty("departureTime") LocalTime departureTime,
                  @JsonProperty("availableSeats") int availableSeats,
                  @JsonProperty("seatMap") Map<Integer, Long> seatMap) {
        this.flightNumber = (flightNumber != null) ? flightNumber : idGenerator.getAndIncrement();
        this.route = route;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.initialAvailableSeats = availableSeats;
        this.seatMap = seatMap != null ? new HashMap<>(seatMap) : new HashMap<>(availableSeats);

    }

    public Map<Integer, Long> getSeatMap() {
        return seatMap;
    }

    public void setSeatMap(Map<Integer, Long> seatMap) {
        this.seatMap = seatMap;
    }
    public boolean assignPassengerToSeat(int seatNumber, long passengerId) {
        if (seatNumber > 0 && seatNumber <= initialAvailableSeats && !seatMap.containsKey(seatNumber)) {
            seatMap.put(seatNumber, passengerId);
            return true;
        }
        return false;
    }
    public boolean unassignPassengerFromSeat(int seatNumber) {
        if (seatMap.containsKey(seatNumber)) {
            seatMap.remove(seatNumber);
            return true;
        }
        return false;
    }
    public long getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(long flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }
    public int getAvailableSeats() {
        return initialAvailableSeats - seatMap.size();
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }



    public void setAvailableSeats(int availableSeats) {
        this.initialAvailableSeats = availableSeats;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flightNumber=" + flightNumber +
                ", route='" + route + '\'' +
                ", departureDate=" + departureDate +
                ", departureTime=" + departureTime +
                ", availableSeats=" + getAvailableSeats() +
                ", seatMap=" + seatMap.toString() +
                '}';
    }
}
