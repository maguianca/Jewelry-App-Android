package com.example.myapp.todo.data.remote

import com.example.myapp.todo.data.Item

data class Payload(val updatedItem: Item)
data class ItemEvent(val event: String, val payload: Payload)

