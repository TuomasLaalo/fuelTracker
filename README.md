# Fuel Tracker

A full-stack web application for tracking fuel consumption, costs, and vehicle analytics. Users can register, add vehicles, log fuel entries, and view detailed analytics with consumption graphs and monthly statistics.

## 🌐 Live Application

**Frontend (Live):** [https://fueltracker-frontend.netlify.app](https://fueltracker-frontend.netlify.app)

**Backend API:** (Deployed separately - see deployment section)

> **Note for user:** The application is deployed and accessible at the URL above. You can register a new account or use test credentials if available.

## 📋 Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Key Files & Locations](#key-files--locations)

## ✨ Features

- **User Authentication**: Registration and login with secure password hashing (BCrypt)
- **Vehicle Management**: Add, edit, and delete vehicles with details (make, model, license plate, tank capacity)
- **Fuel Entry Tracking**: Log fuel purchases with date, location, price, odometer reading
- **Analytics Dashboard**: 
  - Average fuel consumption (L/100km)
  - Consumption history graphs
  - Monthly statistics (consumption, costs, fuel usage)
  - Total distance, fuel used, and costs
- **Automatic Consumption Calculation**: Uses tank capacity logic to automatically detect full tanks and calculate consumption cycles

## 🛠 Technologies

### Backend
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security** - Basic authentication
- **Spring Data JPA** - Database access
- **PostgreSQL** - Production database
- **Maven** - Build tool

### Frontend
- **React 18** - UI framework
- **Vite** - Build tool and dev server
- **Material-UI (MUI)** - Component library
- **React Router** - Navigation
- **Zustand** - State management
- **Axios** - HTTP client
- **Recharts** - Data visualization
- **React Hook Form + Yup** - Form handling and validation
- **dayjs** - Date manipulation

## 📁 Project Structure

```
fueltracker/
├── frontend/                 # React frontend application
│   ├── src/
│   │   ├── api/             # API service layer
│   │   ├── components/      # Reusable components
│   │   ├── pages/           # Page components
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   ├── Vehicles.jsx
│   │   │   ├── FuelEntries.jsx
│   │   │   └── Analytics.jsx
│   │   └── store/           # Zustand state management
│   ├── public/              # Static assets
│   └── dist/                # Production build (generated)
│
├── src/main/java/fi/laalo/fueltracker/
│   ├── config/              # Security and configuration
│   ├── controller/          # REST API controllers
│   ├── dto/                 # Data Transfer Objects
│   ├── model/               # JPA entities (User, Vehicle, FuelEntry)
│   ├── repository/          # JPA repositories
│   ├── service/             # Business logic
│   └── mapper/              # Entity-DTO mappers
│
├── src/test/java/           # Test files
├── src/main/resources/      # Configuration files
│   ├── application.properties
│   ├── application-dev.properties
│   └── application-prod.properties
│
├── DEPLOYMENT.md            # Deployment instructions
└── README.md               # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Node.js 18+** and **npm**
- **PostgreSQL** (for production) or H2 (for development)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd fueltracker
   ```

2. **Configure database** (for local development)
   - Update `src/main/resources/application-dev.properties` with your PostgreSQL credentials
   - Or use H2 in-memory database (uncomment in `pom.xml`)

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   Backend will start on `http://localhost:8080`

4. **Run tests** (optional)
   ```bash
   mvn test
   ```

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start development server**
   ```bash
   npm run dev
   ```
   Frontend will start on `http://localhost:5173`

4. **Build for production**
   ```bash
   npm run build
   ```

## 📡 API Documentation

### Base URL
- **Local:** `http://localhost:8080/api`
- **Production:** (See deployment configuration)

### Main Endpoints

#### Authentication
- `POST /api/auth/register` - Register new user
- `GET /api/users/me` - Get current user

#### Vehicles
- `GET /api/vehicles` - Get all vehicles (authenticated user)
- `GET /api/vehicles/{id}` - Get vehicle by ID
- `POST /api/vehicles` - Create new vehicle
- `PUT /api/vehicles/{id}` - Update vehicle
- `DELETE /api/vehicles/{id}` - Delete vehicle

#### Fuel Entries
- `GET /api/fuelentries` - Get all fuel entries (authenticated user)
- `GET /api/fuelentries/vehicle/{vehicleId}` - Get entries for specific vehicle
- `GET /api/fuelentries/{id}` - Get entry by ID
- `POST /api/fuelentries` - Create new fuel entry
- `PUT /api/fuelentries/{id}` - Update fuel entry
- `DELETE /api/fuelentries/{id}` - Delete fuel entry

#### Analytics
- `GET /api/analytics/vehicles/{vehicleId}/consumption` - Get average consumption
- `GET /api/analytics/vehicles/{vehicleId}/history` - Get consumption history
- `GET /api/health` - Health check endpoint

**Authentication:** All endpoints (except `/api/auth/register` and `/api/health`) require Basic Authentication.

## 🧪 Testing

### Backend Tests

Located in `src/test/java/fi/laalo/fueltracker/`:

- **RepositoryIntegrationTest** - Integration tests for repositories (7 tests)
- **UserControllerTest** - Unit tests for User endpoints (4 tests)
- **VehicleControllerTest** - Unit tests for Vehicle endpoints (6 tests)
- **FuelEntryControllerTest** - Unit tests for FuelEntry endpoints (5 tests)

**Run all tests:**
```bash
mvn test
```

**Run specific test:**
```bash
mvn test -Dtest=UserControllerTest
```

### Test Coverage
- **Total Tests:** 22 tests
- **Coverage:** Controllers and repositories fully tested

## 🚢 Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions.

### Quick Deployment Summary

**Backend:**
- Build: `mvn clean package`
- Deploy JAR file: `target/fueltracker-0.0.1-SNAPSHOT.jar`
- Set environment variables for database and CORS

**Frontend:**
- Build: `npm run build` (in `frontend/` directory)
- Deploy `dist/` folder to hosting service (Netlify, Vercel, etc.)
- Set `VITE_API_BASE_URL` environment variable

## 📍 Key Files & Locations

### For Teachers - Important Files to Review

#### Backend Architecture
- **Controllers:** `src/main/java/fi/laalo/fueltracker/controller/`
  - `AuthController.java` - Authentication endpoints
  - `VehicleController.java` - Vehicle CRUD operations
  - `FuelEntryController.java` - Fuel entry management
  - `AnalyticsController.java` - Analytics calculations
- **Services:** `src/main/java/fi/laalo/fueltracker/service/`
  - `FuelAnalyticsService.java` - **Complex business logic** for consumption calculation using tank capacity
  - `UserService.java`, `VehicleService.java`, `FuelEntryService.java`
- **Models:** `src/main/java/fi/laalo/fueltracker/model/`
  - `User.java`, `Vehicle.java`, `FuelEntry.java` - JPA entities
- **Security:** `src/main/java/fi/laalo/fueltracker/config/SecurityConfig.java`

#### Frontend Architecture
- **Pages:** `frontend/src/pages/`
  - `Analytics.jsx` - **Complex analytics dashboard** with charts
  - `Vehicles.jsx` - Vehicle management
  - `FuelEntries.jsx` - Fuel entry logging
- **State Management:** `frontend/src/store/`
  - `dataStore.js` - Zustand store for vehicles and fuel entries
  - `authStore.js` - Authentication state
- **API Layer:** `frontend/src/api/`
  - `services.js` - API service methods
  - `axios.js` - Axios configuration with interceptors

#### Configuration
- **Backend Config:** `src/main/resources/application-prod.properties`
- **Frontend Config:** `frontend/.env.production` (not in repo, see deployment docs)
- **Security Config:** `src/main/java/fi/laalo/fueltracker/config/SecurityConfig.java`

#### Testing
- **Test Files:** `src/test/java/fi/laalo/fueltracker/`
- **Test Suite:** See test files for comprehensive coverage

### Key Features Implementation

1. **Tank Capacity Logic** (`FuelAnalyticsService.java`)
   - Automatically detects full tanks based on tank capacity
   - Calculates consumption cycles between full tanks
   - Handles edge cases (overfill, partial fills)

2. **Analytics Dashboard** (`Analytics.jsx`)
   - Vehicle-specific monthly statistics
   - Consumption history graphs
   - Real-time calculations from fuel entries

3. **Security** (`SecurityConfig.java`)
   - Basic authentication
   - CORS configuration
   - User-specific data access

## 📝 Notes

- The application uses **tank capacity logic** to automatically detect full tanks and calculate consumption, eliminating the need for manual "full tank" flags
- All user data is isolated - users can only see and manage their own vehicles and fuel entries
- Production uses PostgreSQL with `validate` mode (schema must exist)
- Frontend and backend are deployed separately and communicate via REST API

## 📄 License

See [LICENSE](LICENSE) file for details.

---

**Course Project** - Full-stack web application demonstrating REST API, authentication, database operations, and data visualization.
