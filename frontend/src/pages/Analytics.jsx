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
      const [consResponse, histResponse] = await Promise.all([
        analyticsService.getVehicleConsumption(selectedVehicle),
        analyticsService.getVehicleHistory(selectedVehicle),
      ])

      setConsumption(consResponse.data)
      setHistory(histResponse.data)
    } catch (error) {
      toast.error('Failed to load analytics')
    } finally {
      setLoading(false)
    }
  }

  // Calculate total statistics from fuel entries
  // Backend handles consumption calculation using tank capacity logic
  useEffect(() => {
    if (selectedVehicle && fuelEntries[selectedVehicle]) {
      const entries = fuelEntries[selectedVehicle]
      if (entries && entries.length > 0) {
        // Sort by date for display
        const sortedEntries = [...entries].sort((a, b) => 
          new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime()
        )

        // Calculate basic totals (backend handles consumption)
        const totalLitres = entries.reduce((sum, e) => sum + e.litres, 0)
        const totalCost = entries.reduce((sum, e) => sum + e.totalPrice, 0)
        const avgPricePerLitre = totalLitres > 0 ? totalCost / totalLitres : 0

        const firstEntry = sortedEntries[0]
        const lastEntry = sortedEntries[sortedEntries.length - 1]
        const totalOdometerDistance = Math.max(0, lastEntry.odometer - firstEntry.odometer)

        // Get distance and consumption from backend history data
        const totalDistance = history.reduce((sum, h) => sum + h.distanceKm, 0)
        const consumptionCyclesCount = history.length

        setTotalStats({
          totalLitres: totalLitres,
          totalCost: totalCost,
          avgPricePerLitre: avgPricePerLitre,
          totalDistance: totalDistance,
          totalOdometerDistance: totalOdometerDistance,
          avgConsumption: consumption || 0,
          entryCount: entries.length,
          consumptionCyclesCount: consumptionCyclesCount,
          firstEntryDate: firstEntry.dateTime,
          lastEntryDate: lastEntry.dateTime,
        })
      } else {
        setTotalStats(null)
      }
    } else {
      setTotalStats(null)
    }
  }, [selectedVehicle, fuelEntries, consumption, history])

  // Use backend history data (which should also follow FULL→FULL logic)
  const historyData = history.map((h) => ({
    date: dayjs(h.fromDate).format('DD.MM'),
    consumption: parseFloat(h.consumptionPer100km.toFixed(2)),
    distance: parseFloat(h.distanceKm.toFixed(0)),
  }))

  // Calculate vehicle-specific monthly stats from fuel entries and history
  useEffect(() => {
    if (selectedVehicle && fuelEntries[selectedVehicle]) {
      const entries = fuelEntries[selectedVehicle]
      if (entries && entries.length > 0) {
        // Group entries by month
        const entriesByMonth = {}
        entries.forEach(entry => {
          const monthKey = dayjs(entry.dateTime).format('YYYY-MM')
          if (!entriesByMonth[monthKey]) {
            entriesByMonth[monthKey] = []
          }
          entriesByMonth[monthKey].push(entry)
        })

        // Group history cycles by month (if available)
        const cyclesByMonth = {}
        if (history && history.length > 0) {
          history.forEach(cycle => {
            const monthKey = dayjs(cycle.toDate).format('YYYY-MM')
            if (!cyclesByMonth[monthKey]) {
              cyclesByMonth[monthKey] = []
            }
            cyclesByMonth[monthKey].push(cycle)
          })
        }

        // Calculate stats for each month
        const calculatedStats = {}
        Object.keys(entriesByMonth).forEach(monthKey => {
          const monthEntries = entriesByMonth[monthKey]
          const monthCycles = cyclesByMonth[monthKey] || []
          
          const totalLitres = monthEntries.reduce((sum, e) => sum + e.litres, 0)
          const totalCost = monthEntries.reduce((sum, e) => sum + e.totalPrice, 0)
          
          // Calculate average consumption from cycles in this month
          let avgConsumption = 0
          if (monthCycles.length > 0) {
            const totalConsumption = monthCycles.reduce((sum, c) => sum + c.consumptionPer100km, 0)
            avgConsumption = totalConsumption / monthCycles.length
          }
          
          calculatedStats[monthKey] = {
            avgConsumptionPer100km: avgConsumption,
            totalCost: totalCost,
            totalLitres: totalLitres,
          }
        })
        
        setMonthlyStats(calculatedStats)
      } else {
        setMonthlyStats({})
      }
    } else {
      setMonthlyStats({})
    }
  }, [selectedVehicle, fuelEntries, history])

  // Filter monthly stats to only show months with valid consumption cycles
  const monthlyData = Object.entries(monthlyStats)
    .filter(([_, stats]) => stats.avgConsumptionPer100km > 0) // Only valid consumption
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
                    {totalStats.consumptionCyclesCount === 0 
                      ? 'Need more fuel entries to calculate consumption (tank capacity based)'
                      : totalStats.consumptionCyclesCount === 1
                      ? '1 valid consumption cycle'
                      : `${totalStats.consumptionCyclesCount} valid consumption cycles`}
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
                        : '0 km'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                      {totalStats.entryCount} entries
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
                  Shows consumption cycles detected automatically based on tank capacity
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
                  Not enough fuel entries to calculate consumption history. 
                  Consumption is calculated automatically based on tank capacity.
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
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Calculated from valid consumption cycles (tank capacity based)
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
