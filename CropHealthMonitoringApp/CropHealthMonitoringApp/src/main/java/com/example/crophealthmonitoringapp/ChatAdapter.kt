package com.example.crophealthmonitoringapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messageList: MutableList<Message>,
    private val currentUserId: String // Pass the logged-in user ID to differentiate message alignment
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // Method to add messages dynamically and update the RecyclerView
    fun addMessage(message: Message) {
        messageList.add(message)
        notifyItemInserted(messageList.size - 1) // Notify adapter of the new item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message, currentUserId)
    }

    override fun getItemCount(): Int = messageList.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
        private val senderTextView: TextView = itemView.findViewById(R.id.textViewSender)

        fun bind(message: Message, currentUserId: String) {
            // Display the sender ID
            senderTextView.text = if (message.senderId == currentUserId) "You" else message.senderId

            // Set the message text
            messageTextView.text = message.messageText

            // Align the message based on sender
            val layoutParams = messageTextView.layoutParams as ViewGroup.MarginLayoutParams
            if (message.senderId == currentUserId) {
                // Align to the right for the current user
                layoutParams.marginStart = 50
                layoutParams.marginEnd = 0
                messageTextView.setBackgroundResource(R.drawable.bg_message_sent)
            } else {
                // Align to the left for other users
                layoutParams.marginStart = 0
                layoutParams.marginEnd = 50
                messageTextView.setBackgroundResource(R.drawable.bg_message_received)
            }
            messageTextView.layoutParams = layoutParams
        }
    }
}
