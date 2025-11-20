import { create } from 'zustand'
import { vehicleService, fuelEntryService } from '../api/services'

const useDataStore = create((set) => ({
  vehicles: [],
  fuelEntries: {},
  loading: false,
  error: null,
  
  // Vehicles
  fetchVehicles: async () => {
    set({ loading: true, error: null })
    try {
      const response = await vehicleService.getAll()
      set({ vehicles: response.data, loading: false })
    } catch (error) {
      set({ error: error.message, loading: false })
    }
  },
  
  addVehicle: async (data) => {
    try {
      const response = await vehicleService.create(data)
      set((state) => ({
        vehicles: [...state.vehicles, response.data]
      }))
      return { success: true, data: response.data }
    } catch (error) {
      return { success: false, error: error.response?.data?.message }
    }
  },
  
  updateVehicle: async (id, data) => {
    try {
      const response = await vehicleService.update(id, data)
      set((state) => ({
        vehicles: state.vehicles.map(v => v.id === id ? response.data : v)
      }))
      return { success: true, data: response.data }
    } catch (error) {
      return { success: false, error: error.response?.data?.message }
    }
  },
  
  deleteVehicle: async (id) => {
    try {
      await vehicleService.delete(id)
      set((state) => ({
        vehicles: state.vehicles.filter(v => v.id !== id),
        fuelEntries: Object.fromEntries(
          Object.entries(state.fuelEntries).filter(([key]) => key !== id.toString())
        )
      }))
      return { success: true }
    } catch (error) {
      return { success: false, error: error.response?.data?.message }
    }
  },
  
  // Fuel Entries
  fetchFuelEntries: async (vehicleId) => {
    set({ loading: true, error: null })
    try {
      const response = await fuelEntryService.getByVehicle(vehicleId)
      set((state) => ({
        fuelEntries: {
          ...state.fuelEntries,
          [vehicleId]: response.data
        },
        loading: false
      }))
    } catch (error) {
      set({ error: error.message, loading: false })
    }
  },
  
  addFuelEntry: async (data) => {
    try {
      const response = await fuelEntryService.create(data)
      const vehicleId = response.data.vehicleId
      set((state) => ({
        fuelEntries: {
          ...state.fuelEntries,
          [vehicleId]: [...(state.fuelEntries[vehicleId] || []), response.data]
        }
      }))
      return { success: true, data: response.data }
    } catch (error) {
      return { success: false, error: error.response?.data?.message }
    }
  },
  
  updateFuelEntry: async (id, data) => {
    try {
      const response = await fuelEntryService.update(id, data)
      const vehicleId = response.data.vehicleId
      set((state) => ({
        fuelEntries: {
          ...state.fuelEntries,
          [vehicleId]: (state.fuelEntries[vehicleId] || []).map(e => 
            e.id === id ? response.data : e
          )
        }
      }))
      return { success: true, data: response.data }
    } catch (error) {
      return { success: false, error: error.response?.data?.message }
    }
  },
  
  deleteFuelEntry: async (id, vehicleId) => {
    try {
      await fuelEntryService.delete(id)
      set((state) => ({
        fuelEntries: {
          ...state.fuelEntries,
          [vehicleId]: (state.fuelEntries[vehicleId] || []).filter(e => e.id !== id)
        }
      }))
      return { success: true }
    } catch (error) {
      return { success: false, error: error.response?.data?.message }
    }
  },
}))

export default useDataStore

