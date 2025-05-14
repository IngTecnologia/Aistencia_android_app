package com.inemec.verificacionasistencia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview con configuración de zoom (crop) para mostrar solo el área del óvalo
            val preview = Preview.Builder()
                .build()
                .also { preview ->
                    preview.setSurfaceProvider { surfaceRequest ->
                        // Configurar el surface provider con transformación para hacer zoom
                        val surfaceProvider = binding.cameraPreviewView.surfaceProvider
                        surfaceProvider?.let { provider ->
                            provider.onSurfaceRequested(surfaceRequest)

                            // Aplicar zoom digital a la vista previa
                            // Este zoom solo afecta lo que VE el usuario, no la captura
                            binding.cameraPreviewView.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER

                            // Configurar el zoom en la vista previa después de que esté lista
                            binding.cameraPreviewView.post {
                                applyPreviewZoom()
                            }
                        }
                    }
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Image capture - SIN ZOOM, resolución completa para DeepFace
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(android.util.Size(1280, 960))
                .build()

            try {
                cameraProvider.unbindAll()

                // Bind solo preview e imageCapture
                val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

                // Obtener control de la cámara para configuraciones adicionales si es necesario
                val cameraControl = camera.cameraControl

            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun applyPreviewZoom() {
        // Aplicar zoom digital más moderado a la vista previa
        // Reducido de 2.0x a 1.5x para mayor comodidad
        binding.cameraPreviewView.scaleX = 1.5f  // Zoom de 1.5x (reducido)
        binding.cameraPreviewView.scaleY = 1.5f  // Zoom de 1.5x (reducido)

        // Opcional: Ajustar la posición si es necesario
        binding.cameraPreviewView.translationX = 0f
        binding.cameraPreviewView.translationY = 0f
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            requireContext().cacheDir,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // La imagen se captura SIN ZOOM, tamaño completo
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    this@CameraFragment.photoFile = photoFile
                    showProcessingScreen() // Mostrar pantalla de procesamiento en lugar de la imagen
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

    private fun showProcessingScreen() {
        // Ocultar vista previa y overlay, mostrar pantalla de procesamiento
        binding.cameraPreviewView.visibility = View.GONE
        binding.faceGuideOverlay.visibility = View.GONE
        binding.capturedImageView.visibility = View.GONE

        // Mostrar layout de procesamiento completo
        binding.processingLayout.visibility = View.VISIBLE
        binding.processingTextView.text = "Procesando imagen facial..."

        // Ocultar botones de captura, mostrar botón de reintento
        binding.captureButton.visibility = View.GONE
        binding.retakeButton.visibility = View.VISIBLE
        binding.sendButton.visibility = View.GONE

        // Automáticamente procesar y enviar la imagen
        sendPhoto()
    }

    private fun setupForNewPhoto() {
        // Volver a la vista previa con zoom
        binding.cameraPreviewView.visibility = View.VISIBLE
        binding.faceGuideOverlay.visibility = View.VISIBLE
        binding.capturedImageView.visibility = View.GONE

        // Ocultar layout de procesamiento
        binding.processingLayout.visibility = View.GONE

        // Reaplicar el zoom a la vista previa
        applyPreviewZoom()

        binding.captureButton.visibility = View.VISIBLE
        binding.retakeButton.visibility = View.GONE
        binding.sendButton.visibility = View.GONE

        photoFile?.delete()
        photoFile = null
    }

    private fun processImageForSending(originalFile: File): File {
        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath) ?: return originalFile

        // Aplicar compresión básica para optimizar el envío
        val ratio = minOf(
            MAX_IMAGE_WIDTH.toFloat() / bitmap.width,
            MAX_IMAGE_HEIGHT.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // CORRECCIÓN DE ROTACIÓN para cámara frontal
        val matrix = Matrix()
        // Para cámara frontal: 90 grados en lugar de 270
        matrix.postRotate(0f) // Cambiado de 270f a 90f
        matrix.postScale(-1f, 1f) // Voltear horizontalmente para cámara frontal
        val finalBitmap = Bitmap.createBitmap(
            resizedBitmap, 0, 0, resizedBitmap.width, resizedBitmap.height, matrix, true
        )

        // Guardar imagen procesada
        val processedFile = File(
            requireContext().cacheDir,
            "verification_${System.currentTimeMillis()}.jpg"
        )

        try {
            FileOutputStream(processedFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            bitmap.recycle()
            resizedBitmap.recycle()
            finalBitmap.recycle()

            Log.d(TAG, "Imagen procesada: ${originalFile.length()} -> ${processedFile.length()} bytes")
            return processedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar imagen", e)
            return originalFile
        }
    }

    private fun sendPhoto() {
        val file = photoFile ?: return

        // Actualizar texto de procesamiento
        binding.processingTextView.text = "Enviando para verificación..."

        // Procesar la imagen (solo compresión y rotación, la imagen completa)
        val processedFile = processImageForSending(file)

        val requestFile = processedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", processedFile.name, requestFile)

        val call = ApiClient.apiService.verifyFace(
            UserData.cedula,
            UserData.sessionToken,
            imagePart
        )

        call.enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val verifyResponse = response.body()!!

                    val bundle = Bundle().apply {
                        putBoolean("verified", verifyResponse.verified)
                        putString("cedula", verifyResponse.cedula)
                        putString("tipoRegistro", verifyResponse.tipo_registro)
                        putString("timestamp", verifyResponse.timestamp)
                        putString("recordId", verifyResponse.record_id)
                        putString("errorMessage", if (!verifyResponse.verified) "Verificación facial fallida" else null)
                    }

                    findNavController().navigate(R.id.action_cameraFragment_to_resultFragment, bundle)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                    val bundle = Bundle().apply {
                        putBoolean("verified", false)
                        putString("errorMessage", "Error en la verificación: $errorMessage")
                    }
                    findNavController().navigate(R.id.action_cameraFragment_to_resultFragment, bundle)
                }

                processedFile.delete()
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                val bundle = Bundle().apply {
                    putBoolean("verified", false)
                    putString("errorMessage", "Error de conexión: ${t.message}")
                }
                findNavController().navigate(R.id.action_cameraFragment_to_resultFragment, bundle)
                processedFile.delete()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        photoFile?.delete()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val MAX_IMAGE_WIDTH = 640
        private const val MAX_IMAGE_HEIGHT = 640
        private const val JPEG_QUALITY = 75
    }
}