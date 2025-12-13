package com.example.myapp.todo.ui.items

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.todo.data.Item
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.util.Base64

typealias OnItemFn = (id: String?) -> Unit

@Composable
fun ItemList(itemList: List<Item>, onItemClick: OnItemFn, modifier: Modifier) {
    Log.d("ItemList", "recompose")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(itemList) { item ->
            ItemDetail(item, onItemClick)
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun toBitmap(url64:String):Bitmap {
    val data64 = url64.substring("data:image/jpg;base64,".length)
    val bytes = Base64.getDecoder().decode(data64)
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bmp.copy(Bitmap.Config.ARGB_8888, true)
}
@Composable
fun ItemDetail(item: Item, onItemClick: OnItemFn) {
    Log.d("ItemDetail", "recompose id = ${item._id}, cod: ${item.cod}")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onItemClick(item._id) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.cod,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = item.categorie,
                style = TextStyle(fontSize = 18.sp, fontStyle = FontStyle.Italic)
            )
            Text(
                text = "pietre: ${item.pietre}",
                style = TextStyle(fontSize = 16.sp, color = Color.Gray)
            )
            Text(
                text = "${item.pret}",
                style = TextStyle(fontSize = 16.sp, color = Color.Gray)
            )
            Text(
                text = item.data,
                style = TextStyle(fontSize = 16.sp, color = Color.Gray)
            )
        }
        if (!item.imageUrl.isNullOrEmpty()) {

            val imageModel = if (item.imageUrl.startsWith("data:")) {
                toBitmap(item.imageUrl)
            } else {
                item.imageUrl
            }

            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
