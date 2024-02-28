package com.manish.stepcounterdemo

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class StepCounterService : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var startCount: Long = -1L

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        stepCounterSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor != null) {
            sensorManager!!.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            Log.d("TAG", "onCreate: Starting Sensor")
        } else {
            // Handle case where step counter sensor is not available
            Log.d("TAG", "onCreate: Sensor not available")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currCount = event.values[0].toLong()
        if (startCount == -1L) {
            startCount = currCount
        }

        // Update UI or broadcast step count
        val steps = currCount - startCount
        Log.d("TAG", "onSensorChanged: $steps")
        sendBroadcast(steps)
    }

    private fun sendBroadcast(count: Long) {
        val intent = Intent(ACTION_UPDATE_UI)
        intent.putExtra("steps", count)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle accuracy changes if needed

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_UPDATE_UI = "com.example.action.UPDATE_UI"
    }
}
