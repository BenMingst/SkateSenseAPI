package com.example.skatesenseapi.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skatesenseapi.data.SessionEntity
import com.example.skatesenseapi.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.*

class SessionHistoryAdapter(
    private val onSessionClick: (SessionEntity) -> Unit
) : ListAdapter<SessionEntity, SessionHistoryAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding, onSessionClick)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionViewHolder(
        private val binding: ItemSessionBinding,
        private val onSessionClick: (SessionEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        
        fun bind(session: SessionEntity) {
            binding.apply {
                dateText.text = dateFormat.format(session.startTime)
                distanceValue.text = String.format("%.1f mi", session.distance)
                durationValue.text = formatDuration(session.duration)
                averageSpeedValue.text = String.format("%.1f mph", session.averageSpeed)
                maxSpeedValue.text = String.format("%.1f mph", session.maxSpeed)

                root.setOnClickListener { onSessionClick(session) }
            }
        }

        private fun formatDuration(millis: Long): String {
            val hours = millis / (1000 * 60 * 60)
            val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
            return if (hours > 0) {
                String.format("%dh %dm", hours, minutes)
            } else {
                String.format("%dm", minutes)
            }
        }
    }

    private class SessionDiffCallback : DiffUtil.ItemCallback<SessionEntity>() {
        override fun areItemsTheSame(oldItem: SessionEntity, newItem: SessionEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SessionEntity, newItem: SessionEntity): Boolean {
            return oldItem == newItem
        }
    }
}