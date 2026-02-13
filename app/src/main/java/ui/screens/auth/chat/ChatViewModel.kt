package com.example.nutriplan.ui.screens.auth.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadMessages(conversationId: String) {
        _isLoading.value = true
        firestore.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val messagesList = snapshot?.documents?.mapNotNull { doc ->
                    Message(
                        id = doc.id,
                        conversationId = doc.getString("conversationId") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        isSentByMe = doc.getString("senderId") == auth.currentUser?.uid
                    )
                } ?: emptyList()

                _messages.value = messagesList
                _isLoading.value = false
            }
    }

    fun sendMessage(conversationId: String, text: String) {
        val currentUser = auth.currentUser ?: return
        val message = hashMapOf(
            "conversationId" to conversationId,
            "senderId" to currentUser.uid,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("messages").add(message)
    }
}
