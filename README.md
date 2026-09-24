# ✈ VoyageQuest - Online Travel Booking System

> A complete, desktop-based **Online Travel Booking & Tourism Management System** built with **Java Swing**, **JDBC**, and **MySQL**, adhering to clean **MVC-style architectural separation** and **Object-Oriented Programming (OOP)** principles. Designed specifically as a high-scoring college project for B.Tech / BCA / MCA students with comprehensive viva defense materials.

---

## 📌 Table of Contents
1. [Project Overview](#-project-overview)
2. [Key Features](#-key-features)
3. [Technologies Used](#-technologies-used)
4. [OOP Principles Demonstrated (Viva Special)](#-oop-principles-demonstrated-viva-special)
5. [System Architecture (MVC)](#-system-architecture-mvc)
6. [Database Schema & ER Design](#-database-schema--er-design)
7. [Default Sample Credentials](#-default-sample-credentials)
8. [Prerequisites & Software Requirements](#-prerequisites--software-requirements)
9. [Step-by-Step Installation & Database Setup](#-step-by-step-installation--database-setup)
10. [How to Compile & Run](#-how-to-compile--run)
11. [Project Folder Structure](#-project-folder-structure)
12. [Viva Questions & Answers Guide](#-viva-questions--answers-guide)

---

## 🌟 Project Overview

**VoyageQuest** enables customers to explore popular tourist destinations, customize and book holiday packages, reserve hotel accommodations, execute simulated digital payments (UPI, Cards, Net Banking, Pay on Arrival), view real-time reservation vouchers, and manage booking cancellations.

It provides administrators with an operational control center featuring **8 summary KPI metric cards**, user moderation, destination management, package and hotel catalog configuration, and financial payment audits.

---

## ✨ Key Features

### 👤 Customer / User Portal
- **Login & Registration**:
  - Secure SHA-256 password hashing.
  - Comprehensive validations: empty fields, email regex, 10-digit phone, password confirmation, duplicate email detection.
- **Modern User Dashboard**:
  - Personalized welcome banner and live system date.
  - Quick KPI summary cards (Curated Packages, Top Destinations, Partner Hotels).
  - Clean sidebar navigation.
- **Destinations Catalog**:
  - Explore 8 iconic Indian destinations (Goa, Manali, Jaipur, Kashmir, Delhi, Kerala, Rishikesh, Udaipur).
  - Search by destination, state, or attractions.
  - Direct "Explore Packages for this Destination" shortcut.
- **Travel Packages**:
  - Browse 12+ pre-loaded packages with duration, price per person, inclusions (Hotel, Food, Transport), and sightseeing itineraries.
  - Filter packages by destination and keywords.
- **Hotel Reservations**:
  - Browse 10 partner hotels with star ratings, room types, price per night, and real-time room availability counters.
- **Interactive Booking Engine**:
  - Select departure date (validated to prevent past dates).
  - Specify number of travelers (must be $> 0$).
  - Optional hotel selection (room capacity automatically decremented upon reservation).
  - Live automatic cost calculation:
    $$\text{Package Cost} = \text{Package Price} \times \text{Persons}$$
    $$\text{Hotel Cost} = \text{Hotel Price} \times \text{Nights} \quad (\text{if hotel selected})$$
    $$\text{Total Amount} = \text{Package Cost} + \text{Hotel Cost}$$
  - Generates unique Booking Reference Code (e.g. `TB-2026-4644`).
- **Simulated Payment Gateway**:
  - Simulated payment processing with realistic delay animation.
  - 4 Payment Options:
    1. **UPI**: Google Pay / PhonePe / Paytm / BHIM VPA verification.
    2. **Credit & Debit Cards**: 16-digit card validation, MM/YY expiry, 3-digit CVV, Cardholder name.
    3. **Net Banking**: Instant bank gateway selector (SBI, HDFC, ICICI, Axis, PNB, etc.).
    4. **Pay On Arrival / Cash**: Automatically marked as `PENDING` until arrival.
  - Generates unique transaction codes (e.g. `TXN-UPI-XXXX`, `TXN-CRD-XXXX`).
- **Printable Booking Receipt**:
  - Displays complete itinerary, fare breakdown, and transaction status.
  - Includes **"Print Receipt"** (native OS print dialog) and **"Save Receipt to File"** (saves `.txt` voucher).
- **Booking History & Soft Cancellation**:
  - Tabular view of all user's past and upcoming trips.
  - One-click cancellation with confirmation prompt:
    - Status updated to `CANCELLED` (record preserved in history).
    - Automatically restores reserved room count back to the hotel.
- **User Profile Management**:
  - Displays user metrics: Total Bookings, Active Bookings, Cancelled Trips, Total Amount Spent.
  - Update profile name and phone number.
  - Secure password update modal verifying current password.

---

### 🛡 Administrator Control Center
- **8 KPI Summary Metric Cards**:
  1. Total Registered Users
  2. Total Destinations
  3. Total Travel Packages
  4. Total Partner Hotels
  5. Total System Bookings
  6. Confirmed Trips
  7. Cancelled Trips
  8. Total Revenue Generated (₹)
- **User Management**:
  - View all registered accounts.
  - Search by name, email, or phone.
  - Toggle user account status (`ACTIVE` $\leftrightarrow$ `DISABLED`) to control login access.
  - Passwords protected via SHA-256 (never shown in plain text).
- **Destination Catalog Management**:
  - Add new destinations.
  - Edit existing destinations (state, description, attractions, best time).
  - Delete destinations with cascading safeguards.
- **Package Management**:
  - Create tour packages with custom durations, pricing, places covered, and inclusion checkboxes.
  - Edit / deactivate packages.
- **Hotel & Room Inventory Management**:
  - Register new hotels with room categories and ratings.
  - Direct **"Update Rooms Count"** quick-adjust tool to increase or decrease room availability.
- **Booking Administration**:
  - View all customer reservations.
  - Open and inspect printable customer receipts.
  - Modify booking statuses (`CONFIRMED`, `COMPLETED`, `CANCELLED`).
- **Payment Audits**:
  - Complete financial ledger of transaction codes, payment methods, customer names, and timestamps.

---

## 💻 Technologies Used

| Layer | Technology |
|---|---|
| **Programming Language** | Java (JDK 8 / 11 / 17 / 21 / 26 compatible) |
| **GUI Toolkit** | Java Swing (`javax.swing`, `java.awt`) |
| **Database** | MySQL 8.0+ |
| **Connectivity** | Java Database Connectivity (JDBC) |
| **Connector** | MySQL Connector/J 8.3.0 |
| **Security** | SHA-256 Password Hashing |
| **Build Tools** | Native `javac` / Command Prompt batch scripts (`compile.bat`, `run.bat`) |

---

## 🧠 OOP Principles Demonstrated (Viva Special)

1. **Classes and Objects**:
   - Every entity in the system is mapped to a dedicated domain class (`User`, `Destination`, `TravelPackage`, `Hotel`, `Booking`, `Payment`).
2. **Encapsulation**:
   - All class attributes have `private` or `protected` visibility.
   - Access is mediated exclusively through public getter and setter methods with validation checks.
3. **Inheritance**:
   - `BaseEntity` defines shared persistence properties (`id`, `createdAt`) extended by all model classes.
   - `BaseDAO` encapsulates common JDBC connection acquisition and resource cleanup extended by `UserDAO`, `BookingDAO`, etc.
   - `BaseFrame` standardizes window properties, centering, and alert popups extended by `LoginFrame`, `UserDashboard`, `AdminDashboard`.
4. **Polymorphism (Strategy Pattern)**:
   - `PaymentProcessor` interface defines `processPayment(amount, details)` and `getMethodName()`.
   - Concrete implementations `UpiPaymentProcessor`, `CardPaymentProcessor`, `NetBankingPaymentProcessor`, and `CashPaymentProcessor` provide polymorphic behaviors at runtime.
5. **Interfaces**:
   - `GenericDAO<T>` provides clean generic CRUD contract (`save`, `update`, `delete`, `findById`, `findAll`).
   - `PaymentProcessor` decouples payment mechanism from payment recording.
6. **Exception Handling**:
   - Checked custom exception hierarchy:
     - `TravelException` (root)
     - `ValidationException` (business validation failures)
     - `AuthenticationException` (credential/authorization issues)
     - `DatabaseException` (SQL / connection failures)
   - Extensive usage of Java 7+ **try-with-resources** to eliminate memory leaks and guarantee automatic closure of `Connection`, `PreparedStatement`, and `ResultSet`.

---

## 🏛 System Architecture (MVC)

```text
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│  (LoginFrame, RegisterFrame, UserDashboard, AdminDashboard, Dialogs)   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                            SERVICE LAYER                               │
│  (AuthService, BookingService, PaymentService, DestinationService, ...) │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                          DATA ACCESS LAYER                             │
│     (UserDAO, DestinationDAO, PackageDAO, HotelDAO, BookingDAO, ...)   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                           DATABASE LAYER                               │
│                    MySQL (travel_booking_system)                       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🗄 Database Schema & ER Design

### Database Name: `travel_booking_system`

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : "places"
    USERS ||--o{ PAYMENTS : "makes"
    DESTINATIONS ||--o{ PACKAGES : "has"
    DESTINATIONS ||--o{ HOTELS : "features"
    PACKAGES ||--o{ BOOKINGS : "booked_in"
    HOTELS ||--o{ BOOKINGS : "accommodates"
    BOOKINGS ||--|| PAYMENTS : "generates"

    USERS {
        int id PK
        string full_name
        string email UK
        string phone
        string password_hash
        enum role
        enum status
        timestamp created_at
    }

    DESTINATIONS {
        int id PK
        string name UK
        string state
        text description
        text attractions
        string best_time
        timestamp created_at
    }

    PACKAGES {
        int id PK
        string package_name
        int destination_id FK
        int duration_days
        int duration_nights
        decimal price_per_person
        text places_covered
        boolean hotel_included
        boolean food_included
        boolean transport_included
        text description
        enum status
    }

    HOTELS {
        int id PK
        string hotel_name
        int destination_id FK
        string address
        string room_type
        decimal price_per_night
        int available_rooms
        decimal rating
        text description
        enum status
    }

    BOOKINGS {
        int id PK
        string booking_code UK
        int user_id FK
        int package_id FK
        int hotel_id FK
        date travel_date
        int persons
        decimal package_cost
        decimal hotel_cost
        decimal total_amount
        text special_requests
        enum booking_status
        timestamp created_at
    }

    PAYMENTS {
        int id PK
        string transaction_code UK
        int booking_id FK
        int user_id FK
        decimal amount
        enum payment_method
        string payment_details
        enum payment_status
        timestamp created_at
    }
```

---

## 🔑 Default Sample Credentials

| Role | Email / Username | Password | Notes |
|---|---|---|---|
| **System Administrator** | `admin@travel.com` | `admin123` | Full administrative control |
| **Customer (Sample 1)** | `priya@example.com` | `user123` | Pre-loaded bookings & payments |
| **Customer (Sample 2)** | `rahul@example.com` | `user123` | Pre-loaded bookings |
| **Customer (Sample 3)** | `amit@example.com` | `user123` | Sample cancelled booking |

*(Quick prefill buttons are provided on the Login screen for instant testing during viva presentations.)*

---

## ⚙ Prerequisites & Software Requirements

1. **Java Development Kit (JDK)**: Version 8 or higher (tested and verified on Java 26).
2. **MySQL Server**: Version 8.0 or higher.
3. **MySQL Connector/J**: `mysql-connector-j-8.3.0.jar` (already included in `lib/`).

---

## 🚀 Step-by-Step Installation & Database Setup

### Step 1: Clone or Navigate to Project Directory
```powershell
cd "C:\Users\Chahat chaudhary\.gemini\antigravity\scratch\travel_booking_system"
```

### Step 2: Configure Database Credentials
Open `src/db.properties` (or `bin/db.properties`):
```properties
db.url=jdbc:mysql://localhost:3306/travel_booking_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
```
*(Update `db.user` and `db.password` if your local MySQL uses different credentials.)*

### Step 3: Run Database Scripts (Optional - Automatic Bootstrapping Included!)
The application includes a `DatabaseInitializer` that will **automatically** create the database, tables, and sample data on first launch!

Alternatively, you can manually run the SQL scripts via MySQL command line:
```cmd
mysql -u root -proot < database\schema.sql
mysql -u root -proot < database\sample_data.sql
```

---

## 🔨 How to Compile & Run

### Method A: One-Click Batch Scripts (Windows)
1. Double-click **`compile.bat`** (or execute `.\compile.bat` in CMD).
2. Double-click **`run.bat`** (or execute `.\run.bat` in CMD).

### Method B: Command Line (CMD or PowerShell)

**Compile:**
```cmd
javac -d bin -cp "lib\mysql-connector-j-8.3.0.jar;src" src\util\*.java src\model\*.java src\dao\*.java src\service\*.java src\view\*.java src\Main.java
copy /Y src\db.properties bin\
```

**Run Application:**
```cmd
java -cp "bin;lib\mysql-connector-j-8.3.0.jar" Main
```

**Run Automated Test Suite (15 Test Cases):**
```cmd
java -cp "bin;lib\mysql-connector-j-8.3.0.jar" TestSystem
```

### Method C: In Eclipse / IntelliJ / NetBeans
1. Open the IDE and select **Open Project** -> Choose `travel_booking_system`.
2. Add `lib/mysql-connector-j-8.3.0.jar` to your Project's **Build Path / Libraries**.
3. Set `Main.java` as the Main Class.
4. Click **Run**.

---

## ☁ Cloud Deployment with Docker (Render / Railway)

This application can be deployed as a public web application in the cloud using the provided `Dockerfile`.

### 1. Supported Cloud Environment Variables
| Variable Name | Description | Example |
|---|---|---|
| `PORT` | Web server port (injected automatically by Render/Railway) | `8080` or `10000` |
| `DB_URL` / `DATABASE_URL` / `MYSQL_URL` | Cloud MySQL connection URL | `mysql://root:pass@host:3306/railway` |
| `DB_USER` / `MYSQLUSER` | Database username | `root` |
| `DB_PASSWORD` / `MYSQLPASSWORD` | Database password | `secret_password` |
| `DB_HOST` / `MYSQLHOST` | Database host | `roundhouse.proxy.rlwy.net` |
| `DB_PORT` / `MYSQLPORT` | Database port | `3306` |
| `DB_NAME` / `MYSQLDATABASE` | Database name | `railway` |

> 🔒 **Security Notice**: Never commit database passwords to GitHub! Always add them as **Environment Variables** in the cloud platform's dashboard.

---

### 2. Deploying on Railway (Fastest - Includes Free MySQL)
1. Go to **[Railway.app](https://railway.app)** and log in with your GitHub account.
2. Click **New Project** &rarr; **Provision MySQL**.
3. Click **Add Service** &rarr; **GitHub Repo** &rarr; Select your `travel-booking-system` repository.
4. Railway will automatically detect the `Dockerfile` and build the container!
5. In your Railway Web Service settings &rarr; **Variables**, add:
   - `DATABASE_URL`: `${{MySQL.MYSQL_URL}}` *(or Railway links them automatically)*
6. In **Settings** &rarr; **Networking**, click **Generate Domain** (e.g. `travel-booking-system.up.railway.app`).
7. Open the generated domain in your browser!

---

### 3. Deploying on Render
1. Go to **[Render.com](https://render.com)** and log in.
2. Click **New +** &rarr; **Web Service**.
3. Connect your GitHub repository `travel-booking-system`.
4. Render will detect the `Dockerfile` automatically (Runtime: **Docker**).
5. In **Environment Variables**, add:
   - `DB_URL`: Your cloud MySQL JDBC URL
   - `DB_USER`: Your cloud MySQL username
   - `DB_PASSWORD`: Your cloud MySQL password
6. Click **Create Web Service**. Once built, Render will provide a public HTTPS URL (e.g. `https://travel-booking-system.onrender.com`).

---

## 📁 Project Folder Structure

```text
travel_booking_system/
├── bin/                             # Compiled Java .class files
│   └── db.properties
├── database/                        # Database scripts
│   ├── schema.sql                   # Table schemas & foreign keys
│   └── sample_data.sql              # Preloaded sample data (destinations, packages, users)
├── lib/                             # External library dependencies
│   └── mysql-connector-j-8.3.0.jar  # Official MySQL JDBC Driver
├── src/                             # Source code
│   ├── model/                       # MVC: Model Layer
│   │   ├── BaseEntity.java          # Abstract entity (Inheritance)
│   │   ├── User.java
│   │   ├── Destination.java
│   │   ├── TravelPackage.java
│   │   ├── Hotel.java
│   │   ├── Booking.java
│   │   ├── Payment.java
│   │   └── AdminStats.java
│   ├── dao/                         # Data Access Object Layer
│   │   ├── GenericDAO.java          # Generic DAO interface
│   │   ├── BaseDAO.java             # Abstract DAO base
│   │   ├── UserDAO.java
│   │   ├── DestinationDAO.java
│   │   ├── PackageDAO.java
│   │   ├── HotelDAO.java
│   │   ├── BookingDAO.java
│   │   └── PaymentDAO.java
│   ├── service/                     # Service / Business Logic Layer
│   │   ├── TravelException.java     # Custom exceptions
│   │   ├── ValidationException.java
│   │   ├── AuthenticationException.java
│   │   ├── DatabaseException.java
│   │   ├── PaymentProcessor.java    # Strategy interface (Polymorphism)
│   │   ├── PaymentResult.java
│   │   ├── UpiPaymentProcessor.java
│   │   ├── CardPaymentProcessor.java
│   │   ├── NetBankingPaymentProcessor.java
│   │   ├── CashPaymentProcessor.java
│   │   ├── PaymentService.java
│   │   ├── AuthService.java
│   │   ├── BookingService.java
│   │   ├── DestinationService.java
│   │   ├── PackageService.java
│   │   ├── HotelService.java
│   │   └── AdminService.java
│   ├── view/                        # MVC: Presentation Layer (Swing GUI)
│   │   ├── BaseFrame.java           # Base styled window
│   │   ├── LoginFrame.java          # Modern split-panel login
│   │   ├── RegisterFrame.java       # User registration form
│   │   ├── UserDashboard.java       # Customer portal & bookings
│   │   ├── AdminDashboard.java      # Admin KPI dashboard & management
│   │   ├── BookingDialog.java       # Package reservation modal
│   │   ├── PaymentDialog.java       # Simulated multi-channel payment modal
│   │   ├── ReceiptDialog.java       # Printable reservation voucher
│   │   ├── DestinationDialog.java   # Admin destination form
│   │   ├── PackageDialog.java       # Admin package form
│   │   ├── HotelDialog.java         # Admin hotel & rooms form
│   │   └── ChangePasswordDialog.java
│   ├── util/                        # Utility Layer
│   │   ├── DatabaseConnection.java  # JDBC connection factory
│   │   ├── DatabaseInitializer.java # Auto-setup bootstrap
│   │   ├── PasswordUtil.java        # SHA-256 hashing
│   │   ├── ValidationUtil.java      # Input validation routines
│   │   ├── UITheme.java             # Design system palette & styles
│   │   └── SessionManager.java      # Logged-in session tracker
│   ├── db.properties                # Database credentials
│   ├── Main.java                    # Application launcher
│   └── TestSystem.java              # Automated test suite
├── compile.bat                      # Windows CMD compile script
├── run.bat                          # Windows CMD run script
├── compile.ps1                      # PowerShell compile script
├── run.ps1                          # PowerShell run script
└── README.md                        # Documentation & viva guide
```

---

## 🎓 Viva Questions & Answers Guide

### Q1: What architecture does this project use?
> **Answer**: The project uses an **MVC-style layered architecture**:
> - **Model**: Domain entities (`User`, `TravelPackage`, `Booking`, etc.) encapsulating data and state.
> - **View**: Java Swing components (`LoginFrame`, `UserDashboard`, `AdminDashboard`, `ReceiptDialog`).
> - **Controller/Service**: Business logic (`BookingService`, `AuthService`, `PaymentService`) coordinating validation and transactions.
> - **DAO (Data Access Object)**: Database abstraction using `PreparedStatement` to run SQL queries against MySQL.

### Q2: Why did you use `PreparedStatement` instead of `Statement`?
> **Answer**: 
> 1. **Security**: It prevents **SQL Injection attacks** by parameterizing inputs and separating code from data.
> 2. **Performance**: Prepared statements are pre-compiled and cached by the database engine for faster repetitive execution.
> 3. **Type Safety**: It automatically formats and escapes data types such as Dates, Strings, and Decimals.

### Q3: How is Polymorphism demonstrated in this system?
> **Answer**: Polymorphism is demonstrated in the **Payment Module** using the **Strategy Design Pattern**. The interface `PaymentProcessor` defines the contract `processPayment()`. Four concrete classes (`UpiPaymentProcessor`, `CardPaymentProcessor`, `NetBankingPaymentProcessor`, `CashPaymentProcessor`) provide different payment logic. At runtime, the `PaymentService` invokes `processPayment()` on the interface reference without hardcoding specific implementations.

### Q4: How is password security handled?
> **Answer**: Passwords are never stored in plain text. When a user registers or changes their password, `PasswordUtil.hashPassword()` generates a cryptographic **SHA-256** hash. During login, the input password is hashed and compared with the stored hash using `verifyPassword()`. Even the database administrator cannot read users' plain text passwords.

### Q5: How are hotel room inventories managed during booking and cancellation?
> **Answer**: When a user books a package with a hotel, `BookingService` decrements the hotel's `available_rooms` by 1. If the user cancels the booking, the booking status is set to `CANCELLED` (soft deletion preserving history) and `BookingService` increments the hotel's `available_rooms` count back by 1.

### Q6: What happens if the database tables do not exist when running for the first time?
> **Answer**: `DatabaseInitializer` runs on application startup. It checks if the `travel_booking_system` database and `users` table exist; if not, it automatically runs the DDL and DML statements to create all 6 tables and seeds sample destinations, packages, and hotels.
