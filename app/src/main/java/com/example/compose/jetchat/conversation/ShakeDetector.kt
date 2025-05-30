import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class ShakeDetector(
    private val onDelete: () -> Unit,   // For forward-backward shake (Z)
    private val onRedo: () -> Unit      // For side-to-side shake (X)
) : SensorEventListener {

    private var lastShakeTime: Long = 0
    private val shakeThreshold = 2.5f         // Lower = more sensitive (adjust as needed)
    private val shakeSlopTimeMs = 500L       // 1 second between shakes

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0] / SensorManager.GRAVITY_EARTH
            //val y = event.values[1] / SensorManager.GRAVITY_EARTH
            val z = event.values[2] / SensorManager.GRAVITY_EARTH

            val absX = abs(x)
            val absZ = abs(z)

            val now = System.currentTimeMillis()
            if (now - lastShakeTime < shakeSlopTimeMs) return

            if (absZ > shakeThreshold && absZ > absX) {
                // Forward-backward shake: trigger delete
                lastShakeTime = now
                onDelete()
            } else if (absX > shakeThreshold && absX > absZ) {
                // Side-to-side shake: trigger redo
                lastShakeTime = now
                onRedo()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
