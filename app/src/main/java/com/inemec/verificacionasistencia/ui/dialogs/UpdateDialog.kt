package com.inemec.verificacionasistencia.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.inemec.verificacionasistencia.R

class UpdateDialog(
    context: Context,
    private val message: String,
    private val forceUpdate: Boolean,
    private val downloadUrl: String?
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update)

        setCancelable(!forceUpdate)
        setCanceledOnTouchOutside(!forceUpdate)

        val messageTextView = findViewById<TextView>(R.id.updateMessageTextView)
        val updateButton = findViewById<Button>(R.id.updateButton)
        val laterButton = findViewById<Button>(R.id.laterButton)

        messageTextView.text = message

        updateButton.setOnClickListener {
            downloadUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            dismiss()
        }

        if (forceUpdate) {
            laterButton.visibility = Button.GONE
        } else {
            laterButton.setOnClickListener {
                dismiss()
            }
        }
    }
}