package com.example.myapp.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapp.todo.ui.item.convertDateToString
import java.util.Date
data class Item(
    val _id: String?=null,
    val cod:String="",
    val categorie: String="",
    val pret:Int=0,
    val pietre: Boolean=false,
    val data: String=convertDateToString(Date())
)
