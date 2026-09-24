package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automatically initializes database schema and sample records if absent.
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        try {
            // First check if database exists, create if not
            try (Connection conn = DatabaseConnection.getServerConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS travel_booking_system;");
            } catch (Exception e) {
                // Harmless in cloud environments where database is pre-allocated
            }

            // Now connect to the database and check tables
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'users';");
                boolean usersTableExists = rs.next();
                rs.close();

                if (!usersTableExists) {
                    System.out.println("Initializing schema for travel_booking_system...");
                    createTables(stmt);
                    seedInitialData(stmt);
                    System.out.println("Database schema and sample data initialized successfully!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database initialization notice: " + e.getMessage());
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    full_name VARCHAR(100) NOT NULL," +
                "    email VARCHAR(100) NOT NULL UNIQUE," +
                "    phone VARCHAR(20) NOT NULL," +
                "    password_hash VARCHAR(256) NOT NULL," +
                "    role ENUM('USER', 'ADMIN') DEFAULT 'USER'," +
                "    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS destinations (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    name VARCHAR(100) NOT NULL UNIQUE," +
                "    state VARCHAR(100) NOT NULL," +
                "    description TEXT NOT NULL," +
                "    attractions TEXT NOT NULL," +
                "    best_time VARCHAR(100) NOT NULL," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS packages (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    package_name VARCHAR(150) NOT NULL," +
                "    destination_id INT NOT NULL," +
                "    duration_days INT NOT NULL," +
                "    duration_nights INT NOT NULL," +
                "    price_per_person DECIMAL(10, 2) NOT NULL," +
                "    places_covered TEXT NOT NULL," +
                "    hotel_included BOOLEAN DEFAULT TRUE," +
                "    food_included BOOLEAN DEFAULT TRUE," +
                "    transport_included BOOLEAN DEFAULT TRUE," +
                "    description TEXT NOT NULL," +
                "    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_package_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS hotels (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    hotel_name VARCHAR(150) NOT NULL," +
                "    destination_id INT NOT NULL," +
                "    address VARCHAR(255) NOT NULL," +
                "    room_type VARCHAR(50) NOT NULL DEFAULT 'Deluxe'," +
                "    price_per_night DECIMAL(10, 2) NOT NULL," +
                "    available_rooms INT NOT NULL DEFAULT 10," +
                "    rating DECIMAL(2, 1) NOT NULL DEFAULT 4.5," +
                "    description TEXT," +
                "    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_hotel_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    booking_code VARCHAR(50) NOT NULL UNIQUE," +
                "    user_id INT NOT NULL," +
                "    package_id INT NOT NULL," +
                "    hotel_id INT NULL," +
                "    travel_date DATE NOT NULL," +
                "    persons INT NOT NULL," +
                "    package_cost DECIMAL(10, 2) NOT NULL," +
                "    hotel_cost DECIMAL(10, 2) DEFAULT 0.00," +
                "    total_amount DECIMAL(10, 2) NOT NULL," +
                "    special_requests TEXT," +
                "    booking_status ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'CONFIRMED'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "    CONSTRAINT fk_booking_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE," +
                "    CONSTRAINT fk_booking_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    transaction_code VARCHAR(50) NOT NULL UNIQUE," +
                "    booking_id INT NOT NULL," +
                "    user_id INT NOT NULL," +
                "    amount DECIMAL(10, 2) NOT NULL," +
                "    payment_method ENUM('UPI', 'CARD', 'NET_BANKING', 'CASH') NOT NULL," +
                "    payment_details VARCHAR(255)," +
                "    payment_status ENUM('SUCCESS', 'FAILED', 'PENDING') DEFAULT 'SUCCESS'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE," +
                "    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );
    }

    private static void seedInitialData(Statement stmt) throws SQLException {
        // Users (admin123 and user123)
        stmt.executeUpdate(
                "INSERT INTO users (id, full_name, email, phone, password_hash, role, status) VALUES " +
                "(1, 'System Administrator', 'admin@travel.com', '9876543210', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'ACTIVE'), " +
                "(2, 'Priya Sharma', 'priya@example.com', '9811223344', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER', 'ACTIVE'), " +
                "(3, 'Rahul Verma', 'rahul@example.com', '9822334455', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER', 'ACTIVE'), " +
                "(4, 'Amit Patel', 'amit@example.com', '9833445566', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER', 'ACTIVE') " +
                "ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);"
        );

        // Destinations
        stmt.executeUpdate(
                "INSERT INTO destinations (id, name, state, description, attractions, best_time) VALUES " +
                "(1, 'Goa', 'Goa', 'Tropical paradise famed for pristine beaches, vibrant nightlife, Portuguese heritage, and seafood delicacies.', 'Baga Beach, Calangute, Fort Aguada, Dudhsagar Falls, Basilica of Bom Jesus', 'November to February'), " +
                "(2, 'Manali', 'Himachal Pradesh', 'Breathtaking high-altitude Himalayan resort town known for snowy peaks, pine forests, and adventure sports.', 'Solang Valley, Rohtang Pass, Hadimba Temple, Old Manali, Jogini Waterfall', 'October to June'), " +
                "(3, 'Jaipur', 'Rajasthan', 'The iconic Pink City celebrated for grand palaces, formidable hill forts, rich royal history, and colorful bazaars.', 'Hawa Mahal, Amer Fort, City Palace, Jantar Mantar, Nahargarh Fort', 'October to March'), " +
                "(4, 'Kashmir', 'Jammu & Kashmir', 'Heaven on Earth blessed with postcard-perfect valleys, shikara boat rides on Dal Lake, and snow-laden peaks.', 'Dal Lake Srinagar, Gulmarg Gondola, Pahalgam Valley, Sonamarg, Mughal Gardens', 'March to October (Summer) & Dec-Feb (Snow)'), " +
                "(5, 'Delhi', 'Delhi NCR', 'The historical and cultural capital of India, blending centuries of Mughal and colonial monuments with modern charm.', 'Red Fort, Qutub Minar, India Gate, Humayun Tomb, Lotus Temple, Chandni Chowk', 'October to March'), " +
                "(6, 'Kerala', 'Kerala', 'Gods Own Country, world-renowned for tranquil backwaters, emerald tea plantations, Ayurveda, and coastal beaches.', 'Alleppey Houseboats, Munnar Tea Gardens, Wayanad Wildlife, Kovalam Beach, Periyar', 'September to March'), " +
                "(7, 'Rishikesh', 'Uttarakhand', 'The Yoga Capital of the World along the holy Ganges, known for white-water rafting, serene ashrams, and suspension bridges.', 'Lakshman Jhula, Triveni Ghat Ganga Aarti, Shivpuri Rafting, Beatles Ashram, Neer Waterfall', 'September to April'), " +
                "(8, 'Udaipur', 'Rajasthan', 'The romantic City of Lakes known for shimmering waters, opulent marble palaces, and sunset boat rides.', 'City Palace, Lake Pichola, Jag Mandir, Saheliyon Ki Bari, Fatehsagar Lake', 'September to March') " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name);"
        );

        // Packages
        stmt.executeUpdate(
                "INSERT INTO packages (id, package_name, destination_id, duration_days, duration_nights, price_per_person, places_covered, hotel_included, food_included, transport_included, description, status) VALUES " +
                "(1, 'Goa Sun, Sand & Carnival Tour', 1, 4, 3, 8999.00, 'North Goa Beaches, Fort Aguada, Panaji Cruise, Old Goa Churches', TRUE, TRUE, TRUE, 'Unwind in sunny Goa with beach parties, water sports, sunset cruises, and rich Portuguese architectural tours.', 'ACTIVE'), " +
                "(2, 'Manali Adventure & Snow Experience', 2, 5, 4, 12999.00, 'Solang Valley, Atal Tunnel, Rohtang Pass, Old Manali, Vashisht Baths', TRUE, TRUE, TRUE, 'High adrenaline adventure featuring paragliding, snow skiing, river rafting, and scenic mountain cafes.', 'ACTIVE'), " +
                "(3, 'Royal Jaipur Heritage & Palaces', 3, 3, 2, 6499.00, 'Amer Fort, Hawa Mahal, City Palace, Chokhi Dhani, Jal Mahal', TRUE, TRUE, TRUE, 'Experience Maharaja hospitality with guided palace tours, traditional Rajasthani dinner, and shopping in historic bazaars.', 'ACTIVE'), " +
                "(4, 'Kashmir Valley Paradise Honeymoon Tour', 4, 6, 5, 19999.00, 'Srinagar Dal Lake, Gulmarg, Pahalgam Betaab Valley, Mughal Gardens', TRUE, TRUE, TRUE, 'Romantic getaway with luxury houseboat stay, shikara rides, snow activities in Gulmarg, and saffron valley excursions.', 'ACTIVE'), " +
                "(5, 'Delhi Historical & Cultural Trail', 5, 2, 1, 3999.00, 'Qutub Minar, India Gate, Red Fort, Humayun Tomb, Akshardham Temple', TRUE, FALSE, TRUE, 'Explore the imperial monuments, vibrant street markets, and modern landmarks of India capital.', 'ACTIVE'), " +
                "(6, 'Kerala Backwaters & Munnar Hills', 6, 5, 4, 15499.00, 'Munnar Tea Plantations, Mattupetty Dam, Alleppey Houseboat, Cochin Fort', TRUE, TRUE, TRUE, 'A blissful blend of misty mountain plantations and an overnight luxury houseboat cruise through tranquil backwater lagoons.', 'ACTIVE'), " +
                "(7, 'Rishikesh Rafting & Yoga Retreat', 7, 3, 2, 5999.00, 'Shivpuri 16km White Water Rafting, Cliff Jumping, Ganga Aarti, Camping', TRUE, TRUE, TRUE, 'Rejuvenate your spirit with riverside luxury camping, exhilarating river rafting, bonfire nights, and meditation sessions.', 'ACTIVE'), " +
                "(8, 'Udaipur Royal Lakes & Sunset Tour', 8, 4, 3, 11499.00, 'City Palace Udaipur, Lake Pichola Boat Ride, Jag Mandir, Sajjangarh Monsoon Palace', TRUE, TRUE, TRUE, 'Savor royal dining, scenic sunset boat tours, cultural folk dances, and grand Rajput fortress heritage.', 'ACTIVE') " +
                "ON DUPLICATE KEY UPDATE package_name=VALUES(package_name);"
        );

        // Hotels
        stmt.executeUpdate(
                "INSERT INTO hotels (id, hotel_name, destination_id, address, room_type, price_per_night, available_rooms, rating, description, status) VALUES " +
                "(1, 'Taj Cidade de Goa Heritage', 1, 'Vainguinim Beach, Panaji, Goa', 'Deluxe Sea View', 4500.00, 15, 4.8, 'Colonial Portuguese luxury resort set right on the golden sands of Vainguinim beach.', 'ACTIVE'), " +
                "(2, 'Goa Palms Beach Resort', 1, 'Calangute Beach Road, North Goa', 'Standard AC', 2200.00, 20, 4.2, 'Vibrant boutique hotel within walking distance from famous beach shacks and night markets.', 'ACTIVE'), " +
                "(3, 'The Himalayan Spa Resort', 2, 'Hadimba Temple Road, Manali', 'Luxury Mountain Suite', 3800.00, 12, 4.7, 'Picturesque stone-and-wood Himalayan lodge with panoramic views of snow-draped peaks.', 'ACTIVE'), " +
                "(4, 'Snow Valley Mountain View Hotel', 2, 'Log Huts Area, Manali', 'Deluxe Room', 2000.00, 18, 4.3, 'Cozy alpine stay offering heated rooms, wooden architecture, and delicious multi-cuisine buffet.', 'ACTIVE'), " +
                "(5, 'ITC Rajputana Palace', 3, 'Palace Road, Gopalbari, Jaipur', 'Royal Executive Suite', 4800.00, 10, 4.9, 'Opulent 5-star palace hotel inspired by traditional Rajasthani royal courtyards and havelis.', 'ACTIVE'), " +
                "(6, 'Hotel Pearl Palace Heritage', 3, 'Hathroi Fort, Ajmer Road, Jaipur', 'Deluxe Heritage', 1800.00, 16, 4.4, 'Award-winning boutique heritage hotel featuring ornate fresco paintings and a famous rooftop cafe.', 'ACTIVE'), " +
                "(7, 'Wangnoo Luxury Houseboats', 4, 'Dal Lake Ghat 12, Srinagar, Kashmir', 'Royal Cedar Suite', 3500.00, 8, 4.8, 'Hand-carved cedar wood houseboat floating on Dal Lake with personalized butler and shikara service.', 'ACTIVE'), " +
                "(8, 'Lake Song Backwater Resort', 6, 'Vembanad Lake, Kumarakom, Kerala', 'Cottage by Lake', 3400.00, 12, 4.6, 'Traditional Kerala architecture nestled beside Vembanad Lake offering authentic Ayurvedic spa.', 'ACTIVE') " +
                "ON DUPLICATE KEY UPDATE hotel_name=VALUES(hotel_name);"
        );
    }
}
