import { Routes, Route, Navigate } from 'react-router-dom'
import { useEffect } from 'react'
import useAuthStore from './store/authStore'
import Layout from './components/Layout'
import Login from './pages/Login'
import Register from './pages/Register'
import Vehicles from './pages/Vehicles'
import FuelEntries from './pages/FuelEntries'
import Analytics from './pages/Analytics'

function App() {
  const { isAuthenticated, refreshUser } = useAuthStore()

  useEffect(() => {
    if (isAuthenticated) {
      refreshUser()
    }
  }, [isAuthenticated, refreshUser])

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/vehicles" replace />} />
        <Route path="vehicles" element={<Vehicles />} />
        <Route path="fuel-entries" element={<FuelEntries />} />
        <Route path="analytics" element={<Analytics />} />
      </Route>
    </Routes>
  )
}

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuthStore()
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  
  return children
}

export default App

