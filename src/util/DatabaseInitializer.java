package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automatically initializes database schema, tables, safe migrations,
 * and sample records if absent or updated.
 */
public class DatabaseInitializer {

    public static void main(String[] args) {
        System.out.println("Starting Database Initialization...");
        initializeDatabase();
        System.out.println("Finished Database Initialization.");
    }

    private static volatile boolean initialized = false;
    private static volatile boolean initializing = false;

    public static boolean isInitialized() {
        return initialized;
    }

    public static synchronized void ensureInitialized() {
        if (!initialized && !initializing) {
            initializeDatabase();
        }
    }

    public static synchronized void initializeDatabase() {
        if (initialized || initializing) return;
        initializing = true;
        try {
            int maxAttempts = 5;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    // 1. Create database if it does not exist (harmless on pre-allocated cloud DB)
                    try (Connection conn = DatabaseConnection.getServerConnection();
                         Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS travel_booking_system;");
                    } catch (Exception ignored) {
                        // Harmless in cloud environments where database is pre-allocated
                    }

                    // 2. Connect to database and ensure all tables & columns exist
                    try (Connection conn = DatabaseConnection.getConnectionDirect();
                         Statement stmt = conn.createStatement()) {

                        createTables(stmt);
                        runMigrations(stmt);
                        seedInitialData(stmt);

                        initialized = true;
                        System.out.println("Database schema, tables, and seed data verified successfully!");
                        return;
                    }
                } catch (SQLException e) {
                    System.err.println("Database initialization attempt " + attempt + " of " + maxAttempts + " notice: " + e.getMessage());
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        } finally {
            initializing = false;
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        // 1. Users
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    full_name VARCHAR(100) NOT NULL," +
                "    email VARCHAR(100) NOT NULL UNIQUE," +
                "    phone VARCHAR(20) NOT NULL," +
                "    password_hash VARCHAR(256) NOT NULL," +
                "    role VARCHAR(20) DEFAULT 'TRAVELER'," +
                "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 2. Destinations
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS destinations (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    name VARCHAR(100) NOT NULL UNIQUE," +
                "    country VARCHAR(100) NOT NULL DEFAULT 'India'," +
                "    state VARCHAR(100) NOT NULL," +
                "    description TEXT NOT NULL," +
                "    attractions TEXT NOT NULL," +
                "    best_time VARCHAR(100) NOT NULL," +
                "    image_url VARCHAR(255) DEFAULT ''," +
                "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 3. Flights
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS flights (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    agent_id INT NOT NULL," +
                "    airline VARCHAR(100) NOT NULL," +
                "    flight_number VARCHAR(50) NOT NULL," +
                "    origin VARCHAR(100) NOT NULL," +
                "    destination VARCHAR(100) NOT NULL," +
                "    departure_date DATE NOT NULL," +
                "    departure_time VARCHAR(20) NOT NULL," +
                "    arrival_time VARCHAR(20) NOT NULL," +
                "    price DECIMAL(10, 2) NOT NULL," +
                "    available_seats INT NOT NULL DEFAULT 60," +
                "    image_url VARCHAR(255) DEFAULT ''," +
                "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "    approval_status VARCHAR(20) DEFAULT 'PENDING'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 4. Hotels
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS hotels (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    agent_id INT NOT NULL DEFAULT 1," +
                "    hotel_name VARCHAR(150) NOT NULL," +
                "    destination_id INT NOT NULL," +
                "    address VARCHAR(255) NOT NULL," +
                "    location VARCHAR(150) NOT NULL DEFAULT ''," +
                "    room_type VARCHAR(50) NOT NULL DEFAULT 'Deluxe'," +
                "    price_per_night DECIMAL(10, 2) NOT NULL," +
                "    available_rooms INT NOT NULL DEFAULT 10," +
                "    rating DECIMAL(2, 1) NOT NULL DEFAULT 4.5," +
                "    description TEXT," +
                "    image_url VARCHAR(255) DEFAULT ''," +
                "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "    approval_status VARCHAR(20) DEFAULT 'APPROVED'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 5. Rental Cars
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS cars (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    agent_id INT NOT NULL," +
                "    car_name VARCHAR(100) NOT NULL," +
                "    brand VARCHAR(100) NOT NULL," +
                "    model VARCHAR(100) NOT NULL," +
                "    location VARCHAR(100) NOT NULL," +
                "    car_type VARCHAR(50) NOT NULL DEFAULT 'Sedan'," +
                "    price_per_day DECIMAL(10, 2) NOT NULL," +
                "    available_units INT NOT NULL DEFAULT 5," +
                "    image_url VARCHAR(255) DEFAULT ''," +
                "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "    approval_status VARCHAR(20) DEFAULT 'PENDING'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 6. Packages
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS packages (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    agent_id INT NOT NULL DEFAULT 1," +
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
                "    image_url VARCHAR(255) DEFAULT ''," +
                "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "    approval_status VARCHAR(20) DEFAULT 'APPROVED'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 7. Bookings
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    booking_code VARCHAR(50) NOT NULL UNIQUE," +
                "    user_id INT NOT NULL," +
                "    booking_type VARCHAR(20) DEFAULT 'PACKAGE'," +
                "    item_id INT NULL," +
                "    item_name VARCHAR(150) NULL," +
                "    flight_id INT NULL," +
                "    hotel_id INT NULL," +
                "    car_id INT NULL," +
                "    package_id INT NULL," +
                "    agent_id INT NULL," +
                "    travel_date DATE NOT NULL," +
                "    start_date DATE NULL," +
                "    end_date DATE NULL," +
                "    persons INT NOT NULL DEFAULT 1," +
                "    quantity INT NOT NULL DEFAULT 1," +
                "    package_cost DECIMAL(10, 2) DEFAULT 0.00," +
                "    hotel_cost DECIMAL(10, 2) DEFAULT 0.00," +
                "    total_amount DECIMAL(10, 2) NOT NULL," +
                "    special_requests TEXT," +
                "    booking_status VARCHAR(20) DEFAULT 'CONFIRMED'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 8. Payments
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    transaction_code VARCHAR(50) NOT NULL UNIQUE," +
                "    booking_id INT NOT NULL," +
                "    user_id INT NOT NULL," +
                "    amount DECIMAL(10, 2) NOT NULL," +
                "    payment_method VARCHAR(20) NOT NULL," +
                "    payment_details VARCHAR(255)," +
                "    payment_status VARCHAR(20) DEFAULT 'SUCCESS'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 9. Messages
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS messages (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    user_id INT NOT NULL," +
                "    agent_id INT NULL," +
                "    subject VARCHAR(150) NOT NULL," +
                "    message TEXT NOT NULL," +
                "    reply TEXT NULL," +
                "    status VARCHAR(20) DEFAULT 'OPEN'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    replied_at TIMESTAMP NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );

        // 10. System Settings
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS system_settings (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    setting_key VARCHAR(50) NOT NULL UNIQUE," +
                "    setting_value TEXT NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );
    }

    private static void runMigrations(Statement stmt) {
        // Safe column additions for pre-existing tables
        safeExecute(stmt, "ALTER TABLE users MODIFY COLUMN role VARCHAR(20) DEFAULT 'TRAVELER'");
        safeExecute(stmt, "ALTER TABLE users MODIFY COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'");

        safeExecute(stmt, "ALTER TABLE destinations ADD COLUMN country VARCHAR(100) NOT NULL DEFAULT 'India'");
        safeExecute(stmt, "ALTER TABLE destinations ADD COLUMN image_url VARCHAR(255) DEFAULT ''");
        safeExecute(stmt, "ALTER TABLE destinations ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'");

        safeExecute(stmt, "ALTER TABLE hotels ADD COLUMN agent_id INT NOT NULL DEFAULT 1");
        safeExecute(stmt, "ALTER TABLE hotels ADD COLUMN location VARCHAR(150) NOT NULL DEFAULT ''");
        safeExecute(stmt, "ALTER TABLE hotels ADD COLUMN image_url VARCHAR(255) DEFAULT ''");
        safeExecute(stmt, "ALTER TABLE hotels ADD COLUMN approval_status VARCHAR(20) DEFAULT 'APPROVED'");

        safeExecute(stmt, "ALTER TABLE packages ADD COLUMN agent_id INT NOT NULL DEFAULT 1");
        safeExecute(stmt, "ALTER TABLE packages ADD COLUMN image_url VARCHAR(255) DEFAULT ''");
        safeExecute(stmt, "ALTER TABLE packages ADD COLUMN approval_status VARCHAR(20) DEFAULT 'APPROVED'");

        safeExecute(stmt, "ALTER TABLE bookings MODIFY COLUMN package_id INT NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN booking_type VARCHAR(20) DEFAULT 'PACKAGE'");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN item_id INT NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN item_name VARCHAR(150) NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN flight_id INT NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN car_id INT NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN agent_id INT NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN start_date DATE NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN end_date DATE NULL");
        safeExecute(stmt, "ALTER TABLE bookings ADD COLUMN quantity INT NOT NULL DEFAULT 1");
        safeExecute(stmt, "ALTER TABLE bookings MODIFY COLUMN booking_status VARCHAR(20) DEFAULT 'CONFIRMED'");
    }

    private static void safeExecute(Statement stmt, String sql) {
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException ignored) {
            // Already applied or column exists
        }
    }

    private static void seedInitialData(Statement stmt) throws SQLException {
        // Users (Keyed by unique email)
        stmt.executeUpdate(
                "INSERT INTO users (full_name, email, phone, password_hash, role, status) VALUES " +
                "('System Administrator', 'admin@example.com', '9876543210', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'ACTIVE'), " +
                "('Skyline Travels Agency', 'agent@example.com', '9876543211', 'f44d1ac9bf0c69b083380b86dbdf3b73797150e3cca4820ac399f7917e607647', 'AGENT', 'ACTIVE'), " +
                "('John Traveler', 'traveler@example.com', '9876543212', 'f5b488373cfef0c326c073377d5dd2f325f7fbd9c5a21b20e8b234d35f1652b3', 'TRAVELER', 'ACTIVE'), " +
                "('System Administrator (Legacy)', 'admin@travel.com', '9876543200', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'ACTIVE'), " +
                "('Priya Sharma', 'priya@example.com', '9811223344', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'TRAVELER', 'ACTIVE') " +
                "ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), role=VALUES(role), password_hash=VALUES(password_hash), phone=VALUES(phone), status=VALUES(status);"
        );

        // Destinations
        stmt.executeUpdate(
                "INSERT INTO destinations (id, name, country, state, description, attractions, best_time, image_url, status) VALUES " +
                "(1, 'Goa', 'India', 'Goa', 'Tropical paradise famed for pristine beaches, vibrant nightlife, Portuguese heritage, and seafood delicacies.', 'Baga Beach, Calangute, Fort Aguada, Dudhsagar Falls, Basilica of Bom Jesus', 'November to February', 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=600&auto=format&fit=crop', 'ACTIVE'), " +
                "(2, 'Manali', 'India', 'Himachal Pradesh', 'Breathtaking high-altitude Himalayan resort town known for snowy peaks, pine forests, and adventure sports.', 'Solang Valley, Rohtang Pass, Hadimba Temple, Old Manali, Jogini Waterfall', 'October to June', 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?w=600&auto=format&fit=crop', 'ACTIVE'), " +
                "(3, 'Jaipur', 'India', 'Rajasthan', 'The iconic Pink City celebrated for grand palaces, formidable hill forts, rich royal history, and colorful bazaars.', 'Hawa Mahal, Amer Fort, City Palace, Jantar Mantar, Nahargarh Fort', 'October to March', 'https://images.unsplash.com/photo-1477587458883-47145ed94245?w=600&auto=format&fit=crop', 'ACTIVE'), " +
                "(4, 'Kashmir', 'India', 'Jammu & Kashmir', 'Heaven on Earth blessed with postcard-perfect valleys, shikara boat rides on Dal Lake, and snow-laden peaks.', 'Dal Lake Srinagar, Gulmarg Gondola, Pahalgam Valley, Sonamarg, Mughal Gardens', 'March to October (Summer) & Dec-Feb (Snow)', 'https://images.unsplash.com/photo-1595846519845-68e298c2edd8?w=600&auto=format&fit=crop', 'ACTIVE'), " +
                "(5, 'Delhi', 'India', 'Delhi NCR', 'The historical and cultural capital of India, blending centuries of Mughal and colonial monuments with modern charm.', 'Red Fort, Qutub Minar, India Gate, Humayun Tomb, Lotus Temple, Chandni Chowk', 'October to March', 'https://images.unsplash.com/photo-1587474260584-136574528ed5?w=600&auto=format&fit=crop', 'ACTIVE'), " +
                "(6, 'Kerala', 'India', 'Kerala', 'Gods Own Country, world-renowned for tranquil backwaters, emerald tea plantations, Ayurveda, and coastal beaches.', 'Alleppey Houseboats, Munnar Tea Gardens, Wayanad Wildlife, Kovalam Beach, Periyar', 'September to March', 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?w=600&auto=format&fit=crop', 'ACTIVE'), " +
                "(7, 'Rishikesh', 'India', 'Uttarakhand', 'The Yoga Capital of the World along the holy Ganges, known for white-water rafting, serene ashrams, and suspension bridges.', 'Lakshman Jhula, Triveni Ghat Ganga Aarti, Shivpuri Rafting, Beatles Ashram, Neer Waterfall', 'September to April', 'https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600&auto=format&fit=crop', 'ACTIVE'), " +
                "(8, 'Udaipur', 'India', 'Rajasthan', 'The romantic City of Lakes known for shimmering waters, opulent marble palaces, and sunset boat rides.', 'City Palace, Lake Pichola, Jag Mandir, Saheliyon Ki Bari, Fatehsagar Lake', 'September to March', 'https://images.unsplash.com/photo-1566837945700-30057527ade0?w=600&auto=format&fit=crop', 'ACTIVE') " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name);"
        );

        // Flights
        stmt.executeUpdate(
                "INSERT INTO flights (id, agent_id, airline, flight_number, origin, destination, departure_date, departure_time, arrival_time, price, available_seats, status, approval_status) VALUES " +
                "(1, 2, 'IndiGo', '6E-2041', 'Delhi', 'Mumbai', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '06:00 AM', '08:15 AM', 4500.00, 52, 'ACTIVE', 'APPROVED'), " +
                "(2, 2, 'Air India', 'AI-805', 'Delhi', 'Goa', DATE_ADD(CURDATE(), INTERVAL 7 DAY), '10:30 AM', '01:10 PM', 5800.00, 45, 'ACTIVE', 'APPROVED'), " +
                "(3, 2, 'Vistara', 'UK-993', 'Mumbai', 'Delhi', DATE_ADD(CURDATE(), INTERVAL 6 DAY), '05:45 PM', '08:00 PM', 4900.00, 38, 'ACTIVE', 'APPROVED'), " +
                "(4, 2, 'SpiceJet', 'SG-102', 'Delhi', 'Jaipur', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '07:15 AM', '08:15 AM', 2800.00, 50, 'ACTIVE', 'APPROVED'), " +
                "(5, 2, 'IndiGo', '6E-554', 'Bengaluru', 'Goa', DATE_ADD(CURDATE(), INTERVAL 8 DAY), '02:00 PM', '03:15 PM', 3400.00, 40, 'ACTIVE', 'APPROVED'), " +
                "(6, 2, 'Air India Express', 'IX-114', 'Delhi', 'Kashmir', DATE_ADD(CURDATE(), INTERVAL 10 DAY), '09:00 AM', '10:35 AM', 6200.00, 30, 'ACTIVE', 'APPROVED'), " +
                "(7, 2, 'Akasa Air', 'QP-1302', 'Mumbai', 'Bengaluru', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '08:00 AM', '09:40 AM', 3900.00, 55, 'ACTIVE', 'APPROVED'), " +
                "(8, 2, 'Air India', 'AI-442', 'Delhi', 'Kerala', DATE_ADD(CURDATE(), INTERVAL 12 DAY), '11:00 AM', '02:15 PM', 6900.00, 25, 'ACTIVE', 'PENDING') " +
                "ON DUPLICATE KEY UPDATE flight_number=VALUES(flight_number);"
        );

        // Hotels
        stmt.executeUpdate(
                "INSERT INTO hotels (id, agent_id, hotel_name, destination_id, address, location, room_type, price_per_night, available_rooms, rating, image_url, description, status, approval_status) VALUES " +
                "(1, 2, 'Taj Cidade de Goa Heritage', 1, 'Vainguinim Beach, Panaji, Goa', 'North Goa', 'Deluxe Sea View', 4500.00, 15, 4.8, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&auto=format&fit=crop', 'Colonial Portuguese luxury resort set right on the golden sands of Vainguinim beach.', 'ACTIVE', 'APPROVED'), " +
                "(2, 2, 'Goa Palms Beach Resort', 1, 'Calangute Beach Road, North Goa', 'Calangute', 'Standard AC', 2200.00, 20, 4.2, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=600&auto=format&fit=crop', 'Vibrant boutique hotel within walking distance from famous beach shacks and night markets.', 'ACTIVE', 'APPROVED'), " +
                "(3, 2, 'The Himalayan Spa Resort', 2, 'Hadimba Temple Road, Manali', 'Old Manali', 'Luxury Mountain Suite', 3800.00, 12, 4.7, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=600&auto=format&fit=crop', 'Picturesque stone-and-wood Himalayan lodge with panoramic views of snow-draped peaks.', 'ACTIVE', 'APPROVED'), " +
                "(4, 2, 'Snow Valley Mountain View Hotel', 2, 'Log Huts Area, Manali', 'Log Huts', 'Deluxe Room', 2000.00, 18, 4.3, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=600&auto=format&fit=crop', 'Cozy alpine stay offering heated rooms, wooden architecture, and delicious multi-cuisine buffet.', 'ACTIVE', 'APPROVED'), " +
                "(5, 2, 'ITC Rajputana Palace', 3, 'Palace Road, Gopalbari, Jaipur', 'Jaipur Central', 'Royal Executive Suite', 4800.00, 10, 4.9, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=600&auto=format&fit=crop', 'Opulent 5-star palace hotel inspired by traditional Rajasthani royal courtyards and havelis.', 'ACTIVE', 'APPROVED'), " +
                "(6, 2, 'Hotel Pearl Palace Heritage', 3, 'Hathroi Fort, Ajmer Road, Jaipur', 'Ajmer Road', 'Deluxe Heritage', 1800.00, 16, 4.4, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=600&auto=format&fit=crop', 'Award-winning boutique heritage hotel featuring ornate fresco paintings and a famous rooftop cafe.', 'ACTIVE', 'APPROVED'), " +
                "(7, 2, 'Wangnoo Luxury Houseboats', 4, 'Dal Lake Ghat 12, Srinagar, Kashmir', 'Dal Lake', 'Royal Cedar Suite', 3500.00, 8, 4.8, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=600&auto=format&fit=crop', 'Hand-carved cedar wood houseboat floating on Dal Lake with personalized butler and shikara service.', 'ACTIVE', 'APPROVED'), " +
                "(8, 2, 'The Lalit Grand Palace Srinagar', 4, 'Gupkar Road, Srinagar, Kashmir', 'Gupkar Road', 'Palace View Room', 5500.00, 10, 4.9, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600&auto=format&fit=crop', 'Historic royal palace established by Maharaja Pratap Singh overlooking the serene Dal Lake.', 'ACTIVE', 'APPROVED'), " +
                "(9, 2, 'The Imperial New Delhi', 5, 'Janpath, Connaught Place, New Delhi', 'Connaught Place', 'Heritage Suite', 5000.00, 14, 4.8, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=600&auto=format&fit=crop', 'Iconic British Raj era art-deco luxury hotel located at the center of the capital.', 'ACTIVE', 'APPROVED'), " +
                "(10, 2, 'Lake Song Backwater Resort', 6, 'Vembanad Lake, Kumarakom, Kerala', 'Kumarakom', 'Cottage by Lake', 3400.00, 12, 4.6, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=600&auto=format&fit=crop', 'Traditional Kerala architecture nestled beside Vembanad Lake offering authentic Ayurvedic spa.', 'ACTIVE', 'APPROVED') " +
                "ON DUPLICATE KEY UPDATE hotel_name=VALUES(hotel_name);"
        );

        // Cars
        stmt.executeUpdate(
                "INSERT INTO cars (id, agent_id, car_name, brand, model, location, car_type, price_per_day, available_units, image_url, status, approval_status) VALUES " +
                "(1, 2, 'Hyundai Creta SX', 'Hyundai', 'Creta 2024', 'Delhi', 'SUV', 2800.00, 5, 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(2, 2, 'Toyota Innova Crysta', 'Toyota', 'Innova Crysta 2.4G', 'Goa', 'SUV', 3500.00, 4, 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(3, 2, 'Honda City ZX', 'Honda', 'City 5th Gen', 'Mumbai', 'Sedan', 2400.00, 6, 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(4, 2, 'Maruti Suzuki Swift', 'Maruti Suzuki', 'Swift ZXi', 'Jaipur', 'Hatchback', 1600.00, 8, 'https://images.unsplash.com/photo-1502877338535-766e1452684a?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(5, 2, 'Mahindra Thar 4x4', 'Mahindra', 'Thar LX Hard Top', 'Manali', 'SUV', 3800.00, 3, 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(6, 2, 'Mercedes-Benz C-Class', 'Mercedes-Benz', 'C200', 'Delhi', 'Luxury', 8500.00, 2, 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(7, 2, 'Tata Nexon EV', 'Tata', 'Nexon EV Long Range', 'Bengaluru', 'SUV', 2500.00, 4, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(8, 2, 'Kia Seltos GTX+', 'Kia', 'Seltos 2024', 'Kerala', 'SUV', 3000.00, 3, 'https://images.unsplash.com/photo-1502877338535-766e1452684a?w=600&auto=format&fit=crop', 'ACTIVE', 'PENDING') " +
                "ON DUPLICATE KEY UPDATE car_name=VALUES(car_name);"
        );

        // Packages
        stmt.executeUpdate(
                "INSERT INTO packages (id, agent_id, package_name, destination_id, duration_days, duration_nights, price_per_person, places_covered, hotel_included, food_included, transport_included, description, image_url, status, approval_status) VALUES " +
                "(1, 2, 'Goa Sun, Sand & Carnival Tour', 1, 4, 3, 8999.00, 'North Goa Beaches, Fort Aguada, Panaji Cruise, Old Goa Churches', TRUE, TRUE, TRUE, 'Unwind in sunny Goa with beach parties, water sports, sunset cruises, and rich Portuguese architectural tours.', 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(2, 2, 'Manali Adventure & Snow Experience', 2, 5, 4, 12999.00, 'Solang Valley, Atal Tunnel, Rohtang Pass, Old Manali, Vashisht Baths', TRUE, TRUE, TRUE, 'High adrenaline adventure featuring paragliding, snow skiing, river rafting, and scenic mountain cafes.', 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(3, 2, 'Royal Jaipur Heritage & Palaces', 3, 3, 2, 6499.00, 'Amer Fort, Hawa Mahal, City Palace, Chokhi Dhani, Jal Mahal', TRUE, TRUE, TRUE, 'Experience Maharaja hospitality with guided palace tours, traditional Rajasthani dinner, and shopping in historic bazaars.', 'https://images.unsplash.com/photo-1477587458883-47145ed94245?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(4, 2, 'Kashmir Valley Paradise Honeymoon Tour', 4, 6, 5, 19999.00, 'Srinagar Dal Lake, Gulmarg, Pahalgam Betaab Valley, Mughal Gardens', TRUE, TRUE, TRUE, 'Romantic getaway with luxury houseboat stay, shikara rides, snow activities in Gulmarg, and saffron valley excursions.', 'https://images.unsplash.com/photo-1595846519845-68e298c2edd8?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(5, 2, 'Delhi Historical & Cultural Trail', 5, 2, 1, 3999.00, 'Qutub Minar, India Gate, Red Fort, Humayun Tomb, Akshardham Temple', TRUE, FALSE, TRUE, 'Explore the imperial monuments, vibrant street markets, and modern landmarks of India capital.', 'https://images.unsplash.com/photo-1587474260584-136574528ed5?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED'), " +
                "(6, 2, 'Kerala Backwaters & Munnar Hills', 6, 5, 4, 15499.00, 'Munnar Tea Plantations, Mattupetty Dam, Alleppey Houseboat, Cochin Fort', TRUE, TRUE, TRUE, 'A blissful blend of misty mountain plantations and an overnight luxury houseboat cruise through tranquil backwater lagoons.', 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?w=600&auto=format&fit=crop', 'ACTIVE', 'APPROVED') " +
                "ON DUPLICATE KEY UPDATE package_name=VALUES(package_name);"
        );

        // System Settings
        stmt.executeUpdate(
                "INSERT INTO system_settings (setting_key, setting_value) VALUES " +
                "('site_name', 'VoyageQuest - Online Travel Booking Platform'), " +
                "('contact_email', 'support@travelbooking.com'), " +
                "('support_phone', '+91 98765 43210'), " +
                "('booking_enabled', 'true'), " +
                "('maintenance_mode', 'false'), " +
                "('default_currency', 'INR (₹)') " +
                "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);"
        );
    }
}
