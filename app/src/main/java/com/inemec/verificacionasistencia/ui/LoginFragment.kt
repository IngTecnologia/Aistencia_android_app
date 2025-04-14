package com.inemec.verificacionasistencia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.inemec.verificacionasistencia.R
import com.inemec.verificacionasistencia.databinding.FragmentLoginBinding
import com.inemec.verificacionasistencia.model.UserData

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continuarButton.setOnClickListener {
            if (validateInput()) {
                val cedula = binding.cedulaEditText.text.toString()
                val tipoRegistro = if (binding.entradaRadioButton.isChecked) "entrada" else "salida"

                // Guardar datos para uso en otros fragmentos
                UserData.cedula = cedula
                UserData.tipoRegistro = tipoRegistro

                // Navegar al fragmento de ubicación
                findNavController().navigate(R.id.action_loginFragment_to_locationFragment)
            }
        }
    }

    private fun validateInput(): Boolean {
        val cedula = binding.cedulaEditText.text.toString()

        if (cedula.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, ingresa tu número de cédula", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cedula.length < 5) {
            Toast.makeText(requireContext(), "El número de cédula es demasiado corto", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}