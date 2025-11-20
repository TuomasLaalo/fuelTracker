import { useEffect, useState } from 'react'
import {
  Box,
  Typography,
  Paper,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  CircularProgress,
  Grid,
  Card,
  CardContent,
} from '@mui/material'
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import dayjs from 'dayjs'
import { analyticsService } from '../api/services'
import useDataStore from '../store/dataStore'
import toast from 'react-hot-toast'

export default function Analytics() {
  const { vehicles, fuelEntries, fetchVehicles, fetchFuelEntries } = useDataStore()
  const [selectedVehicle, setSelectedVehicle] = useState('')
  const [consumption, setConsumption] = useState(null)
  const [history, setHistory] = useState([])
  const [monthlyStats, setMonthlyStats] = useState({})
  const [loading, setLoading] = useState(false)
  const [totalStats, setTotalStats] = useState(null)

  useEffect(() => {
    fetchVehicles()
  }, [fetchVehicles])

  // Auto-select vehicle if user has only one
  useEffect(() => {
    if (vehicles.length === 1 && !selectedVehicle) {
      setSelectedVehicle(vehicles[0].id.toString())
    }
  }, [vehicles, selectedVehicle])

  useEffect(() => {
    if (selectedVehicle) {
      loadAnalytics()
      fetchFuelEntries(selectedVehicle)
    }
  }, [selectedVehicle, fetchFuelEntries])

  const loadAnalytics = async () => {
    setLoading(true)
    try {
      const [consResponse, histResponse, monthlyResponse] = await Promise.all([
        analyticsService.getVehicleConsumption(selectedVehicle),
        analyticsService.getVehicleHistory(selectedVehicle),
        analyticsService.getAllMonthlyStats(),
      ])

      setConsumption(consResponse.data)
      setHistory(histResponse.data)
      setMonthlyStats(monthlyResponse.data)
    } catch (error) {
      toast.error('Failed to load analytics')
    } finally {
      setLoading(false)
    }
  }

  // Calculate total statistics from fuel entries
  useEffect(() => {
    if (selectedVehicle && fuelEntries[selectedVehicle]) {
      const entries = fuelEntries[selectedVehicle]
      if (entries && entries.length > 0) {
        // Sort by date (chronological order)
        const sortedEntries = [...entries].sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime))

        const totalLitres = entries.reduce((sum, e) => sum + e.litres, 0)
        const totalCost = entries.reduce((sum, e) => sum + e.totalPrice, 0)
        const avgPricePerLitre = totalLitres > 0 ? totalCost / totalLitres : 0

        // Calculate total distance (only from full tank entries, sorted by date)
        const fullTankEntries = sortedEntries.filter(e => e.fullTank)
        let totalDistance = 0
        let totalFuelForDistance = 0
        
        for (let i = 1; i < fullTankEntries.length; i++) {
          const currentOdometer = fullTankEntries[i].odometer
          const previousOdometer = fullTankEntries[i - 1].odometer
          
          // Check if odometer was reset (decreased significantly) or if it's a small decrease (data error)
          const distance = currentOdometer - previousOdometer
          
          if (distance > 0) {
            // Normal case: odometer increased
            totalDistance += distance
            totalFuelForDistance += fullTankEntries[i].litres
          } else if (distance < -50) {
            // Odometer reset (decreased by more than 50km): use current odometer as distance
            // This is an approximation - we don't know the actual distance before reset
            totalDistance += currentOdometer
            totalFuelForDistance += fullTankEntries[i].litres
          } else if (distance < 0 && distance >= -50) {
            // Small decrease (likely data error): skip this entry
            // Could also be a partial fill-up that was marked as full tank
            console.warn(`Odometer decreased by ${Math.abs(distance)}km between entries, skipping calculation`)
          }
        }

        // Calculate average consumption from full tank entries
        const avgConsumptionFromFullTanks = totalDistance > 0 
          ? (totalFuelForDistance / totalDistance) * 100 
          : 0

        // If not enough full tank entries, calculate from all entries
        let avgConsumptionFromAll = 0
        if (fullTankEntries.length < 2 && sortedEntries.length >= 2) {
          let allDistance = 0
          let allFuel = 0
          for (let i = 1; i < sortedEntries.length; i++) {
            const currentOdometer = sortedEntries[i].odometer
            const previousOdometer = sortedEntries[i - 1].odometer
            const distance = currentOdometer - previousOdometer
            
            if (distance > 0) {
              allDistance += distance
              allFuel += sortedEntries[i].litres
            } else if (distance < -100) {
              // Odometer reset
              allDistance += currentOdometer
              allFuel += sortedEntries[i].litres
            }
          }
          avgConsumptionFromAll = allDistance > 0 ? (allFuel / allDistance) * 100 : 0
        }

        const firstEntry = sortedEntries[0]
        const lastEntry = sortedEntries[sortedEntries.length - 1]
        const totalOdometerDistance = Math.max(0, lastEntry.odometer - firstEntry.odometer)

        // Use full tank consumption if available, otherwise use all entries
        const avgConsumption = avgConsumptionFromFullTanks > 0 
          ? avgConsumptionFromFullTanks 
          : avgConsumptionFromAll

        setTotalStats({
          totalLitres: totalLitres,
          totalCost: totalCost,
          avgPricePerLitre: avgPricePerLitre,
          totalDistance: totalDistance > 0 ? totalDistance : totalOdometerDistance,
          totalOdometerDistance: totalOdometerDistance,
          avgConsumption: avgConsumption,
          entryCount: entries.length,
          fullTankCount: fullTankEntries.length,
          firstEntryDate: firstEntry.dateTime,
          lastEntryDate: lastEntry.dateTime,
        })
      } else {
        setTotalStats(null)
      }
    } else {
      setTotalStats(null)
    }
  }, [selectedVehicle, fuelEntries])

  const historyData = history.map((h) => ({
    date: dayjs(h.fromDate).format('DD.MM'),
    consumption: parseFloat(h.consumptionPer100km.toFixed(2)),
    distance: parseFloat(h.distanceKm.toFixed(0)),
  }))

  const monthlyData = Object.entries(monthlyStats)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([month, stats]) => ({
      month: dayjs(month).format('MMM YYYY'),
      consumption: parseFloat(stats.avgConsumptionPer100km.toFixed(2)),
      totalCost: parseFloat(stats.totalCost.toFixed(2)),
      totalLitres: parseFloat(stats.totalLitres.toFixed(1)),
    }))

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Analytics</Typography>
        {vehicles.length > 1 ? (
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Select Vehicle</InputLabel>
            <Select
              value={selectedVehicle}
              onChange={(e) => setSelectedVehicle(e.target.value)}
              label="Select Vehicle"
            >
              {vehicles.map((v) => (
                <MenuItem key={v.id} value={v.id.toString()}>
                  {v.make} {v.model} ({v.licensePlate})
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        ) : vehicles.length === 1 ? (
          <Typography variant="body1">
            Vehicle: {vehicles[0].make} {vehicles[0].model} ({vehicles[0].licensePlate})
          </Typography>
        ) : null}
      </Box>

      {loading ? (
        <Box display="flex" justifyContent="center" p={3}>
          <CircularProgress />
        </Box>
      ) : !selectedVehicle ? (
        <Paper sx={{ p: 3 }}>
          <Typography color="text.secondary" align="center">
            {vehicles.length === 0 
              ? 'Please add a vehicle first' 
              : 'Please select a vehicle to view analytics'}
          </Typography>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {/* Statistics Cards */}
          <Grid item xs={12} md={3}>
            <Card>
              <CardContent>
                <Typography color="text.secondary" gutterBottom>
                  Average Consumption
                </Typography>
                <Typography variant="h4">
                  {(consumption && consumption > 0) || (totalStats && totalStats.avgConsumption > 0)
                    ? `${((consumption && consumption > 0) ? consumption : totalStats.avgConsumption).toFixed(2)} L/100km`
                    : 'N/A'}
                </Typography>
                {totalStats && (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    {totalStats.fullTankCount === 0 
                      ? 'Mark entries as "Full Tank" for accurate calculation'
                      : totalStats.fullTankCount < 2
                      ? 'Need 2+ full tank entries for calculation. Check that odometer readings are correct.'
                      : totalStats.totalDistance === 0
                      ? '⚠️ Odometer readings may be incorrect (decreasing values detected)'
                      : ''}
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>

          {totalStats && (
            <>
              <Grid item xs={12} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      Total Distance
                    </Typography>
                    <Typography variant="h4">
                      {totalStats.totalDistance > 0 
                        ? `${totalStats.totalDistance.toFixed(0)} km`
                        : totalStats.totalOdometerDistance > 0
                        ? `${totalStats.totalOdometerDistance.toFixed(0)} km`
                        : '0 km'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                      {totalStats.entryCount} entries
                      {totalStats.fullTankCount < 2 && totalStats.fullTankCount > 0 && (
                        <span> ({totalStats.fullTankCount} full tank)</span>
                      )}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      Total Fuel Used
                    </Typography>
                    <Typography variant="h4">
                      {totalStats.totalLitres.toFixed(1)} L
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                      Avg: €{totalStats.avgPricePerLitre.toFixed(3)}/L
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} md={3}>
                <Card>
                  <CardContent>
                    <Typography color="text.secondary" gutterBottom>
                      Total Cost
                    </Typography>
                    <Typography variant="h4">
                      €{totalStats.totalCost.toFixed(2)}
                    </Typography>
                    {totalStats.totalDistance > 0 && (
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        {(totalStats.totalCost / totalStats.totalDistance).toFixed(2)} €/km
                      </Typography>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            </>
          )}

          {/* Consumption History Chart */}

          {historyData.length > 0 && (
            <Grid item xs={12}>
              <Paper sx={{ p: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Consumption History (L/100km)
                </Typography>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Shows consumption between full tank fill-ups
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={historyData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis label={{ value: 'L/100km', angle: -90, position: 'insideLeft' }} />
                    <Tooltip 
                      formatter={(value) => [`${value} L/100km`, 'Consumption']}
                      labelFormatter={(label) => `Date: ${label}`}
                    />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="consumption"
                      stroke="#8884d8"
                      strokeWidth={2}
                      name="L/100km"
                      dot={{ r: 4 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
                {historyData.length > 0 && (
                  <Box mt={2}>
                    <Typography variant="body2" color="text.secondary">
                      Best: {Math.min(...historyData.map(d => d.consumption)).toFixed(2)} L/100km | 
                      Worst: {Math.max(...historyData.map(d => d.consumption)).toFixed(2)} L/100km | 
                      Average: {(historyData.reduce((sum, d) => sum + d.consumption, 0) / historyData.length).toFixed(2)} L/100km
                    </Typography>
                  </Box>
                )}
              </Paper>
            </Grid>
          )}

          {historyData.length === 0 && totalStats && (
            <Grid item xs={12}>
              <Paper sx={{ p: 2 }}>
                <Typography variant="body2" color="text.secondary" align="center">
                  Not enough full tank entries to calculate consumption history. 
                  Mark entries as "Full Tank" for accurate consumption tracking.
                </Typography>
              </Paper>
            </Grid>
          )}

          {monthlyData.length > 0 && (
            <>
              <Grid item xs={12} md={6}>
                <Paper sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>
                    Monthly Consumption (L/100km)
                  </Typography>
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={monthlyData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis label={{ value: 'L/100km', angle: -90, position: 'insideLeft' }} />
                      <Tooltip 
                        formatter={(value) => [`${value} L/100km`, 'Consumption']}
                      />
                      <Legend />
                      <Bar dataKey="consumption" fill="#82ca9d" name="L/100km" />
                    </BarChart>
                  </ResponsiveContainer>
                </Paper>
              </Grid>

              <Grid item xs={12} md={6}>
                <Paper sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>
                    Monthly Costs (€)
                  </Typography>
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={monthlyData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis label={{ value: '€', angle: -90, position: 'insideLeft' }} />
                      <Tooltip 
                        formatter={(value) => [`€${value}`, 'Total Cost']}
                      />
                      <Legend />
                      <Bar dataKey="totalCost" fill="#8884d8" name="Total Cost (€)" />
                    </BarChart>
                  </ResponsiveContainer>
                </Paper>
              </Grid>

              <Grid item xs={12} md={6}>
                <Paper sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>
                    Monthly Fuel Usage (Liters)
                  </Typography>
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={monthlyData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" />
                      <YAxis label={{ value: 'Liters', angle: -90, position: 'insideLeft' }} />
                      <Tooltip 
                        formatter={(value) => [`${value} L`, 'Total Litres']}
                      />
                      <Legend />
                      <Bar dataKey="totalLitres" fill="#ffc658" name="Total Litres (L)" />
                    </BarChart>
                  </ResponsiveContainer>
                </Paper>
              </Grid>
            </>
          )}
        </Grid>
      )}
    </Box>
  )
}

