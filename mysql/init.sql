CREATE DATABASE IF NOT EXISTS lot_database;
USE lot_database;

CREATE TABLE IF NOT EXISTS flights (
    flightNumber BIGINT NOT NULL AUTO_INCREMENT,
    route VARCHAR(255) NOT NULL,
    departureDate DATE NOT NULL,
    departureTime TIME NOT NULL,
    availableSeats INT NOT NULL,
    PRIMARY KEY (flightNumber)
    );

CREATE TABLE IF NOT EXISTS passengers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    phoneNumber VARCHAR(20),
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS seat_assignments (
    flightNumber BIGINT NOT NULL,
    seatNumber INT NOT NULL,
    passengerId BIGINT NOT NULL,
    PRIMARY KEY (flightNumber, seatNumber),
    FOREIGN KEY (flightNumber) REFERENCES flights(flightNumber),
    FOREIGN KEY (passengerId) REFERENCES passengers(id)
    );

INSERT INTO flights (route, departureDate, departureTime, availableSeats) VALUES
    ('Kielce-Warszawa', '2024-04-22', '15:30:00', 180),
    ('Warszawa-Kielce', '2024-04-23', '12:00:00', 200);

INSERT INTO passengers (firstName, lastName, phoneNumber) VALUES
    ('Patryk', 'Gubala', '333222111'),
    ('Jan', 'Kowalski', '111222333');