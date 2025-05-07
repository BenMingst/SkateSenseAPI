package com.example.skatesenseapi.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.skatesenseapi.R
import com.example.skatesenseapi.bluetooth.BluetoothScanDialog
import com.example.skatesenseapi.bluetooth.BluetoothScanManager
import com.example.skatesenseapi.databinding.ActivityMainBinding
import com.example.skatesenseapi.utils.AppError
import com.example.skatesenseapi.utils.PermissionManager
import com.example.skatesenseapi.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager
    private var longPressJob: Job? = null
    private var jsonUpdateJob: Job? = null

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanManager: BluetoothScanManager

    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startBluetoothScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)
        initializeBluetooth()
        setupUI()
        checkPermissions()
        observeViewModel()
    }

    private fun initializeBluetooth() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        scanManager = BluetoothScanManager(this, bluetoothAdapter)
    }

    private fun setupUI() {
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.startStopButton.apply {
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (viewModel.isRunning.value) {
                            startLongPressTimer()
                        } else {
                            if (permissionManager.arePermissionsGranted()) {
                                viewModel.startTracking()
                                text = getString(R.string.stop)
                            } else {
                                checkPermissions()
                            }
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        longPressJob?.cancel()
                    }
                }
                true
            }
        }

        binding.viewHistoryButton.setOnClickListener {
            startActivity(Intent(this, SessionHistoryActivity::class.java))
        }
    }

    private fun checkPermissions() {
        if (!permissionManager.arePermissionsGranted()) {
            permissionManager.checkAndRequestPermissions(this)
        }
    }

    private fun startLongPressTimer() {
        longPressJob = lifecycleScope.launch {
            delay(5000) // 5 seconds long press
            viewModel.stopTracking()
            binding.startStopButton.text = getString(R.string.start)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.bluetoothStatus.collect { status ->
                updateBluetoothStatus(status)
            }
        }

        lifecycleScope.launch {
            viewModel.isRunning.collect { isRunning ->
                if (isRunning) {
                    startJsonUpdates()
                } else {
                    stopJsonUpdates()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errors.collect { error ->
                error?.let { showError(it) }
            }
        }
    }

    private fun updateBluetoothStatus(status: MainViewModel.BluetoothStatus) {
        val (text, color) = when (status) {
            MainViewModel.BluetoothStatus.CONNECTED -> 
                getString(R.string.connected_to) to R.color.status_connected
            MainViewModel.BluetoothStatus.CONNECTING -> 
                getString(R.string.connecting) to R.color.status_connecting
            MainViewModel.BluetoothStatus.DISCONNECTED -> 
                getString(R.string.not_connected) to R.color.status_disconnected
        }

        binding.bluetoothStatus.apply {
            this.text = text
            setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_status_dot,
                0, 0, 0
            )
            compoundDrawables[0]?.setTint(
                ContextCompat.getColor(context, color)
            )
        }
    }

    private fun startJsonUpdates() {
        jsonUpdateJob = lifecycleScope.launch {
            while (true) {
                binding.jsonPreview.text = viewModel.getJsonString()
                delay(250) // Update every quarter second
            }
        }
    }

    private fun stopJsonUpdates() {
        jsonUpdateJob?.cancel()
        binding.jsonPreview.text = ""
    }

    private fun startBluetoothScan() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
            return
        }

        if (!permissionManager.arePermissionsGranted()) {
            checkPermissions()
            return
        }

        BluetoothScanDialog(scanManager) { device ->
            viewModel.connectToBluetooth(device.address)
        }.show(supportFragmentManager, "bluetooth_scan")
    }

    private fun showError(error: AppError) {
        val message = when (error) {
            is AppError.BluetoothError -> error.message
            is AppError.LocationError -> error.message
            is AppError.WeatherError -> error.message
            is AppError.NavigationError -> error.message
            is AppError.SensorError -> error.message
        }

        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Dismiss") {
                viewModel.clearError()
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.md_theme_light_primary))
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.PERMISSION_REQUEST_CODE) {
            if (permissionManager.arePermissionsGranted()) {
                // Permissions granted, can start tracking
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        longPressJob?.cancel()
        jsonUpdateJob?.cancel()
    }
}