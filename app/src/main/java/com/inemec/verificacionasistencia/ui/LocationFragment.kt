package com.inemec.verificacionasistencia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.inemec.verificacionasistencia.R
import com.inemec.verificacionasistencia.api.ApiClient
import com.inemec.verificacionasistencia.databinding.FragmentLocationBinding
import com.inemec.verificacionasistencia.model.InitResponse
import com.inemec.verificacionasistencia.model.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val cancellationTokenSource = CancellationTokenSource()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Permiso concedido, obtener ubicación
                getCurrentLocation()
            }
            else -> {
                // Permiso denegado
                showLocationError("Permiso de ubicación denegado. No es posible verificar tu ubicación.")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.retryLocationButton.setOnClickListener {
            checkLocationPermission()
        }

        // Verificar permisos de ubicación al iniciar
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        binding.locationProgressBar.visibility = View.VISIBLE
        binding.locationStatusTextView.text = "Verificando permisos de ubicación..."
        binding.locationErrorTextView.visibility = View.GONE
        binding.retryLocationButton.visibility = View.GONE

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido
                getCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Mostrar explicación sobre por qué necesitamos permiso
                showLocationError("Se requiere acceso a la ubicación para verificar tu posición.")
                binding.retryLocationButton.visibility = View.VISIBLE
            }
            else -> {
                // Solicitar permiso
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentLocation() {
        binding.locationStatusTextView.text = "Obteniendo tu ubicación..."

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    // Guardar ubicación
                    UserData.latitude = location.latitude
                    UserData.longitude = location.longitude

                    binding.locationStatusTextView.text = "Ubicación obtenida. Verificando..."

                    // Verificar con la API
                    verifyLocation(location.latitude, location.longitude)
                } else {
                    showLocationError("No se pudo obtener la ubicación. Intenta de nuevo.")
                }
            }.addOnFailureListener { e ->
                showLocationError("Error al obtener ubicación: ${e.message}")
            }
        } catch (e: SecurityException) {
            showLocationError("Error de permisos: ${e.message}")
        }
    }

    private fun verifyLocation(latitude: Double, longitude: Double) {
        val call = ApiClient.apiService.verifyInit(
            UserData.cedula,
            latitude,
            longitude,
            UserData.tipoRegistro
        )

        call.enqueue(object : Callback<InitResponse> {
            override fun onResponse(call: Call<InitResponse>, response: Response<InitResponse>) {
                binding.locationProgressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val initResponse = response.body()

                    if (initResponse != null && initResponse.valid) {
                        // Ubicación válida
                        UserData.sessionToken = initResponse.session_token ?: ""

                        binding.locationStatusTextView.text = "¡Hola ${initResponse.nombre}! Ubicación verificada."
                        binding.locationErrorTextView.visibility = View.GONE

                        // Esperar 2 segundos y navegar a la cámara
                        binding.root.postDelayed({
                            findNavController().navigate(R.id.action_locationFragment_to_cameraFragment)
                        }, 2000)
                    } else {
                        // Ubicación no válida
                        showLocationError(initResponse?.mensaje ?: "Ubicación fuera del área permitida.")
                    }
                } else {
                    showLocationError("Error en la verificación: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<InitResponse>, t: Throwable) {
                binding.locationProgressBar.visibility = View.GONE
                showLocationError("Error de conexión: ${t.message}")
            }
        })
    }

    private fun showLocationError(message: String) {
        binding.locationProgressBar.visibility = View.GONE
        binding.locationStatusTextView.text = "Error al verificar ubicación."
        binding.locationErrorTextView.text = message
        binding.locationErrorTextView.visibility = View.VISIBLE
        binding.retryLocationButton.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancellationTokenSource.cancel()
        _binding = null
    }
}