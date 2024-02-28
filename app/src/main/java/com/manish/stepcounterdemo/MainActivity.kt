package com.manish.stepcounterdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.manish.stepcounterdemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var permissions: Array<String>
    private lateinit var binding: ActivityMainBinding
    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adding required permissions
        permissions = arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION)

        binding.btnStart.setOnClickListener {
            if (isStarted) {
                stopSensors()
            } else {
                if (isPermissionsAllowed())
                    initSensors()
                else
                    permissionsLauncher.launch(permissions)
            }
        }

        // Register BroadcastReceiver to receive updates
        val filter = IntentFilter(StepCounterService.ACTION_UPDATE_UI)
        LocalBroadcastManager.getInstance(this).registerReceiver(stepsReceiver, filter)
    }

    private fun isPermissionsAllowed(): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (isPermissionsAllowed())
                initSensors()
        }

    private fun initSensors() {
        binding.helperText.visibility = View.VISIBLE
        binding.btnStart.text = "Stop"

        val service = Intent(this, StepCounterService::class.java)
        startService(service)
        isStarted = true
    }

    private fun stopSensors() {
        val service = Intent(this, StepCounterService::class.java)
        stopService(service)
        binding.btnStart.text = "Start"
        isStarted = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSensors()
    }

    private val stepsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == StepCounterService.ACTION_UPDATE_UI) {
                val data = intent.getLongExtra("steps", 0L)

                // Update UI
                binding.stepsCount.text = data.toString()
                binding.progressBar.progress = (data % 100L).toInt()
            }
        }
    }
}