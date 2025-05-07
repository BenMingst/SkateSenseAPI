package com.example.skatesenseapi.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skatesenseapi.databinding.ItemSettingBinding

data class SettingItem(
    val name: String,
    val key: String,
    val enabled: Boolean
)

class SettingsAdapter(
    private val onToggle: (String) -> Unit
) : ListAdapter<SettingItem, SettingsAdapter.ViewHolder>(SettingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSettingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.settingSwitch.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onToggle(getItem(position).key)
                }
            }
        }

        fun bind(item: SettingItem) {
            binding.settingName.text = item.name
            binding.settingSwitch.isChecked = item.enabled
        }
    }
}

private class SettingDiffCallback : DiffUtil.ItemCallback<SettingItem>() {
    override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
        return oldItem == newItem
    }
}