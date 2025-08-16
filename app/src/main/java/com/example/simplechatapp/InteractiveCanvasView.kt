package com.example.simplechatapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.input.key.type
import com.google.gson.Gson // For JSON serialization (add Gson dependency)
import com.google.gson.reflect.TypeToken // For JSON deserialization
import java.util.UUID

// --- Data classes for canvas objects ---
// Base class for all drawable objects on the canvas
interface CanvasObject {
    val id: String
}

data class SerializablePath(
    override val id: String = UUID.randomUUID().toString(),
    val points: MutableList<PointF> = mutableListOf(),
    var color: Int,
    var strokeWidth: Float
) : CanvasObject {
    @Transient // Tell Gson to ignore this field during serialization
    val androidPath: Path = Path() // For actual drawing

    fun reconstructPath() {
        androidPath.reset()
        if (points.isNotEmpty()) {
            androidPath.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                androidPath.lineTo(points[i].x, points[i].y)
            }
        }
    }
}

// Later you'll add:
// data class TextObject(...) : CanvasObject
// data class ImageObject(...) : CanvasObject

class InteractiveCanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs) {

    private val drawnPaths = mutableListOf<SerializablePath>()
    private var currentDrawingPath: SerializablePath? = null
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private var currentBrushColor: Int = Color.BLACK
    private var currentStrokeWidth: Float = 10f

    // Zoom/Pan variables (simplified for now)
    private var scaleFactor = 1.0f
    private var canvasTranslateX = 0f
    private var canvasTranslateY = 0f
    // TODO: Implement actual zoom/pan gesture handling

    private val gson = Gson()

    init {
        // Essential for a custom view that draws
        setWillNotDraw(false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = (event.x - canvasTranslateX) / scaleFactor
        val touchY = (event.y - canvasTranslateY) / scaleFactor

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentDrawingPath = SerializablePath(
                    color = currentBrushColor,
                    strokeWidth = currentStrokeWidth
                ).also {
                    it.points.add(PointF(touchX, touchY))
                    it.reconstructPath() // Start the path
                }
                invalidate() // Redraw
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentDrawingPath?.let {
                    it.points.add(PointF(touchX, touchY))
                    // Instead of full reconstruct, can use path.lineTo for efficiency during move
                    it.androidPath.lineTo(touchX, touchY)
                }
                invalidate() // Redraw
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentDrawingPath?.let {
                    // Finalize the path object if necessary
                    // For simple paths, it's already updated during ACTION_MOVE
                    if (it.points.size > 1) { // Only add if it's more than a dot
                        drawnPaths.add(it)
                    }
                }
                currentDrawingPath = null // Reset for the next path
                invalidate() // Redraw
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        // Apply zoom and pan transformations
        canvas.scale(scaleFactor, scaleFactor)
        canvas.translate(canvasTranslateX / scaleFactor, canvasTranslateY / scaleFactor) // Adjust translation by scale

        // Draw all completed paths
        drawnPaths.forEach { pathData ->
            paint.color = pathData.color
            paint.strokeWidth = pathData.strokeWidth
            // Ensure path is reconstructed if it wasn't already (e.g., after deserialization)
            if (pathData.androidPath.isEmpty && pathData.points.isNotEmpty()) {
                pathData.reconstructPath()
            }
            canvas.drawPath(pathData.androidPath, paint)
        }

        // Draw the current path being drawn
        currentDrawingPath?.let { pathData ->
            paint.color = pathData.color
            paint.strokeWidth = pathData.strokeWidth
            canvas.drawPath(pathData.androidPath, paint)
        }

        canvas.restore()
    }

    fun setPenColor(color: Int) {
        currentBrushColor = color
    }

    fun setPenStrokeWidth(width: Float) {
        currentStrokeWidth = width
    }

    fun clearCanvas() {
        drawnPaths.clear()
        currentDrawingPath = null
        invalidate()
    }

    // --- Serialization ---
    fun getContentAsJson(): String? {
        if (drawnPaths.isEmpty()) return null
        // For now, only serializing paths. Later, serialize all CanvasObject types.
        return gson.toJson(drawnPaths)
    }

    fun loadContentFromJson(jsonString: String?) {
        clearCanvas()
        if (jsonString.isNullOrEmpty()) {
            invalidate()
            return
        }
        try {
            val typeToken = object : TypeToken<List<SerializablePath>>() {}.type
            val paths: List<SerializablePath> = gson.fromJson(jsonString, typeToken)
            drawnPaths.addAll(paths)
            drawnPaths.forEach { it.reconstructPath() } // Rebuild Path objects
        } catch (e: Exception) {
            // Handle JSON parsing error, e.g., log it or show a toast
            e.printStackTrace()
        }
        invalidate()
    }

    // Call this to get a bitmap of the current view (e.g., for thumbnail)
    // Note: This captures only the visible part and current zoom.
    // For a full export, you'd need to draw to an offscreen bitmap at 1.0f scale.
    fun captureCanvasBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Optionally draw a background color first
        // canvas.drawColor(Color.WHITE)
        this.draw(canvas)
        return bitmap
    }
}
