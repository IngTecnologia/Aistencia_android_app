package com.inemec.verificacionasistencia.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.inemec.verificacionasistencia.R
import com.inemec.verificacionasistencia.api.ApiClient
import com.inemec.verificacionasistencia.databinding.FragmentCameraBinding
import com.inemec.verificacionasistencia.model.UserData
import com.inemec.verificacionasistencia.model.VerifyResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var photoFile: File? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Se requiere permiso de cámara para la verificación facial",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar permiso de cámara
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Configurar botones
        binding.captureButton.setOnClickListener { takePhoto() }
        binding.retakeButton.setOnClickListener { setupForNewPhoto() }
        binding.sendButton.setOnClickListener { sendPhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Image capture
            imageCapture = ImageCapture.Builder()
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create temporary file
        val photoFile = File(
            requireContext().cacheDir,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    this@CameraFragment.photoFile = photoFile
                    showCapturedPhoto(photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(
                        requireContext(),
                        "Error al capturar foto: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun showCapturedPhoto(photoFile: File) {
        // Hide preview and show captured image
        binding.cameraPreviewView.visibility = View.GONE
        binding.capturedImageView.visibility = View.VISIBLE
        binding.capturedImageView.setImageURI(android.net.Uri.fromFile(photoFile))

        // Update buttons
        binding.captureButton.visibility = View.GONE
        binding.retakeButton.visibility = View.VISIBLE
        binding.sendButton.visibility = View.VISIBLE
    }

    private fun setupForNewPhoto() {
        // Show preview and hide captured image
        binding.cameraPreviewView.visibility = View.VISIBLE
        binding.capturedImageView.visibility = View.GONE

        // Update buttons
        binding.captureButton.visibility = View.VISIBLE
        binding.retakeButton.visibility = View.GONE
        binding.sendButton.visibility = View.GONE

        // Delete the previous photo file
        photoFile?.delete()
        photoFile = null
    }

    private fun sendPhoto() {
        val file = photoFile ?: return

        // Verificar si se requiere comentario
        val fueraUbicacion = UserData.fueraUbicacion
        val requiereComentario = UserData.requiereComentario

        if (fueraUbicacion && requiereComentario && (UserData.comentario.isNullOrBlank())) {
            // Mostrar diálogo para solicitar comentario
            showCommentDialog()
            return
        }

        // Create multipart request
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        // Call API
        val call = ApiClient.apiService.verifyFace(
            UserData.cedula,
            UserData.sessionToken,
            imagePart,
            UserData.fueraUbicacion,
            UserData.comentario
        )

        // Show progress
        Toast.makeText(requireContext(), "Enviando verificación...", Toast.LENGTH_SHORT).show()
        binding.sendButton.isEnabled = false

        call.enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                binding.sendButton.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val verifyResponse = response.body()!!

                    // Store in bundle to pass to result fragment
                    val bundle = Bundle().apply {
                        putBoolean("verified", verifyResponse.verified)
                        putString("cedula", verifyResponse.cedula)
                        putString("tipoRegistro", verifyResponse.tipo_registro)
                        putString("timestamp", verifyResponse.timestamp)
                        putString("recordId", verifyResponse.record_id)
                        putBoolean("fueraUbicacion", verifyResponse.fuera_de_ubicacion)
                        putString("comentario", verifyResponse.comentario)
                        putString("ubicacion", verifyResponse.ubicacion_nombre)
                        putString("errorMessage", if (!verifyResponse.verified) "Verificación facial fallida" else null)
                    }

                    // Navigate to result fragment
                    findNavController().navigate(R.id.action_cameraFragment_to_resultFragment, bundle)
                } else {
                    // Handle API error
                    val errorMessage = response.errorBody()?.string() ?: "Error desconocido"

                    val bundle = Bundle().apply {
                        putBoolean("verified", false)
                        putString("errorMessage", "Error en la verificación: $errorMessage")
                    }

                    findNavController().navigate(R.id.action_cameraFragment_to_resultFragment, bundle)
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                binding.sendButton.isEnabled = true

                val bundle = Bundle().apply {
                    putBoolean("verified", false)
                    putString("errorMessage", "Error de conexión: ${t.message}")
                }

                findNavController().navigate(R.id.action_cameraFragment_to_resultFragment, bundle)
            }
        })
        Log.d("TokenDebug", "Enviando token: ${UserData.sessionToken}")
    }

    private fun showCommentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_comment, null)
        val commentEditText = dialogView.findViewById<EditText>(R.id.commentEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Comentario requerido")
            .setMessage("Estás fuera de tu ubicación asignada. Por favor, proporciona un comentario para este registro.")
            .setView(dialogView)
            .setPositiveButton("Enviar") { dialog, _ ->
                val comentario = commentEditText.text.toString()
                if (comentario.isBlank()) {
                    Toast.makeText(requireContext(), "El comentario es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                UserData.comentario = comentario
                dialog.dismiss()
                sendPhoto()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}