package com.example.crophealthmonitoringapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpertsAdapter(
    private val experts: MutableList<Expert>,
    private val onChatClick: (String) -> Unit
) : RecyclerView.Adapter<ExpertsAdapter.ExpertViewHolder>() {

    fun updateList(newExperts: List<Expert>) {
        experts.clear()
        experts.addAll(newExperts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expert, parent, false)
        return ExpertViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpertViewHolder, position: Int) {
        val expert = experts[position]
        holder.bind(expert, onChatClick)
    }

    override fun getItemCount(): Int = experts.size

    class ExpertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val expertNameTextView: TextView = itemView.findViewById(R.id.expertNameTextView)
        private val expertSpecializationTextView: TextView = itemView.findViewById(R.id.expertSpecializationTextView)
        private val chatButton: Button = itemView.findViewById(R.id.chatButton)

        fun bind(expert: Expert, onChatClick: (String) -> Unit) {
            expertNameTextView.text = expert.name ?: "Unknown Name"
            expertSpecializationTextView.text = expert.specialization ?: "Specialization not available"
            chatButton.setOnClickListener { onChatClick(expert.id ?: "") }

            // Optionally, manage button visibility
            chatButton.visibility = if (expert.id.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }
}
