import { create } from 'zustand'
import { authService } from '../api/services'

const useAuthStore = create((set) => ({
  user: JSON.parse(localStorage.getItem('user')) || null,
  isAuthenticated: !!localStorage.getItem('auth'),
  
  login: async (email, password) => {
    try {
      // Store credentials for Basic Auth
      localStorage.setItem('auth', JSON.stringify({ email, password }))
      
      // Fetch user data
      const response = await authService.getCurrentUser()
      const user = response.data
      
      localStorage.setItem('user', JSON.stringify(user))
      set({ user, isAuthenticated: true })
      
      return { success: true }
    } catch (error) {
      localStorage.removeItem('auth')
      return { success: false, error: error.response?.data?.message || 'Login failed' }
    }
  },
  
  register: async (email, password) => {
    try {
      await authService.register(email, password)
      // Auto login after registration
      return await useAuthStore.getState().login(email, password)
    } catch (error) {
      return { success: false, error: error.response?.data?.message || 'Registration failed' }
    }
  },
  
  logout: () => {
    localStorage.removeItem('auth')
    localStorage.removeItem('user')
    set({ user: null, isAuthenticated: false })
  },
  
  refreshUser: async () => {
    try {
      const response = await authService.getCurrentUser()
      const user = response.data
      localStorage.setItem('user', JSON.stringify(user))
      set({ user })
    } catch (error) {
      useAuthStore.getState().logout()
    }
  },
}))

export default useAuthStore

