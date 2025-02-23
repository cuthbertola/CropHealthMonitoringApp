package com.example.crophealthmonitoringapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: MutableList<Message> = mutableListOf(),
    private val chatSummaries: MutableList<ChatSummary> = mutableListOf(),
    private val userId: String? = null,
    private val onChatClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_SYSTEM = 3
        private const val VIEW_TYPE_SUMMARY = 4
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatSummaries.isNotEmpty()) {
            VIEW_TYPE_SUMMARY
        } else {
            when {
                messages[position].senderId == userId -> VIEW_TYPE_SENT
                messages[position].senderId == "system" -> VIEW_TYPE_SYSTEM
                else -> VIEW_TYPE_RECEIVED
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> SentMessageViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false))
            VIEW_TYPE_RECEIVED -> ReceivedMessageViewHolder(inflater.inflate(R.layout.item_message_received, parent, false))
            VIEW_TYPE_SYSTEM -> SystemMessageViewHolder(inflater.inflate(R.layout.item_message_system, parent, false))
            VIEW_TYPE_SUMMARY -> ChatSummaryViewHolder(inflater.inflate(R.layout.item_chat_summary, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (chatSummaries.isNotEmpty()) {
            val summary = chatSummaries[position]
            (holder as? ChatSummaryViewHolder)?.bind(summary, onChatClick)
        } else {
            val message = messages[position]
            when (holder) {
                is SentMessageViewHolder -> holder.bind(message)
                is ReceivedMessageViewHolder -> holder.bind(message)
                is SystemMessageViewHolder -> holder.bind(message)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (chatSummaries.isNotEmpty()) chatSummaries.size else messages.size
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setChatSummaries(summaries: List<ChatSummary>) {
        chatSummaries.clear()
        chatSummaries.addAll(summaries)
        notifyDataSetChanged()
    }

    // Centralized timestamp formatting
    private fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    // ViewHolder for Sent Messages
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sentMessageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.sentMessageTimestamp)

        fun bind(message: Message) {
            messageTextView.text = message.messageText
            timestampTextView.text = formatTimestamp(message.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    }

    // ViewHolder for Received Messages
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.receivedMessageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.receivedMessageTimestamp)

        fun bind(message: Message) {
            messageTextView.text = message.messageText
            timestampTextView.text = formatTimestamp(message.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    }

    // ViewHolder for System Messages
    class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val systemMessageTextView: TextView = itemView.findViewById(R.id.systemMessageTextView)

        fun bind(message: Message) {
            systemMessageTextView.text = message.messageText
        }
    }

    // ViewHolder for Chat Summaries
    class ChatSummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val summaryNameTextView: TextView = itemView.findViewById(R.id.summaryNameTextView)
        private val summaryLastMessageTextView: TextView = itemView.findViewById(R.id.summaryLastMessageTextView)
        private val summaryTimestampTextView: TextView = itemView.findViewById(R.id.summaryTimestampTextView)

        fun bind(summary: ChatSummary, onChatClick: ((String) -> Unit)?) {
            summaryNameTextView.text = summary.participantName
            summaryLastMessageTextView.text = summary.lastMessage
            summaryTimestampTextView.text = formatTimestamp(summary.lastMessageTimestamp)

            itemView.setOnClickListener {
                onChatClick?.invoke(summary.chatId)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    }
}
