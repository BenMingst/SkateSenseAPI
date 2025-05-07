package com.example.skatesenseapi.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.skatesenseapi.databinding.DialogBluetoothScanBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BluetoothScanDialog(
    private val scanManager: BluetoothScanManager,
    private val onDeviceSelected: (BluetoothDevice) -> Unit
) : DialogFragment() {

    private var _binding: DialogBluetoothScanBinding? = null
    private val binding get() = _binding!!
    private val deviceAdapter = BluetoothDeviceAdapter { device ->
        onDeviceSelected(device)
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBluetoothScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deviceList.adapter = deviceAdapter
        binding.scanButton.setOnClickListener {
            if (scanManager.isScanning) {
                scanManager.stopScan()
                binding.scanButton.text = "Start Scan"
                binding.progressBar.visibility = View.GONE
            } else {
                scanManager.startScan()
                binding.scanButton.text = "Stop Scan"
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        scanManager.scannedDevices
            .onEach { devices ->
                deviceAdapter.submitList(devices.toList())
                binding.emptyView.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanManager.stopScan()
        _binding = null
    }
}