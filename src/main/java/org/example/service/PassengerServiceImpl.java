package org.example.service;

import org.example.model.Passenger;
import org.example.repository.PassengerRepository;
import java.util.List;
import java.util.Optional;

public class PassengerServiceImpl implements PassengerService {
    private final PassengerRepository passengerRepository;

    public PassengerServiceImpl(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public void addPassenger(Passenger passenger) throws IllegalArgumentException {
        if (passenger.getFirstName().isEmpty() || passenger.getLastName().isEmpty() || passenger.getPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("Passenger data cannot be empty.");
        }
        passengerRepository.save(passenger);
    }

    @Override
    public void updatePassenger(long id, Passenger passenger) {
        passengerRepository.update(id, passenger);
    }

    @Override
    public void deletePassenger(long id) {
        passengerRepository.delete(id);
    }

    @Override
    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    @Override
    public Optional<Passenger> getPassengerById(long id) {
        return passengerRepository.findById(id);
    }
}
