package com.inemec.verificacionasistencia.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class FaceGuideOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint().apply {
        color = Color.BLACK // Completamente opaco para ocultar lo que está fuera
        style = Paint.Style.FILL
    }

    private val guidePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val instructionPaint = Paint().apply {
        color = Color.WHITE
        textSize = 16f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private var centerX = 0f
    private var centerY = 0f
    private var ovalWidth = 0f
    private var ovalHeight = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f

        // Calcular el tamaño del óvalo (aproximadamente 60% del ancho de la pantalla)
        ovalWidth = w * 0.6f
        ovalHeight = ovalWidth * 1.2f // Hacer el óvalo un poco más alto que ancho
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // PASO 1: Llenar toda la pantalla con negro
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // PASO 2: Crear un "agujero" oval transparente
        // Configurar paint para modo "clear" (crear transparencia)
        val clearPaint = Paint().apply {
            color = Color.TRANSPARENT
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        // Dibujar óvalo transparente (esto "borra" el negro en esa área)
        canvas.drawOval(
            centerX - ovalWidth / 2,
            centerY - ovalHeight / 2,
            centerX + ovalWidth / 2,
            centerY + ovalHeight / 2,
            clearPaint
        )

        // PASO 3: Dibujar el contorno del óvalo como guía
        canvas.drawOval(
            centerX - ovalWidth / 2,
            centerY - ovalHeight / 2,
            centerX + ovalWidth / 2,
            centerY + ovalHeight / 2,
            guidePaint
        )

        // PASO 4: Dibujar texto de instrucción fuera del óvalo
        val instructionText = "Alinee su rostro con el óvalo"
        canvas.drawText(
            instructionText,
            centerX,
            centerY + ovalHeight / 2 + 60f,
            instructionPaint
        )
    }

    /**
     * Obtiene las coordenadas del óvalo guía.
     */
    fun getOvalBounds(): RectF {
        return RectF(
            centerX - ovalWidth / 2,
            centerY - ovalHeight / 2,
            centerX + ovalWidth / 2,
            centerY + ovalHeight / 2
        )
    }
}