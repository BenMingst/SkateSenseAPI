package com.example.skatesenseapi.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("MissingPermission")
class BluetoothScanManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    private var scanner: BluetoothLeScanner? = null
    private val discoveredDevices = mutableMapOf<String, BluetoothDevice>()
    
    private val _scanResults = MutableStateFlow<Map<String, BluetoothDevice>>(emptyMap())
    val scanResults: StateFlow<Map<String, BluetoothDevice>> = _scanResults
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            device.name?.let { name ->
                if (name.contains("SkateSense", ignoreCase = true)) {
                    discoveredDevices[device.address] = device
                    _scanResults.value = discoveredDevices.toMap()
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // Handle scan failure
        }
    }

    fun startScan() {
        discoveredDevices.clear()
        _scanResults.value = emptyMap()
        
        scanner = bluetoothAdapter.bluetoothLeScanner
        scanner?.startScan(scanCallback)
    }

    fun stopScan() {
        scanner?.stopScan(scanCallback)
    }
}