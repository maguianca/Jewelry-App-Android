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
import android.util.Base64
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

typealias OnItemFn = (id: String?) -> Unit
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ItemList(itemList: List<Item>, onItemClick: OnItemFn, modifier: Modifier,listState: LazyListState = rememberLazyListState(),sharedTransitionScope: SharedTransitionScope,
             animatedVisibilityScope: AnimatedVisibilityScope) {
    Log.d("ItemList", "recompose")
    LazyColumn(
        state=listState,
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(itemList) { item ->
            ItemDetail(item, onItemClick,sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope)
        }
    }
}
fun toBitmap(url64: String): Bitmap? {
    try {
        val commaIndex = url64.indexOf(',')
        val base64String = if (commaIndex != -1) {
            url64.substring(commaIndex + 1)
        } else {
            url64
        }

        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        Log.e("ItemList", "Eroare la decodare Base64 pentru imagine", e)
        return null
    }
}
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ItemDetail(item: Item, onItemClick: OnItemFn,sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope) {
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

            with(sharedTransitionScope) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "image-${item._id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
