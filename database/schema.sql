-- ==========================================================
-- Online Travel Booking System Database Schema
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
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Destinations Table
CREATE TABLE IF NOT EXISTS destinations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    state VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    attractions TEXT NOT NULL,
    best_time VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Travel Packages Table
CREATE TABLE IF NOT EXISTS packages (
    id INT AUTO_INCREMENT PRIMARY KEY,
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
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_package_destination FOREIGN KEY (destination_id) 
        REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Hotels Table
CREATE TABLE IF NOT EXISTS hotels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hotel_name VARCHAR(150) NOT NULL,
    destination_id INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    room_type VARCHAR(50) NOT NULL DEFAULT 'Deluxe',
    price_per_night DECIMAL(10, 2) NOT NULL,
    available_rooms INT NOT NULL DEFAULT 10,
    rating DECIMAL(2, 1) NOT NULL DEFAULT 4.5,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hotel_destination FOREIGN KEY (destination_id) 
        REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    package_id INT NOT NULL,
    hotel_id INT NULL,
    travel_date DATE NOT NULL,
    persons INT NOT NULL,
    package_cost DECIMAL(10, 2) NOT NULL,
    hotel_cost DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    special_requests TEXT,
    booking_status ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Payments Table
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
