import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import dayjs from 'dayjs'
import {
  Box,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  CircularProgress,
  Select,
  FormControl,
  InputLabel,
  FormHelperText,
  FormControlLabel,
  Switch,
} from '@mui/material'
// Using simple TextField for date-time input instead of date picker
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import useDataStore from '../store/dataStore'
import toast from 'react-hot-toast'

const schema = yup.object({
  vehicleId: yup.number().required('Vehicle is required'),
  dateTime: yup.string().required('Date and time is required'),
  litres: yup.number().positive('Litres must be positive').required('Litres is required'),
  odometer: yup.number().positive('Odometer must be positive').when('useTrip', {
    is: false,
    then: (schema) => schema.required('Odometer is required when not using trip'),
    otherwise: (schema) => schema.nullable(),
  }),
  tripDistance: yup.number().positive('Trip distance must be positive').when('useTrip', {
    is: true,
    then: (schema) => schema.required('Trip distance is required when using trip'),
    otherwise: (schema) => schema.nullable(),
  }),
  useTrip: yup.boolean(),
  pricePerLitre: yup.number().positive('Price per litre must be positive').required('Price per litre is required'),
  totalPrice: yup.number().positive('Total price must be positive').required('Total price is required'),
  location: yup.string(),
  notes: yup.string(),
})

export default function FuelEntries() {
  const { vehicles, fuelEntries, loading, fetchVehicles, fetchFuelEntries, addFuelEntry, updateFuelEntry, deleteFuelEntry } = useDataStore()
  const [selectedVehicle, setSelectedVehicle] = useState('')
  const [open, setOpen] = useState(false)
  const [editingEntry, setEditingEntry] = useState(null)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
  })

  const litres = watch('litres')
  const pricePerLitre = watch('pricePerLitre')
  const useTrip = watch('useTrip')
  const tripDistance = watch('tripDistance')
  const vehicleId = watch('vehicleId')

  useEffect(() => {
    if (litres && pricePerLitre) {
      setValue('totalPrice', (litres * pricePerLitre).toFixed(2))
    }
  }, [litres, pricePerLitre, setValue])

  // Calculate odometer automatically when using trip
  useEffect(() => {
    if (useTrip && tripDistance != null && tripDistance > 0 && vehicleId && selectedVehicle) {
      const vehicle = vehicles.find(v => v.id === parseInt(vehicleId))
      if (!vehicle) return
      
      const entries = fuelEntries[selectedVehicle] || []
      
      let baseOdometer = 0
      if (entries.length === 0) {
        // First entry: use vehicle's initial odometer
        baseOdometer = vehicle.initialOdometer || 0
      } else {
        // Not first entry: use last entry's odometer
        const sortedEntries = [...entries].sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime))
        const lastEntry = sortedEntries[sortedEntries.length - 1]
        baseOdometer = lastEntry.odometer || 0
      }
      
      const calculatedOdometer = baseOdometer + tripDistance
      setValue('odometer', calculatedOdometer, { shouldValidate: false })
    } else if (useTrip && (!tripDistance || tripDistance === 0)) {
      // Clear odometer if trip is enabled but no distance entered yet
      setValue('odometer', null, { shouldValidate: false })
    }
  }, [useTrip, tripDistance, vehicleId, selectedVehicle, vehicles, fuelEntries, setValue])

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
      fetchFuelEntries(selectedVehicle)
    }
  }, [selectedVehicle, fetchFuelEntries])

  const handleOpen = (entry = null) => {
    setEditingEntry(entry)
    if (entry) {
      reset({
        ...entry,
        dateTime: dayjs(entry.dateTime).format('YYYY-MM-DDTHH:mm'),
        vehicleId: entry.vehicleId,
        useTrip: false,
        tripDistance: null,
        odometer: entry.odometer || null,
      })
      setSelectedVehicle(entry.vehicleId.toString())
    } else {
      const vehicle = vehicles.find(v => v.id === parseInt(selectedVehicle))
      const entries = selectedVehicle ? (fuelEntries[selectedVehicle] || []) : []
      const isFirstEntry = entries.length === 0
      
      // Set initial odometer value
      let initialOdometer = null
      if (isFirstEntry && vehicle?.initialOdometer != null) {
        initialOdometer = vehicle.initialOdometer
      } else if (!isFirstEntry) {
        // For subsequent entries, use last entry's odometer
        const sortedEntries = [...entries].sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime))
        const lastEntry = sortedEntries[sortedEntries.length - 1]
        initialOdometer = lastEntry?.odometer || null
      }
      
      reset({
        vehicleId: selectedVehicle ? parseInt(selectedVehicle) : '',
        dateTime: dayjs().format('YYYY-MM-DDTHH:mm'),
        useTrip: false,
        tripDistance: null,
        odometer: initialOdometer,
      })
    }
    setOpen(true)
  }

  const handleClose = () => {
    setOpen(false)
    setEditingEntry(null)
    reset({})
  }

  const onSubmit = async (data) => {
    const submitData = {
      ...data,
      dateTime: data.dateTime ? (typeof data.dateTime === 'string' 
        ? new Date(data.dateTime).toISOString() 
        : dayjs(data.dateTime).toISOString()) : new Date().toISOString(),
      vehicleId: parseInt(data.vehicleId),
      totalPrice: parseFloat(data.totalPrice),
      useTrip: data.useTrip || false,
      tripDistance: data.useTrip ? parseFloat(data.tripDistance) : null,
      odometer: data.useTrip ? parseFloat(data.odometer) : parseFloat(data.odometer),
    }

    const result = editingEntry
      ? await updateFuelEntry(editingEntry.id, submitData)
      : await addFuelEntry(submitData)

    if (result.success) {
      toast.success(editingEntry ? 'Fuel entry updated!' : 'Fuel entry added!')
      handleClose()
      if (selectedVehicle) {
        fetchFuelEntries(selectedVehicle)
      }
    } else {
      toast.error(result.error || 'Operation failed')
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this fuel entry?')) {
      const result = await deleteFuelEntry(id, selectedVehicle)
      if (result.success) {
        toast.success('Fuel entry deleted!')
        fetchFuelEntries(selectedVehicle)
      } else {
        toast.error(result.error || 'Delete failed')
      }
    }
  }

  const entries = selectedVehicle ? (fuelEntries[selectedVehicle] || []) : []

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Fuel Entries</Typography>
        {vehicles.length > 1 && (
          <FormControl sx={{ minWidth: 200, mr: 2 }}>
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
        )}
        {vehicles.length === 1 && (
          <Typography variant="body1" sx={{ mr: 2 }}>
            Vehicle: {vehicles[0].make} {vehicles[0].model} ({vehicles[0].licensePlate})
          </Typography>
        )}
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpen()}
          disabled={!selectedVehicle}
        >
          Add Entry
        </Button>
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
              : 'Please select a vehicle to view fuel entries'}
          </Typography>
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Date</TableCell>
                <TableCell>Litres</TableCell>
                <TableCell>Odometer</TableCell>
                <TableCell>Price/L</TableCell>
                <TableCell>Total</TableCell>
                <TableCell>Location</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {entries.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography color="text.secondary">No fuel entries found</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                entries.map((entry) => (
                  <TableRow key={entry.id}>
                    <TableCell>{dayjs(entry.dateTime).format('DD.MM.YYYY HH:mm')}</TableCell>
                    <TableCell>{entry.litres} L</TableCell>
                    <TableCell>{entry.odometer} km</TableCell>
                    <TableCell>€{entry.pricePerLitre.toFixed(2)}</TableCell>
                    <TableCell>€{entry.totalPrice.toFixed(2)}</TableCell>
                    <TableCell>{entry.location || '-'}</TableCell>
                    <TableCell align="right">
                      <IconButton onClick={() => handleOpen(entry)} size="small">
                        <EditIcon />
                      </IconButton>
                      <IconButton onClick={() => handleDelete(entry.id)} size="small" color="error">
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>{editingEntry ? 'Edit Fuel Entry' : 'Add Fuel Entry'}</DialogTitle>
          <DialogContent>
            <FormControl fullWidth margin="normal" error={!!errors.vehicleId}>
              <InputLabel>Vehicle</InputLabel>
              <Select
                {...register('vehicleId')}
                value={watch('vehicleId') || ''}
                onChange={(e) => setValue('vehicleId', e.target.value)}
                label="Vehicle"
              >
                {vehicles.map((v) => (
                  <MenuItem key={v.id} value={v.id}>
                    {v.make} {v.model} ({v.licensePlate})
                  </MenuItem>
                ))}
              </Select>
              {errors.vehicleId && <FormHelperText>{errors.vehicleId.message}</FormHelperText>}
            </FormControl>

            <TextField
              fullWidth
              label="Date and Time"
              type="datetime-local"
              {...register('dateTime')}
              value={watch('dateTime') ? (typeof watch('dateTime') === 'string' 
                ? watch('dateTime').slice(0, 16) 
                : dayjs(watch('dateTime')).format('YYYY-MM-DDTHH:mm')) : ''}
              onChange={(e) => {
                const value = e.target.value
                setValue('dateTime', value ? dayjs(value).toISOString() : null)
              }}
              error={!!errors.dateTime}
              helperText={errors.dateTime?.message}
              margin="normal"
              InputLabelProps={{
                shrink: true,
              }}
            />

            <TextField
              fullWidth
              label="Litres"
              type="number"
              step="0.01"
              inputProps={{ step: "0.01", min: "0" }}
              {...register('litres', { valueAsNumber: true })}
              error={!!errors.litres}
              helperText={errors.litres?.message || "Enter value with up to 2 decimal places (e.g., 1.35)"}
              margin="normal"
            />
            
            <FormControlLabel
              control={
                <Switch
                  checked={watch('useTrip') || false}
                  onChange={(e) => {
                    const checked = e.target.checked
                    setValue('useTrip', checked)
                    if (!checked) {
                      // When disabling trip, clear trip distance and restore odometer if needed
                      setValue('tripDistance', null)
                      const vehicle = vehicles.find(v => v.id === parseInt(selectedVehicle))
                      const entries = selectedVehicle ? (fuelEntries[selectedVehicle] || []) : []
                      const isFirstEntry = entries.length === 0
                      
                      if (isFirstEntry && vehicle?.initialOdometer != null) {
                        setValue('odometer', vehicle.initialOdometer)
                      } else if (!isFirstEntry) {
                        const sortedEntries = [...entries].sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime))
                        const lastEntry = sortedEntries[sortedEntries.length - 1]
                        if (lastEntry?.odometer) {
                          setValue('odometer', lastEntry.odometer)
                        }
                      }
                    } else {
                      // When enabling trip, clear odometer (it will be calculated)
                      setValue('odometer', null)
                      setValue('tripDistance', null)
                    }
                  }}
                />
              }
              label="Use Trip Meter (instead of Odometer)"
              sx={{ mt: 1, mb: 1 }}
            />
            
            {useTrip ? (
              <>
                <TextField
                  fullWidth
                  label="Trip Distance (km)"
                  type="number"
                  step="0.01"
                  inputProps={{ step: "0.01", min: "0" }}
                  {...register('tripDistance', { 
                    valueAsNumber: true,
                    onChange: (e) => {
                      const value = parseFloat(e.target.value) || 0
                      setValue('tripDistance', value, { shouldValidate: true })
                    }
                  })}
                  value={watch('tripDistance') ?? ''}
                  error={!!errors.tripDistance}
                  helperText={errors.tripDistance?.message || "Distance driven since last fill-up. Odometer will be calculated automatically."}
                  margin="normal"
                />
                {watch('odometer') != null && (
                  <TextField
                    fullWidth
                    label="Calculated Odometer (km)"
                    type="number"
                    value={watch('odometer')?.toFixed(2) || ''}
                    margin="normal"
                    InputProps={{
                      readOnly: true,
                    }}
                    helperText="This will be saved automatically"
                  />
                )}
              </>
            ) : (
              <TextField
                fullWidth
                label="Odometer (km)"
                type="number"
                step="0.01"
                inputProps={{ step: "0.01", min: "0" }}
                {...register('odometer', { valueAsNumber: true })}
                value={watch('odometer') ?? ''}
                error={!!errors.odometer}
                helperText={errors.odometer?.message || (selectedVehicle && vehicles.find(v => v.id === parseInt(selectedVehicle))?.initialOdometer != null && (fuelEntries[selectedVehicle] || []).length === 0 
                  ? `Vehicle initial odometer: ${vehicles.find(v => v.id === parseInt(selectedVehicle)).initialOdometer} km`
                  : "Total distance driven by the vehicle")}
                margin="normal"
              />
            )}
            <TextField
              fullWidth
              label="Price per Litre (€)"
              type="number"
              step="0.001"
              inputProps={{ step: "0.001", min: "0" }}
              {...register('pricePerLitre', { valueAsNumber: true })}
              error={!!errors.pricePerLitre}
              helperText={errors.pricePerLitre?.message || "Enter value with up to 3 decimal places (e.g., 1.666)"}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Total Price (€)"
              type="number"
              step="0.01"
              inputProps={{ step: "0.01", min: "0" }}
              {...register('totalPrice', { valueAsNumber: true })}
              error={!!errors.totalPrice}
              helperText={errors.totalPrice?.message || "Enter value with up to 2 decimal places (e.g., 3.22)"}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Location"
              {...register('location')}
              error={!!errors.location}
              helperText={errors.location?.message}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Notes"
              multiline
              rows={3}
              {...register('notes')}
              error={!!errors.notes}
              helperText={errors.notes?.message}
              margin="normal"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose}>Cancel</Button>
            <Button type="submit" variant="contained">
              {editingEntry ? 'Update' : 'Add'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  )
}

