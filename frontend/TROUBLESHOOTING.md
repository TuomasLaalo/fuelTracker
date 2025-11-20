# Troubleshooting: Backend Connection

## Problem: "No connection to browser" or "Cannot connect to backend"

### 1. Check if Backend is Running

Make sure your Spring Boot backend is running:
```bash
# In the root directory (where pom.xml is)
mvn spring-boot:run
```

The backend should start on `http://localhost:8080`

### 2. Check Backend Port

Verify the backend is running on port 8080. Check `application.properties`:
```properties
server.port=8080  # (or check if it's set elsewhere)
```

### 3. Check Frontend Port

The frontend should run on port 5173 (Vite default). Check `vite.config.js`:
```js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 4. CORS Configuration

The backend should allow requests from `http://localhost:5173`. Check `SecurityConfig.java`:
- Allowed origins should include `http://localhost:5173`
- CORS should be enabled in SecurityFilterChain

### 5. Test Backend Directly

Test if backend is accessible:
```bash
# In browser or Postman
GET http://localhost:8080/api/users/register
```

Should return 400 (Bad Request) but NOT connection error.

### 6. Check Browser Console

Open browser DevTools (F12) and check:
- **Console tab**: Look for CORS errors or network errors
- **Network tab**: Check if requests are being made and what status they return

### 7. Common Issues

**Issue**: CORS error in browser console
- **Solution**: Make sure backend SecurityConfig allows `http://localhost:5173`

**Issue**: "Network error" or "ERR_CONNECTION_REFUSED"
- **Solution**: Backend is not running. Start it with `mvn spring-boot:run`

**Issue**: Proxy not working
- **Solution**: Make sure you're using `npm run dev` (not building), and Vite proxy is configured

**Issue**: Port already in use
- **Solution**: Change port in `vite.config.js` or stop other services using port 5173/8080

### 8. Quick Test

1. Start backend: `mvn spring-boot:run`
2. Wait for "Started FueltrackerApplication"
3. Start frontend: `cd frontend && npm run dev`
4. Open browser: `http://localhost:5173`
5. Check the red status bar at top - it should show "Checking backend connection..." then disappear if connected

### 9. Still Not Working?

Check:
- Firewall blocking connections
- Antivirus blocking localhost connections
- Other services using ports 8080 or 5173
- Backend logs for errors
- Frontend terminal for errors

