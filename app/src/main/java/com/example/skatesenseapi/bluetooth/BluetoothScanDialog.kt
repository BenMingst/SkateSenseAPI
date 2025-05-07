package com.example.skatesenseapi.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skatesenseapi.databinding.DialogBluetoothScanBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BluetoothScanDialog(
    private val scanManager: BluetoothScanManager,
    private val onDeviceSelected: (BluetoothDevice) -> Unit
) : DialogFragment() {

    private var _binding: DialogBluetoothScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var deviceAdapter: BluetoothDeviceAdapter

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
        setupUI()
        observeScanResults()
        startScanning()
    }

    private fun setupUI() {
        deviceAdapter = BluetoothDeviceAdapter { device ->
            onDeviceSelected(device)
            dismiss()
        }

        binding.deviceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun observeScanResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            scanManager.scanResults.collectLatest { devices ->
                deviceAdapter.submitList(devices.values.toList())
                binding.scanProgress.visibility = 
                    if (devices.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun startScanning() {
        scanManager.startScan()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanManager.stopScan()
        _binding = null
    }
}