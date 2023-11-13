package edu.iu.habahram.simpleinsta
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

abstract class MeasurableSensor(
    protected val sensorType: Int
) {

    protected var onSensorValuesChanged: ((List<Float>) -> Unit)? = null

    abstract val doesSensorExist: Boolean

    abstract fun startListening()
    abstract fun stopListening()

    fun setOnSensorValuesChangedListener(listener: (List<Float>) -> Unit) {
        onSensorValuesChanged = listener
    }
}

abstract class AndroidSensor(
    private val context: Context,
    private val sensorFeature: String,
    sensorType: Int
) : MeasurableSensor(sensorType), SensorEventListener {

    override val doesSensorExist: Boolean
        get() = context.packageManager.hasSystemFeature(sensorFeature)

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    override fun startListening() {
        if (!doesSensorExist) {
            return
        }
        if (!::sensorManager.isInitialized && sensor == null) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensor = sensorManager.getDefaultSensor(sensorType)
        }
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun stopListening() {
        if (!doesSensorExist || !::sensorManager.isInitialized) {
            return
        }
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!doesSensorExist) {
            return
        }
        if (event?.sensor?.type == sensorType) {
            onSensorValuesChanged?.invoke(event.values.toList())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
}