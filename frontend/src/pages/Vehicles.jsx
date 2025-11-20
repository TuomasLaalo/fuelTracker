import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
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
  CircularProgress,
  Select,
  FormControl,
  InputLabel,
  MenuItem,
  FormHelperText,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import useDataStore from '../store/dataStore'
import toast from 'react-hot-toast'

const schema = yup.object({
  make: yup.string().required('Make is required'),
  model: yup.string().required('Model is required'),
  fuelType: yup.string().oneOf(['Petrol', 'Diesel'], 'Fuel type must be Petrol or Diesel').required('Fuel type is required'),
  manufacturingYear: yup
    .number()
    .min(1900, 'Year must be at least 1900')
    .max(2100, 'Year cannot be more than 2100')
    .required('Manufacturing year is required'),
  licensePlate: yup.string().required('License plate is required'),
  initialOdometer: yup
    .number()
    .positive('Initial odometer must be positive')
    .required('Initial odometer is required'),
})

export default function Vehicles() {
  const { vehicles, loading, fetchVehicles, addVehicle, updateVehicle, deleteVehicle } = useDataStore()
  const [open, setOpen] = useState(false)
  const [editingVehicle, setEditingVehicle] = useState(null)

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

  useEffect(() => {
    fetchVehicles()
  }, [fetchVehicles])

  const handleOpen = (vehicle = null) => {
    setEditingVehicle(vehicle)
    if (vehicle) {
      reset({
        ...vehicle,
        initialOdometer: vehicle.initialOdometer ?? null,
      })
    } else {
      reset({})
    }
    setOpen(true)
  }

  const handleClose = () => {
    setOpen(false)
    setEditingVehicle(null)
    reset({})
  }

  const onSubmit = async (data) => {
    // Ensure initialOdometer is sent as a number
    const submitData = {
      ...data,
      initialOdometer: data.initialOdometer != null ? parseFloat(data.initialOdometer) : null,
    }
    
    const result = editingVehicle
      ? await updateVehicle(editingVehicle.id, submitData)
      : await addVehicle(submitData)

    if (result.success) {
      toast.success(editingVehicle ? 'Vehicle updated!' : 'Vehicle added!')
      handleClose()
      fetchVehicles()
    } else {
      toast.error(result.error || 'Operation failed')
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this vehicle?')) {
      const result = await deleteVehicle(id)
      if (result.success) {
        toast.success('Vehicle deleted!')
        fetchVehicles()
      } else {
        toast.error(result.error || 'Delete failed')
      }
    }
  }

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" p={3}>
        <CircularProgress />
      </Box>
    )
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Vehicles</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpen()}
        >
          Add Vehicle
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Make</TableCell>
              <TableCell>Model</TableCell>
              <TableCell>Fuel Type</TableCell>
              <TableCell>Year</TableCell>
              <TableCell>License Plate</TableCell>
              <TableCell>Initial Odometer</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {vehicles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography color="text.secondary">No vehicles found</Typography>
                </TableCell>
              </TableRow>
            ) : (
              vehicles.map((vehicle) => (
                <TableRow key={vehicle.id}>
                  <TableCell>{vehicle.make}</TableCell>
                  <TableCell>{vehicle.model}</TableCell>
                  <TableCell>{vehicle.fuelType}</TableCell>
                  <TableCell>{vehicle.manufacturingYear}</TableCell>
                  <TableCell>{vehicle.licensePlate}</TableCell>
                  <TableCell>{vehicle.initialOdometer != null ? `${vehicle.initialOdometer.toLocaleString()} km` : '-'}</TableCell>
                  <TableCell align="right">
                    <IconButton onClick={() => handleOpen(vehicle)} size="small">
                      <EditIcon />
                    </IconButton>
                    <IconButton onClick={() => handleDelete(vehicle.id)} size="small" color="error">
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>{editingVehicle ? 'Edit Vehicle' : 'Add Vehicle'}</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              label="Make"
              {...register('make')}
              error={!!errors.make}
              helperText={errors.make?.message}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Model"
              {...register('model')}
              error={!!errors.model}
              helperText={errors.model?.message}
              margin="normal"
            />
            <FormControl fullWidth margin="normal" error={!!errors.fuelType}>
              <InputLabel>Fuel Type</InputLabel>
              <Select
                {...register('fuelType')}
                value={watch('fuelType') || ''}
                onChange={(e) => setValue('fuelType', e.target.value)}
                label="Fuel Type"
              >
                <MenuItem value="Petrol">Petrol</MenuItem>
                <MenuItem value="Diesel">Diesel</MenuItem>
              </Select>
              {errors.fuelType && <FormHelperText>{errors.fuelType.message}</FormHelperText>}
            </FormControl>
            
            <FormControl fullWidth margin="normal" error={!!errors.manufacturingYear}>
              <InputLabel>Manufacturing Year</InputLabel>
              <Select
                {...register('manufacturingYear', { valueAsNumber: true })}
                value={watch('manufacturingYear') || ''}
                onChange={(e) => setValue('manufacturingYear', parseInt(e.target.value))}
                label="Manufacturing Year"
              >
                {Array.from({ length: 2100 - 1900 + 1 }, (_, i) => 1900 + i).reverse().map((year) => (
                  <MenuItem key={year} value={year}>
                    {year}
                  </MenuItem>
                ))}
              </Select>
              {errors.manufacturingYear && <FormHelperText>{errors.manufacturingYear.message}</FormHelperText>}
            </FormControl>
            <TextField
              fullWidth
              label="License Plate"
              {...register('licensePlate')}
              error={!!errors.licensePlate}
              helperText={errors.licensePlate?.message}
              margin="normal"
            />
            
            <TextField
              fullWidth
              label="Initial Odometer (km)"
              type="number"
              step="0.01"
              inputProps={{ step: "0.01", min: "0" }}
              {...register('initialOdometer', { valueAsNumber: true })}
              value={watch('initialOdometer') ?? ''}
              error={!!errors.initialOdometer}
              helperText={errors.initialOdometer?.message || "Current odometer reading when adding this vehicle"}
              margin="normal"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose}>Cancel</Button>
            <Button type="submit" variant="contained">
              {editingVehicle ? 'Update' : 'Add'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  )
}

