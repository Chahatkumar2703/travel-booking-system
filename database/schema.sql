-- ==========================================================
-- Online Travel Booking Platform Database Schema
-- Database: travel_booking_system
-- Technology: MySQL 8.0+
-- ==========================================================

CREATE DATABASE IF NOT EXISTS travel_booking_system;
USE travel_booking_system;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    role ENUM('ADMIN', 'AGENT', 'TRAVELER', 'USER') DEFAULT 'TRAVELER',
    status ENUM('ACTIVE', 'DISABLED', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Destinations Table
CREATE TABLE IF NOT EXISTS destinations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    state VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    attractions TEXT NOT NULL,
    best_time VARCHAR(100) NOT NULL,
    image_url VARCHAR(255) DEFAULT '',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Flights Table
CREATE TABLE IF NOT EXISTS flights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    agent_id INT NOT NULL,
    airline VARCHAR(100) NOT NULL,
    flight_number VARCHAR(50) NOT NULL,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_date DATE NOT NULL,
    departure_time VARCHAR(20) NOT NULL,
    arrival_time VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    available_seats INT NOT NULL DEFAULT 60,
    image_url VARCHAR(255) DEFAULT '',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flight_agent FOREIGN KEY (agent_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Hotels Table
CREATE TABLE IF NOT EXISTS hotels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    agent_id INT NOT NULL DEFAULT 1,
    hotel_name VARCHAR(150) NOT NULL,
    destination_id INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    location VARCHAR(150) NOT NULL DEFAULT '',
    room_type VARCHAR(50) NOT NULL DEFAULT 'Deluxe',
    price_per_night DECIMAL(10, 2) NOT NULL,
    available_rooms INT NOT NULL DEFAULT 10,
    rating DECIMAL(2, 1) NOT NULL DEFAULT 4.5,
    description TEXT,
    image_url VARCHAR(255) DEFAULT '',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hotel_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
    CONSTRAINT fk_hotel_agent FOREIGN KEY (agent_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Rental Cars Table
CREATE TABLE IF NOT EXISTS cars (
    id INT AUTO_INCREMENT PRIMARY KEY,
    agent_id INT NOT NULL,
    car_name VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    car_type VARCHAR(50) NOT NULL DEFAULT 'Sedan',
    price_per_day DECIMAL(10, 2) NOT NULL,
    available_units INT NOT NULL DEFAULT 5,
    image_url VARCHAR(255) DEFAULT '',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_car_agent FOREIGN KEY (agent_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Travel Packages Table
CREATE TABLE IF NOT EXISTS packages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    agent_id INT NOT NULL DEFAULT 1,
    package_name VARCHAR(150) NOT NULL,
    destination_id INT NOT NULL,
    duration_days INT NOT NULL,
    duration_nights INT NOT NULL,
    price_per_person DECIMAL(10, 2) NOT NULL,
    places_covered TEXT NOT NULL,
    hotel_included BOOLEAN DEFAULT TRUE,
    food_included BOOLEAN DEFAULT TRUE,
    transport_included BOOLEAN DEFAULT TRUE,
    description TEXT NOT NULL,
    image_url VARCHAR(255) DEFAULT '',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_package_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
    CONSTRAINT fk_package_agent FOREIGN KEY (agent_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Bookings Table (Universal for Flight, Hotel, Car, and Package)
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    booking_type ENUM('PACKAGE', 'FLIGHT', 'HOTEL', 'CAR') DEFAULT 'PACKAGE',
    item_id INT NULL,
    item_name VARCHAR(150) NULL,
    flight_id INT NULL,
    hotel_id INT NULL,
    car_id INT NULL,
    package_id INT NULL,
    agent_id INT NULL,
    travel_date DATE NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    persons INT NOT NULL DEFAULT 1,
    quantity INT NOT NULL DEFAULT 1,
    package_cost DECIMAL(10, 2) DEFAULT 0.00,
    hotel_cost DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    special_requests TEXT,
    booking_status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_flight FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_car FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(50) NOT NULL UNIQUE,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('UPI', 'CARD', 'NET_BANKING', 'CASH') NOT NULL,
    payment_details VARCHAR(255),
    payment_status ENUM('SUCCESS', 'FAILED', 'PENDING') DEFAULT 'SUCCESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Messages / Feedback Table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    agent_id INT NULL,
    subject VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    reply TEXT NULL,
    status ENUM('OPEN', 'REPLIED', 'CLOSED') DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    replied_at TIMESTAMP NULL,
    CONSTRAINT fk_message_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_agent FOREIGN KEY (agent_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. System Settings Table
CREATE TABLE IF NOT EXISTS system_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(50) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
