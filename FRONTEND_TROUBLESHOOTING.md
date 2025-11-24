# Frontend Troubleshooting Guide

## Quick Checks

### 1. Verify Backend is Running
- Backend should be running on **http://localhost:8080**
- Test: Open `http://localhost:8080/api/health` in browser
- Should return: `{"status":"UP"}` or similar

### 2. Check Frontend Terminal Output
When you run `npm run dev`, you should see:
```
  VITE v7.x.x  ready in xxx ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

**If you see errors:**
- **Port 5173 already in use**: Change port in `vite.config.js` or stop other service
- **Module not found**: Run `npm install` in the `frontend` directory
- **Syntax errors**: Check the error message for the file

### 3. Check Browser Console
Open browser DevTools (F12) and check:
- **Console tab**: Look for JavaScript errors
- **Network tab**: Check if requests to `/api/*` are being made
- **Errors**: Red text indicates problems

### 4. Common Issues

#### Issue: "Cannot GET /" or blank page
**Solution**: 
- Make sure you're accessing `http://localhost:5173` (not 8080)
- Check browser console for errors
- Verify `index.html` exists in `frontend/` directory

#### Issue: "Failed to fetch" or CORS errors
**Solution**:
- Verify backend is running on port 8080
- Check CORS configuration in `SecurityConfig.java`
- Verify `application-dev.properties` has correct CORS origins

#### Issue: Port 5173 already in use
**Solution**:
```javascript
// In vite.config.js, change port:
server: {
  port: 5174,  // or any other available port
  ...
}
```

#### Issue: Frontend shows "Checking backend connection..." forever
**Solution**:
- Backend might not be running
- Check `http://localhost:8080/api/health` directly
- Verify backend logs for errors

### 5. Step-by-Step Verification

1. **Start Backend**:
   ```bash
   # In project root
   mvn spring-boot:run
   # Wait for "Started FueltrackerApplication"
   ```

2. **Test Backend**:
   - Open browser: `http://localhost:8080/api/health`
   - Should see JSON response

3. **Start Frontend**:
   ```bash
   # In frontend directory
   cd frontend
   npm install  # If not done already
   npm run dev
   ```

4. **Open Frontend**:
   - Browser: `http://localhost:5173`
   - Should see login page or redirect to login

### 6. Check Dependencies

If frontend won't start, reinstall dependencies:
```bash
cd frontend
rm -rf node_modules package-lock.json  # On Windows: rmdir /s node_modules
npm install
npm run dev
```

### 7. Verify Proxy Configuration

The `vite.config.js` should have:
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

This means:
- Frontend requests to `/api/*` are proxied to `http://localhost:8080/api/*`
- Backend must be running on port 8080

### 8. Test Backend Connection Manually

Open browser console on `http://localhost:5173` and run:
```javascript
fetch('/api/health')
  .then(r => r.json())
  .then(console.log)
  .catch(console.error)
```

Should return: `{status: "UP"}` or similar

## Still Not Working?

1. **Check both terminals** (backend and frontend) for error messages
2. **Check browser console** (F12) for JavaScript errors
3. **Verify ports**: Backend 8080, Frontend 5173
4. **Try different browser** or clear cache
5. **Check firewall** isn't blocking localhost connections

