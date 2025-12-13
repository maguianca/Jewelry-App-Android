package com.example.myapp.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapp.todo.ui.item.convertDateToString
import java.util.Date
@Entity
data class Item(
    @PrimaryKey val _id: String = "${System.currentTimeMillis()*10000}",
    val cod:String="",
    val categorie: String="",
    val pret:Int=0,
    val pietre: Boolean=false,
    val data: String=convertDateToString(Date()),
    val imageUrl: String="",
    var requiresCreate: Boolean=false,
    var requiresUpdate: Boolean=false
)
