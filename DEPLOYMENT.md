# Deployment Checklist

## Pre-Deployment Checklist

### ✅ Backend Configuration

- [x] **CORS Configuration**: Updated to read from `spring.web.cors.allowed-origins` property
- [x] **Database Configuration**: Production properties use environment variables
- [x] **JPA Settings**: Production uses `validate` instead of `update` for schema
- [x] **Logging**: SQL logging disabled in production
- [x] **Exception Handling**: Global exception handler in place
- [x] **Security**: Basic authentication configured

### ✅ Frontend Configuration

- [x] **API Base URL**: Configurable via `VITE_API_BASE_URL` environment variable
- [x] **Error Messages**: Removed hardcoded localhost references
- [x] **Build Script**: `npm run build` available

### ⚠️ Required Environment Variables

#### Backend (Spring Boot)
Set these environment variables or update `application-prod.properties`:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://your-db-host:5432/fueltracker
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_db_password

# CORS Configuration
SPRING_WEB_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com

# Active Profile
SPRING_PROFILES_ACTIVE=prod
```

#### Frontend (Vite)
Create a `.env.production` file or set environment variables:

```bash
VITE_API_BASE_URL=https://your-backend-api.com/api
```

## Deployment Steps

### 1. Backend Deployment

1. **Build the application:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Run tests (optional but recommended):**
   ```bash
   mvn test
   ```

3. **Set environment variables** (see above)

4. **Run the application:**
   ```bash
   java -jar target/fueltracker-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

   Or with environment variables:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export DATABASE_URL=jdbc:postgresql://...
   export DATABASE_USERNAME=...
   export DATABASE_PASSWORD=...
   export SPRING_WEB_CORS_ALLOWED_ORIGINS=https://your-frontend.com
   java -jar target/fueltracker-0.0.1-SNAPSHOT.jar
   ```

### 2. Frontend Deployment

1. **Install dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Set environment variables:**
   Create `.env.production`:
   ```bash
   VITE_API_BASE_URL=https://your-backend-api.com/api
   ```

3. **Build for production:**
   ```bash
   npm run build
   ```

4. **Deploy the `dist` folder** to your hosting service (e.g., Netlify, Vercel, AWS S3, etc.)

### 3. Database Setup

1. **Create production database:**
   ```sql
   CREATE DATABASE fueltracker;
   ```

2. **Run migrations** (if using Flyway/Liquibase) or let JPA create schema on first run
   - ⚠️ **Note**: Production uses `validate` mode, so schema must exist or be created manually

3. **Verify database connection** before deploying backend

## Post-Deployment Verification

- [ ] Backend health check: `GET /api/health` returns 200
- [ ] Frontend loads without errors
- [ ] User registration works
- [ ] User login works
- [ ] CORS is properly configured (no CORS errors in browser console)
- [ ] API requests succeed from frontend
- [ ] Database operations work correctly

## Security Considerations

- [x] ✅ Passwords are encrypted with BCrypt
- [x] ✅ Basic authentication implemented
- [x] ✅ CORS configured (needs production origins set)
- [ ] ⚠️ **Consider**: Enable HTTPS in production
- [ ] ⚠️ **Consider**: Add rate limiting
- [ ] ⚠️ **Consider**: Add request logging/monitoring
- [ ] ⚠️ **Consider**: Set up database backups

## Known Issues / Notes

1. **CSRF is disabled**: Currently disabled for API compatibility. Consider enabling for production if needed.
2. **Database schema**: Production uses `validate` mode - ensure schema exists before deployment
3. **Error messages**: Generic error messages in production (good for security, but may need logging)

## Troubleshooting

### CORS Errors
- Verify `SPRING_WEB_CORS_ALLOWED_ORIGINS` includes your frontend domain
- Check that frontend domain matches exactly (including protocol: https://)

### Database Connection Issues
- Verify `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` are set correctly
- Check database is accessible from deployment server
- Verify database exists and user has proper permissions

### Frontend API Connection Issues
- Verify `VITE_API_BASE_URL` is set correctly
- Check backend is running and accessible
- Verify CORS configuration allows frontend origin

## Production Environment Variables Summary

### Backend
```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://host:port/database
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
SPRING_WEB_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### Frontend
```bash
VITE_API_BASE_URL=https://your-backend-domain.com/api
```

