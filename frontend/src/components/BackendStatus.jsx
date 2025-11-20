import { useEffect, useState } from 'react'
import { Alert, Box, CircularProgress } from '@mui/material'
import api from '../api/axios'

export default function BackendStatus() {
  const [status, setStatus] = useState('checking')
  const [error, setError] = useState(null)

  useEffect(() => {
    let isMounted = true
    let intervalId = null
    
    const checkBackend = async () => {
      try {
        // Use health check endpoint
        const response = await api.get('/health')
        if (isMounted && response.status === 200) {
          setStatus('connected')
          setError(null)
          // Stop checking if connected
          if (intervalId) {
            clearInterval(intervalId)
            intervalId = null
          }
        }
      } catch (err) {
        if (isMounted) {
          setStatus('disconnected')
          setError(err.message)
          // Start checking periodically if disconnected
          if (!intervalId) {
            intervalId = setInterval(checkBackend, 10000) // Check every 10 seconds
          }
        }
      }
    }

    // Check once on mount
    checkBackend()

    return () => {
      isMounted = false
      if (intervalId) {
        clearInterval(intervalId)
      }
    }
  }, []) // Empty dependency array - only run on mount

  if (status === 'checking') {
    return (
      <Box display="flex" alignItems="center" gap={1} p={1}>
        <CircularProgress size={16} />
        <Alert severity="info" sx={{ flex: 1 }}>
          Checking backend connection...
        </Alert>
      </Box>
    )
  }

  if (status === 'disconnected') {
    return (
      <Alert severity="error" sx={{ m: 1 }}>
        <strong>Backend not connected!</strong> Make sure the Spring Boot server is running on http://localhost:8080
        {error && <div style={{ fontSize: '0.875rem', marginTop: '4px' }}>Error: {error}</div>}
      </Alert>
    )
  }

  return null
}

