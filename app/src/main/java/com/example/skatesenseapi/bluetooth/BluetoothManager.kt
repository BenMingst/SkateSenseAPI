package com.example.skatesenseapi.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.example.skatesenseapi.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothManager(
    context: Context,
    private val onStatusChanged: (MainViewModel.BluetoothStatus) -> Unit
) : BleManager(context) {
    
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    companion object {
        private const val TAG = "BluetoothManager"
        private val SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb") // Example UUID, replace with your service UUID
        private val CHARACTERISTIC_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb") // Example UUID, replace with your characteristic UUID
    }
    
    override fun getGattCallback(): BleManagerGattCallback {
        return object : BleManagerGattCallback() {
            override fun isRequiredServiceSupported(gatt: android.bluetooth.BluetoothGatt): Boolean {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    writeCharacteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                }
                return writeCharacteristic != null
            }
            
            override fun onServicesInvalidated() {
                writeCharacteristic = null
            }
            
            override fun initialize() {
                super.initialize()
                scope.launch {
                    _isConnected.value = true
                    onStatusChanged(MainViewModel.BluetoothStatus.CONNECTED)
                }
            }
            
            override fun onDeviceDisconnected() {
                super.onDeviceDisconnected()
                scope.launch {
                    _isConnected.value = false
                    onStatusChanged(MainViewModel.BluetoothStatus.DISCONNECTED)
                }
            }
        }
    }
    
    fun sendData(jsonString: String) {
        if (!isConnected.value) {
            Log.w(TAG, "Cannot send data - not connected")
            return
        }
        
        writeCharacteristic?.let { characteristic ->
            writeCharacteristic(characteristic, jsonString.toByteArray())
                .with { device, data -> 
                    Log.d(TAG, "Data sent successfully")
                }
                .enqueue()
        }
    }
}