import axios from 'axios'
import toast from 'react-hot-toast'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - add Basic Auth
api.interceptors.request.use(
  (config) => {
    const auth = localStorage.getItem('auth')
    if (auth) {
      const { email, password } = JSON.parse(auth)
      const credentials = btoa(`${email}:${password}`)
      config.headers.Authorization = `Basic ${credentials}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      
      if (status === 401) {
        toast.error('Authentication failed. Please login again.')
        localStorage.removeItem('auth')
        localStorage.removeItem('user')
        window.location.href = '/login'
      } else if (status === 403) {
        toast.error('You do not have permission to perform this action.')
      } else if (status === 400) {
        const message = data?.message || data?.error || 'Bad request'
        toast.error(message)
      } else if (status >= 500) {
        toast.error('Server error. Please try again later.')
      }
    } else if (error.request) {
      // Network error - backend might not be running
      console.error('Network error:', error.request)
      toast.error('Cannot connect to backend. Make sure the server is running on http://localhost:8080')
    } else {
      console.error('Request setup error:', error.message)
      toast.error('An unexpected error occurred.')
    }
    
    return Promise.reject(error)
  }
)

export default api

