package com.inemec.verificacionasistencia.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.inemec.verificacionasistencia.R

class CommentDialog(
    private val onCommentProvided: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_comment, null)

        val commentEditText = view.findViewById<EditText>(R.id.commentEditText)

        builder.setView(view)
            .setTitle("Registro fuera de ubicación")
            .setPositiveButton("Enviar") { _, _ ->
                val comment = commentEditText.text.toString().trim()
                if (comment.isNotEmpty()) {
                    onCommentProvided(comment)
                } else {
                    // Si está vacío, enviar un comentario por defecto
                    onCommentProvided("Sin comentario")
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(false) // No permitir cerrar el diálogo sin responder

        return builder.create()
    }
}