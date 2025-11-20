import api from './axios'

// Auth
export const authService = {
  register: (email, password) => 
    api.post('/users/register', { email, password }),
  
  getCurrentUser: () => 
    api.get('/users/me'),
}

// Vehicles
export const vehicleService = {
  getAll: () => 
    api.get('/vehicles'),
  
  getById: (id) => 
    api.get(`/vehicles/${id}`),
  
  create: (data) => 
    api.post('/vehicles', data),
  
  update: (id, data) => 
    api.put(`/vehicles/${id}`, data),
  
  delete: (id) => 
    api.delete(`/vehicles/${id}`),
}

// Fuel Entries
export const fuelEntryService = {
  getByVehicle: (vehicleId) => 
    api.get(`/fuelentries/vehicle/${vehicleId}`),
  
  getById: (id) => 
    api.get(`/fuelentries/${id}`),
  
  create: (data) => 
    api.post('/fuelentries', data),
  
  update: (id, data) => 
    api.put(`/fuelentries/${id}`, data),
  
  delete: (id) => 
    api.delete(`/fuelentries/${id}`),
}

// Analytics
export const analyticsService = {
  getVehicleConsumption: (vehicleId) => 
    api.get(`/analytics/vehicles/${vehicleId}/consumption`),
  
  getVehicleHistory: (vehicleId) => 
    api.get(`/analytics/vehicles/${vehicleId}/history`),
  
  getMonthlyStats: (year, month) => 
    api.get(`/analytics/monthly/${year}/${month}`),
  
  getAllMonthlyStats: () => 
    api.get('/analytics/monthly'),
}

