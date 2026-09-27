# ✈ VoyageQuest - Online Travel Booking Platform

> **Comprehensive Full-Stack Java Web Application & College Project**  
> Built with **Core Java JDK (Standard `HttpServer`)**, **JDBC**, **MySQL**, and a **Modern Responsive Multi-Page Web Frontend** (with dual Desktop Swing UI support).  
> Designed specifically for B.Tech / BCA / MCA Computer Science students with an 18-step live viva demonstration workflow and full defense documentation.

---

## 📌 Table of Contents
1. [Project Overview & Objectives](#-project-overview--objectives)
2. [Three-Tier Architecture & System Design](#-three-tier-architecture--system-design)
3. [User Roles & Permissions Matrix](#-user-roles--permissions-matrix)
4. [Four Core Travel Services](#-four-core-travel-services)
5. [Universal Booking Engine & Fare Calculation](#-universal-booking-engine--fare-calculation)
6. [Unified Chronological Travel Itinerary](#-unified-chronological-travel-itinerary)
7. [Customer Support & Agent Messaging Inbox](#-customer-support--agent-messaging-inbox)
8. [Database Schema & ER Relationships](#-database-schema--er-relationships)
9. [Pre-Seeded Demo Credentials](#-pre-seeded-demo-credentials)
10. [REST API Documentation](#-rest-api-documentation)
11. [Quick-Start Setup & Installation](#-quick-start-setup--installation)
12. [Docker & Cloud Deployment (Render/Railway)](#-docker--cloud-deployment)
13. [18-Step Live Viva Demonstration Script](#-18-step-live-viva-demonstration-script)
14. [Viva Voce Technical Question Bank (CSE Defense)](#-viva-voce-technical-question-bank)

---

## 🌟 Project Overview & Objectives

**VoyageQuest** is an end-to-end travel booking platform that bridges travelers, independent travel agencies, and system administrators into a single unified ecosystem. 

Traditional college projects often present static HTML mockups or rely on bulky frameworks where students cannot explain the underlying mechanisms. VoyageQuest is engineered with **zero external framework dependencies** (pure standard JDK 17+ and MySQL Connector/J), demonstrating pure mastery of:
- **Object-Oriented Programming (OOP)**: Encapsulation, Inheritance, Polymorphism, Abstraction.
- **MVC & Clean DAO Design**: Complete decoupling of presentation, business rules, and SQL persistence.
- **RESTful API Engineering**: High-throughput non-blocking HTTP endpoints with JSON serialization.
- **Relational Data Integrity**: Foreign keys, ACID transactions, atomic inventory decrements, and soft cancellations.
- **Web Security**: Strict SHA-256 password hashing with salt, SQL injection immunity via `PreparedStatement`, and role authorization guards.

---

## 🏛 Three-Tier Architecture & System Design

```
+---------------------------------------------------------------------------------+
|                               PRESENTATION LAYER                                |
|  - Web Frontend: HTML5, CSS3 Variables, ES6 JavaScript, FontAwesome Icons       |
|  - Responsive Pages: index, flights, hotels, cars, packages, booking,           |
|                      my-bookings, itinerary, traveler/agent/admin dashboards    |
|  - Desktop Client: Java Swing UI with FlatLaf Theme (src/view/*.java)           |
+---------------------------------------+-----------------------------------------+
                                        | HTTP / JSON (REST APIs)
+---------------------------------------v-----------------------------------------+
|                                BUSINESS LOGIC LAYER                             |
|  - WebServer (com.sun.net.httpserver.HttpServer)                                |
|  - AuthService: SHA-256 Hashing, Session Validation, Registration Guard         |
|  - BookingService: Universal Calculations, Inventory Lock, Auto-Itinerary       |
|  - AgentService: Partner Inventory Submissions, Listing Moderation Hooks        |
|  - AdminService: 8 Platform KPIs, Global User Moderation, Financial Audits      |
|  - MessageService: Bidirectional Inquiries & Feedback Dispatcher                |
+---------------------------------------+-----------------------------------------+
                                        | Java Database Connectivity (JDBC)
+---------------------------------------v-----------------------------------------+
|                                DATA ACCESS LAYER                                |
|  - Data Access Objects (DAOs): UserDAO, FlightDAO, HotelDAO, CarDAO,           |
|                               PackageDAO, BookingDAO, MessageDAO, SettingsDAO   |
|  - DatabaseInitializer: Idempotent Schema Migrations & Automated Seed Engine    |
|  - DatabaseConnection: Thread-Safe Singleton Connection Pool                    |
+---------------------------------------+-----------------------------------------+
                                        | SQL Queries
+---------------------------------------v-----------------------------------------+
|                           PERSISTENCE LAYER (MySQL 8.0)                         |
|  Tables: users, destinations, flights, hotels, cars, packages,                  |
|          bookings, payments, messages, system_settings                          |
+---------------------------------------------------------------------------------+
```

---

## 👥 User Roles & Permissions Matrix

The platform implements strict role-based access control (RBAC):

| Capability / Feature | Traveler (`TRAVELER`) | Travel Agent (`AGENT`) | Administrator (`ADMIN`) |
| :--- | :---: | :---: | :---: |
| **Self-Registration** | Yes (via Register page) | Yes (via Register page) | **Strictly Forbidden** (Pre-seeded only) |
| **Search Flights, Hotels, Cars, Packages** | Yes | Yes | Yes |
| **Universal Checkout & Instant Booking** | Yes | Yes | Yes |
| **View Personal Bookings & Print Receipts** | Yes | Yes | Yes |
| **Interactive Chronological Itinerary** | Yes | Yes | Yes |
| **Cancel Personal Bookings (Restores Stock)** | Yes | No (Customer owned) | Yes (Admin Override) |
| **Submit New Inventory (Flights/Hotels/Cars/Packages)**| No | Yes (Starts as `PENDING`) | Yes (Direct / Agent behalf) |
| **View Own Inventory & Moderation Status** | No | Yes | Yes (All Listings) |
| **Approve / Reject Agent Listings** | No | No | Yes |
| **Reply to Customer Inquiries** | No (Can send inquiries) | Yes (Assigned inquiries) | Yes (Platform inquiries) |
| **Moderate Users (`ACTIVE` $\leftrightarrow$ `DISABLED`)** | No | No | Yes |
| **Platform 8 KPI Metrics & Financial Ledger** | No | No | Yes |
| **Configure System Settings (Tax, Currency, Site)** | No | No | Yes |

---

## ✈ Four Core Travel Services

1. **Domestic & International Flights (`flights.html` / `FlightDAO`)**:
   - Filter by Origin, Destination, and Travel Date.
   - Airline branding, flight numbers, departure/arrival schedules, and live seat counters.
2. **Hotels & Resorts (`hotels.html` / `HotelDAO`)**:
   - Location filtering, star ratings, room types (Deluxe, Suite, Executive), amenities, and available room counts.
3. **Car Rentals (`cars.html` / `CarDAO`)**:
   - Vehicle categories (SUV, Sedan, Hatchback, Luxury), transmission types, daily rental rates, and fleet counts.
4. **All-Inclusive Tour Packages (`packages.html` / `PackageDAO`)**:
   - Multi-day sightseeing packages, included meals/lodging/transport, and per-person pricing.

---

## 💳 Universal Booking Engine & Fare Calculation

The platform implements a unified checkout system (`booking.html` $\rightarrow$ `/api/bookings`) that handles standalone reservations as well as bundled packages:

- **Flight Bookings**:
  $$\text{Total Fare} = \text{Price Per Seat} \times \text{Number of Passengers}$$
  *Action:* Automatically validates seat inventory and decrements `available_seats`.

- **Hotel Bookings**:
  $$\text{Nights} = \text{Check-Out Date} - \text{Check-In Date}$$
  $$\text{Total Fare} = \text{Price Per Night} \times \text{Rooms Reserved} \times \text{Nights}$$
  *Action:* Decrements `available_rooms`.

- **Car Rentals**:
  $$\text{Rental Days} = \text{Drop-Off Date} - \text{Pick-Up Date}$$
  $$\text{Total Fare} = \text{Daily Rate} \times \text{Vehicles} \times \text{Rental Days}$$
  *Action:* Decrements `available_units`.

- **Tour Packages**:
  $$\text{Total Fare} = (\text{Price Per Person} \times \text{Travelers}) + \text{Optional Hotel Addon}$$
  *Action:* Decrements package capacity and optional hotel rooms.

### Cancellation & Stock Restoration
When a user or admin cancels a booking (`/api/bookings/cancel`), the database executes an atomic transaction that:
1. Marks `booking_status = 'CANCELLED'`.
2. Marks `payment_status = 'REFUNDED'`.
3. Restores reserved units/seats back into the corresponding inventory table.

---

## 📅 Unified Chronological Travel Itinerary

Located at `itinerary.html` (`/api/itinerary?userId=...`):
- Consolidates all of a traveler's confirmed flight bookings, hotel stays, and car rentals into a single master timeline.
- Automatically sorts legs chronologically by date.
- Displays summary trip statistics: Total trip segments, flight legs, hotel nights, car rentals, and total budget invested.
- Formatted with print styling for digital travel passes (`window.print()`).

---

## 💬 Customer Support & Agent Messaging Inbox

1. **Traveler Inquiries**:
   - Travelers can dispatch questions regarding check-in times, luggage allowances, or seat preferences to their assigned Travel Agent or General Support (`POST /api/messages`).
2. **Travel Agent Inbox**:
   - Travel Agents review customer inquiries directly on their dashboard (`agent-dashboard.html`).
   - Clicking **Reply** records the agent's response (`POST /api/messages/reply`), instantly updating the traveler's view.

---

## 🗄 Database Schema & ER Relationships

The database `travel_booking_system` consists of 10 normalized tables:

1. **`users`**: `id`, `full_name`, `email` (UNIQUE), `password_hash`, `phone`, `role` (`ADMIN`/`AGENT`/`TRAVELER`), `status` (`ACTIVE`/`DISABLED`), `created_at`.
2. **`destinations`**: `id`, `name`, `state`, `country`, `description`, `image_url`, `best_season`, `is_active`.
3. **`flights`**: `id`, `agent_id`, `airline`, `flight_number`, `origin`, `destination`, `departure_date`, `departure_time`, `arrival_time`, `price`, `available_seats`, `status`, `approval_status`.
4. **`hotels`**: `id`, `agent_id`, `destination_id`, `hotel_name`, `location`, `star_rating`, `price_per_night`, `available_rooms`, `room_type`, `amenities`, `status`, `approval_status`.
5. **`cars`**: `id`, `agent_id`, `destination_id`, `car_name`, `brand`, `car_type`, `location`, `price_per_day`, `available_units`, `transmission`, `fuel_type`, `status`, `approval_status`.
6. **`packages`**: `id`, `agent_id`, `destination_id`, `package_name`, `duration_days`, `duration_nights`, `price_per_person`, `places_covered`, `hotel_included`, `food_included`, `transport_included`, `description`, `status`, `approval_status`.
7. **`bookings`**: `id`, `booking_code` (UNIQUE), `user_id`, `booking_type` (`FLIGHT`/`HOTEL`/`CAR`/`PACKAGE`), `flight_id`, `hotel_id`, `car_id`, `package_id`, `agent_id`, `travel_date`, `start_date`, `end_date`, `persons`, `quantity`, `total_amount`, `booking_status`, `payment_status`, `special_requests`, `created_at`.
8. **`payments`**: `id`, `booking_id`, `transaction_code` (UNIQUE), `amount`, `payment_method` (`CARD`/`UPI`/`NETBANKING`/`CASH`), `payment_details`, `payment_status`, `payment_date`.
9. **`messages`**: `id`, `user_id`, `agent_id`, `booking_id`, `subject`, `message`, `reply`, `status`, `created_at`, `replied_at`.
10. **`system_settings`**: `id`, `setting_key` (UNIQUE), `setting_value`, `description`, `updated_at`.

---

## 🔑 Pre-Seeded Demo Credentials

All passwords utilize SHA-256 cryptography with automated fallback verification:

| Role | Account Name | Email / Username | Password | Default Landing Page |
| :--- | :--- | :--- | :--- | :--- |
| 🛡 **Admin** | System Administrator | `admin@example.com` | `admin123` | `admin-dashboard.html` |
| 💼 **Travel Agent** | Skyline Travels Agency | `agent@example.com` | `agent123` | `agent-dashboard.html` |
| 👤 **Traveler** | John Traveler | `traveler@example.com` | `traveler123` | `traveler-dashboard.html` |

> 💡 **Viva Tip:** Every web page contains a top **"Demo Bar"** with 1-click login buttons to instantly switch between Traveler, Agent, and Admin roles without retyping credentials during live exams.

---

## 🔌 REST API Documentation

### 1. Authentication
- `POST /api/auth/login`: Authenticate user `{ email, password }`.
- `POST /api/auth/register`: Register new account `{ fullName, email, phone, password, role }` (*blocks `ADMIN`*).

### 2. Travel Inventory
- `GET /api/flights`: Retrieve flights (optional filters: `origin`, `dest`, `date`).
- `POST /api/flights`: Create flight listing (Agent/Admin).
- `GET /api/hotels`: Retrieve hotels (optional filters: `destId`, `city`).
- `POST /api/hotels`: Create hotel listing.
- `GET /api/cars`: Retrieve rental cars (optional filters: `location`, `type`).
- `POST /api/cars`: Create rental car listing.
- `GET /api/packages`: Retrieve tour packages (optional filter: `destId`).
- `POST /api/packages`: Create tour package.

### 3. Bookings & Itinerary
- `GET /api/bookings?userId={id}`: List all reservations made by user.
- `POST /api/bookings`: Universal checkout `{ userId, bookingType, flightId, hotelId, carId, packageId, travelDate, startDate, endDate, quantity, amount, paymentMethod }`.
- `POST /api/bookings/cancel`: Soft cancellation `{ bookingId, userId, isAdmin }`.
- `GET /api/itinerary?userId={id}`: Returns unified chronological timeline of active flights, hotels, and cars.

### 4. Communication
- `GET /api/messages?userId={id}` or `?agentId={id}`: Retrieve message history.
- `POST /api/messages`: Send inquiry `{ userId, agentId, subject, message }`.
- `POST /api/messages/reply`: Agent reply `{ messageId, reply }`.

### 5. Portals
- `GET /api/agent/stats?agentId={id}`: Agent metrics (Listings, Pending, Bookings, Revenue).
- `GET /api/agent/listings?agentId={id}`: Agent's inventory items with approval status.
- `GET /api/agent/bookings?agentId={id}`: Customer reservations on agent's inventory.
- `GET /api/admin/stats`: 8 Global platform KPIs.
- `GET /api/admin/users`: All registered user records.
- `POST /api/admin/toggle-user`: Enable/disable user `{ userId, status }`.
- `GET /api/admin/listings`: Moderation queue of all inventory items.
- `POST /api/admin/listings`: Moderate item `{ action: 'approve'|'reject'|'delete', type, id }`.
- `GET /api/admin/bookings`: Global booking records.
- `POST /api/admin/update-booking-status`: Force status `{ bookingId, status }`.
- `GET /api/admin/payments`: Transaction audit log.
- `GET /api/admin/settings`: Platform configurations.
- `POST /api/admin/settings`: Update platform settings.

---

## 🚀 Quick-Start Setup & Installation

### Prerequisites
- **Java Development Kit (JDK 17 or newer)** installed and added to `PATH`.
- **MySQL 8.0 Server** running locally on port `3306`.

### Step 1: Clone or Open Project
```powershell
cd C:\Users\Chahat chaudhary\.gemini\antigravity\scratch\travel_booking_system
```

### Step 2: Database Configuration
Verify your MySQL credentials in `src/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/travel_booking_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
db.user=root
db.password=root
```
*(The system automatically creates the database, all 10 tables, and default seed data on startup!)*

### Step 3: Compile Source Code
Double-click `compile.bat` or run:
```powershell
.\compile.ps1
```

### Step 4: Run Application
- **To run the Full Web Application (Default)**:
  ```powershell
  .\run_web.bat
  ```
  Open browser: `http://localhost:8080`

- **To run the Desktop Swing GUI**:
  ```powershell
  .\run.bat
  ```

- **To run the Automated Test Suites**:
  ```powershell
  # 1. Agent Hotel Creation & Moderation Test
  java -cp "bin;lib/mysql-connector-j-8.3.0.jar" AgentHotelCreationTest

  # 2. Multi-User Session Isolation & Zero-State Dashboard Test
  java -cp "bin;lib/mysql-connector-j-8.3.0.jar" UserIsolationTest

  # 3. Comprehensive End-to-End System Test
  java -cp "bin;lib/mysql-connector-j-8.3.0.jar" EndToEndTest
  ```

---

## 🐳 Docker & Cloud Deployment

VoyageQuest includes a multi-stage `Dockerfile` and dynamic environment variable parser supporting **Render**, **Railway**, and self-hosted Linux VPS.

1. **Build Container**:
   ```bash
   docker build -t voyagequest-platform .
   ```
2. **Run Container**:
   ```bash
   docker run -p 8080:8080 -e DB_URL="jdbc:mysql://host:3306/db" -e DB_USER="root" -e DB_PASSWORD="password" voyagequest-platform
   ```

---

## 🎬 18-Step Live Viva Demonstration Script

Follow these exact steps during your college project presentation to demonstrate every feature required by your examiner:

1. **Launch Server**: Start `run_web.bat` and navigate to `http://localhost:8080`.
2. **Explore Public Home (`index.html`)**: Show the hero section, quick search tabs for Flights, Hotels, Cars, and Packages.
3. **One-Click Traveler Login**: Click **"Traveler (John)"** in the top Demo Bar. Note that the navbar immediately displays `TRAVELER` badge and `Traveler Hub` button.
4. **Search Flights (`flights.html`)**: Filter by origin "Delhi", click **"Book Flight"** on Air India Express.
5. **Universal Booking Engine (`booking.html`)**: Set 2 passengers, select departure date, review live fare computation ($\$95 \times 2 = \$190$).
6. **Digital Payment**: Choose **UPI**, enter `john@upi`, and click **"Confirm & Pay"**. Show the instant e-Voucher modal.
7. **View My Bookings (`my-bookings.html`)**: Filter by **Flights**, click **"Voucher"** to show printable ticket layout with barcode.
8. **Unified Itinerary (`itinerary.html`)**: Show how the flight has been automatically added to the vertical chronological itinerary.
9. **Book Hotel & Rental Car**:
   - Go to `hotels.html`, book 1 Deluxe room at The Grand Palace for 3 nights.
   - Go to `cars.html`, book a Toyota Fortuner SUV for 3 days.
10. **Re-inspect Itinerary**: Revisit `itinerary.html` to show the full 3-segment trip timeline (Flight Leg $\rightarrow$ Hotel Stay $\rightarrow$ Rental Car Pickup).
11. **Send Traveler Inquiry (`traveler-dashboard.html`)**: Under "Messages", click "Send New Message", select "Assigned Travel Agent (Agent Smith)", subject: "Airport Pickup Request", body: "Do you offer airport taxi pickup?".
12. **Switch to Travel Agent Role**: Click **"Travel Agent (Skyline)"** in the top Demo Bar. The portal redirects to `agent-dashboard.html`.
13. **Agent Reviews Inquiry & Replies**: Under "Inquiries & Feedback", view John's message and click "Reply". Enter: "Yes, our luxury cab will meet you at Terminal 3." Submit reply.
14. **Agent Publishes New Inventory**:
    - Click "+ Add Travel Inventory" $\rightarrow$ "Add New Flight".
    - Fill in airline: "Indigo Airlines", flight: "6E-502", DEL to GOA, price: $\$85$, seats: 40.
    - Submit. Notice listing appears with badge **`PENDING`** (Awaiting Admin review).
15. **Switch to Administrator Role**: Click **"Admin"** in top Demo Bar $\rightarrow$ lands on `admin-dashboard.html`.
16. **Admin Approves Listing**: Under "Listing Moderation Queue", locate the newly created Indigo 6E-502 flight and click **"Approve"**. Status flips to `APPROVED`.
17. **Admin Platform Oversight**:
    - Review the **8 summary KPI metric cards**.
    - Go to "User Moderation" $\rightarrow$ show `ACTIVE` accounts and disable/enable controls.
    - Go to "System Configurations" $\rightarrow$ update platform commission or tax rate.
18. **Verify Public Availability**: Switch back to Traveler $\rightarrow$ open `flights.html` $\rightarrow$ verify the newly approved Indigo flight is now live and bookable!

---

## 🎓 Viva Voce Technical Question Bank (CSE Defense)

### Q1: What architecture does this project follow?
**Answer:** The project follows a strictly decoupled **Three-Tier MVC (Model-View-Controller) Architecture**:
- **Model Layer (`src/model`)**: Encapsulates entity state and business objects (`User`, `Flight`, `Hotel`, `Car`, `Booking`, `Message`).
- **Data Access Layer (`src/dao`)**: Pure JDBC implementation using `PreparedStatement` to run CRUD SQL queries against MySQL without mixing business logic.
- **Service Layer (`src/service`)**: Implements business constraints, validation, inventory decrement/restoration transactions, and fare calculations.
- **Controller / Presentation Layer (`src/WebServer.java` & `frontend/`)**: Exposes REST endpoints consuming JSON over standard HTTP and rendering modern responsive views.

### Q2: Why did you use core Java `HttpServer` instead of Spring Boot?
**Answer:** While Spring Boot simplifies setup with annotations, it creates a heavy abstraction layer. Using standard JDK `com.sun.net.httpserver.HttpServer`:
1. Requires **zero external framework dependencies**, demonstrating core Java competency.
2. Yields an ultra-lightweight footprint ($<30\text{ MB}$ memory vs $>350\text{ MB}$ for Spring).
3. Directly demonstrates how HTTP request parsing, headers, MIME types, and JSON responses operate at the socket protocol level.

### Q3: How do you prevent SQL Injection attacks?
**Answer:** Every database operation in our DAOs exclusively utilizes parameterized **`java.sql.PreparedStatement`**. User input values are passed as typed parameters (`setString()`, `setInt()`, `setDouble()`), ensuring that the database driver escapes all input strings and treats them strictly as data literals, never as executable SQL code.

### Q4: How is sensitive user authentication secured?
**Answer:**
1. Passwords are never stored in plain text.
2. The `PasswordUtil` utility hashes passwords using **SHA-256** cryptographic one-way hashing combined with salt.
3. Direct self-registration of `ADMIN` accounts is strictly blocked in `AuthService`; administrator accounts can only be provisioned through system database migrations.

### Q5: How is inventory concurrency handled when a traveler books or cancels?
**Answer:**
- When a booking is confirmed, the system executes an atomic SQL query:
  ```sql
  UPDATE flights SET available_seats = available_seats - ? WHERE id = ? AND available_seats >= ?;
  ```
  If zero rows are updated, a `ValidationException` is thrown indicating sold-out status.
- When a cancellation is initiated, the quantity is automatically credited back to the corresponding inventory table using `available_seats = available_seats + ?`.

### Q6: What OOP principles are implemented in this project?
**Answer:**
1. **Encapsulation**: All entity classes have private attributes accessed through getter and setter methods with boundary validation.
2. **Inheritance**: `BaseEntity` defines standard `id` and `createdAt` properties, inherited by `User`, `Flight`, `Hotel`, `Car`, `TravelPackage`, and `Booking`.
3. **Polymorphism**: The universal `BookingDAO` polymorphically handles reservations for flights, hotels, cars, and tour packages using unified database schemas and dynamic left joins.
4. **Abstraction**: Database connection handling is abstracted behind `DatabaseConnection.getConnection()`, shielding caller classes from driver loading and connection pooling details.

---

## 📄 License & Attribution
Developed for B.Tech Computer Science and Engineering Academic Capstone / Minor Project Submissions. Free to use, adapt, and extend for educational evaluations.
