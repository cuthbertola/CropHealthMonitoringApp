package com.example.crophealthmonitoringapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crophealthmonitoringapp.databinding.ActivityExpertChatListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExpertChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpertChatListBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var databaseReference: DatabaseReference
    private val chatSummaries = mutableListOf<ChatSummary>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityExpertChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        binding.recyclerViewChats.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(
            chatSummaries = chatSummaries,
            onChatClick = { chatId ->
                openChat(chatId)
            }
        )
        binding.recyclerViewChats.adapter = chatAdapter

        // Load expert's chats
        val expertId = FirebaseAuth.getInstance().currentUser?.uid
        if (expertId != null) {
            loadChats(expertId)
        } else {
            Toast.makeText(this, "Error: Expert not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadChats(expertId: String) {
        // Firebase reference to "chats"
        databaseReference = FirebaseDatabase.getInstance().reference.child("chats")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatSummaries.clear()
                for (child in snapshot.children) {
                    val participants = child.child("participants")
                    val expert = participants.child("expert_Id").getValue(String::class.java)

                    if (expert == expertId) {
                        val chatId = child.key ?: continue
                        val farmerId = participants.child("user_Id").getValue(String::class.java)
                        farmerId?.let {
                            fetchUserName(it) { farmerName ->
                                val lastMessage = child.child("messages").children.lastOrNull()
                                val lastMessageText = lastMessage?.child("messageText")?.getValue(String::class.java)
                                    ?: "No messages yet"
                                val lastTimestamp = lastMessage?.child("timestamp")?.getValue(Long::class.java)
                                    ?: System.currentTimeMillis()

                                chatSummaries.add(
                                    ChatSummary(
                                        chatId = chatId,
                                        participantName = farmerName,
                                        lastMessage = lastMessageText,
                                        lastMessageTimestamp = lastTimestamp
                                    )
                                )
                                chatAdapter.notifyDataSetChanged()
                                toggleEmptyState()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExpertChatListActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ExpertChatListActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("fullName").getValue(String::class.java) ?: "Farmer"
                callback(userName)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExpertChatListActivity", "Failed to fetch user name: ${error.message}")
                callback("Farmer")
            }
        })
    }

    private fun toggleEmptyState() {
        if (chatSummaries.isEmpty()) {
            binding.recyclerViewChats.visibility = View.GONE
            binding.noChatsTextView.visibility = View.VISIBLE
        } else {
            binding.recyclerViewChats.visibility = View.VISIBLE
            binding.noChatsTextView.visibility = View.GONE
        }
    }

    private fun openChat(chatId: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chatId", chatId)
        startActivity(intent)
    }
}
