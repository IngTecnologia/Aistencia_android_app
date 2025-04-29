package com.inemec.verificacionasistencia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.inemec.verificacionasistencia.R
import com.inemec.verificacionasistencia.databinding.FragmentResultBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialmente, mostrar indicador de carga
        binding.resultProgressBar.visibility = View.VISIBLE
        binding.processingTextView.visibility = View.VISIBLE
        binding.successLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.newVerificationButton.visibility = View.GONE

        // Procesar argumentos
        arguments?.let { args ->
            val verified = args.getBoolean("verified", false)

            // Ocultar indicador de carga
            binding.resultProgressBar.visibility = View.GONE
            binding.processingTextView.visibility = View.GONE

            if (verified) {
                // Mostrar resultado exitoso
                binding.successLayout.visibility = View.VISIBLE

                // Llenar datos
                binding.resultCedulaTextView.text = args.getString("cedula", "")
                binding.resultTipoTextView.text = formatTipoRegistro(args.getString("tipoRegistro", ""))
                binding.resultTimestampTextView.text = formatTimestamp(args.getString("timestamp", ""))

                // Información de ubicación
                val fueraUbicacion = args.getBoolean("fueraUbicacion", false)
                val ubicacion = args.getString("ubicacion", "Principal")

                if (fueraUbicacion) {
                    binding.resultUbicacionTextView.text = "$ubicacion (Fuera de ubicación)"
                    binding.resultUbicacionTextView.setTextColor(resources.getColor(R.color.warning_color, null))
                } else {
                    binding.resultUbicacionTextView.text = ubicacion
                    binding.resultUbicacionTextView.setTextColor(binding.resultCedulaTextView.currentTextColor)
                }

                // Comentario si existe
                val comentario = args.getString("comentario", "")
                if (fueraUbicacion && comentario.isNotBlank()) {
                    binding.comentarioLayout.visibility = View.VISIBLE
                    binding.resultComentarioTextView.text = comentario
                } else {
                    binding.comentarioLayout.visibility = View.GONE
                }
            } else {
                // Mostrar error
                binding.errorLayout.visibility = View.VISIBLE
                binding.errorMessageTextView.text = args.getString("errorMessage", "Verificación fallida")
            }

            // Mostrar botón para nueva verificación
            binding.newVerificationButton.visibility = View.VISIBLE
        }

        // Configurar botón para volver al inicio
        binding.newVerificationButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_loginFragment)
        }
    }

    private fun formatTipoRegistro(tipo: String): String {
        return when (tipo.lowercase()) {
            "entrada" -> "Entrada"
            "salida" -> "Salida"
            else -> tipo
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            timestamp
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}