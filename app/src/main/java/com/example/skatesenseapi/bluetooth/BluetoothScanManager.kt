package com.example.skatesenseapi.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothScanManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    private val _scannedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val scannedDevices: StateFlow<Set<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null) {
                _scannedDevices.value = _scannedDevices.value + device
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            val newDevices = results
                .map { it.device }
                .filter { it.name != null }
            _scannedDevices.value = _scannedDevices.value + newDevices
        }

        override fun onScanFailed(errorCode: Int) {
            // Handle scan failure
            isScanning = false
        }
    }

    fun startScan() {
        if (!isScanning && bluetoothAdapter.isEnabled) {
            _scannedDevices.value = emptySet()
            scanner?.startScan(scanCallback)
            isScanning = true
        }
    }

    fun stopScan() {
        if (isScanning) {
            scanner?.stopScan(scanCallback)
            isScanning = false
        }
    }

    val isEnabled: Boolean
        get() = bluetoothAdapter.isEnabled
}