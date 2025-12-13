package com.example.myapp.todo.ui.item

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.core.Result
import com.example.myapp.todo.ImagePicker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String?, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState

    var cod by rememberSaveable { mutableStateOf(itemUiState.item.cod) }
    var categorie by rememberSaveable { mutableStateOf(itemUiState.item.categorie) }
    var pret by rememberSaveable { mutableStateOf(itemUiState.item.pret) }
    var data by rememberSaveable { mutableStateOf(convertStringToDate(itemUiState.item.data)) }
    var pietre by rememberSaveable { mutableStateOf(itemUiState.item.pietre) }
    var imageUrl by rememberSaveable { mutableStateOf(itemUiState.item.imageUrl) }

    Log.d("ItemScreen", "recompose, text = $cod")

    LaunchedEffect(itemUiState.submitResult) {
        Log.d("ItemScreen", "Submit = ${itemUiState.submitResult}");
        if (itemUiState.submitResult is Result.Success) {
            Log.d("ItemScreen", "Closing screen");
            onClose();
        }
    }

    var textInitialized by remember { mutableStateOf(itemId == null) }
    LaunchedEffect(itemId, itemUiState.loadResult) {
        Log.d("ItemScreen", "Text initialized = ${itemUiState.loadResult}");
        if (textInitialized) {
            return@LaunchedEffect
        }
        if (!(itemUiState.loadResult is Result.Loading)) {
            cod=itemUiState.item.cod
            categorie =itemUiState.item.categorie
            pret=itemUiState.item.pret
            pietre=itemUiState.item.pietre
            data= convertStringToDate(itemUiState.item.data)
            imageUrl=itemUiState.item.imageUrl
            textInitialized = true
        }
    }

    val defaultFieldModifier = Modifier.padding(vertical = 5.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) },
                actions = {
                    Button(onClick = {
                        Log.d("ItemScreen", "back to list");
                        onClose()
                    }) { Text("Back") }
                    Button(onClick = {
                        Log.d("ItemScreen", "save item text = $cod");
                        itemViewModel.UpdateItem(cod,categorie,pret,pietre,data,imageUrl)
                    }, modifier=Modifier.padding(horizontal=8.dp)) { Text("Update") }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            if (itemUiState.loadResult is Result.Loading) {
                CircularProgressIndicator()
                return@Scaffold
            }
            if (itemUiState.submitResult is Result.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { LinearProgressIndicator() }
            }
            if (itemUiState.loadResult is Result.Error) {
                Text(text = "Failed to load item - ${(itemUiState.loadResult as Result.Error).exception?.message}")
            }
            Column {
                TextField(
                    value = cod,
                    onValueChange = { cod = it }, label = { Text("cod") },
                    modifier = defaultFieldModifier.fillMaxWidth(),
                )

                TextField(
                    value = categorie,
                    onValueChange = { categorie= it }, label = { Text("categorie") },
                    modifier = defaultFieldModifier.fillMaxWidth(),
                )

                MyNumberField(
                    value = pret,
                    onValueChanged = {pret=it},
                    modifier = defaultFieldModifier,
                    label = "pret"
                )

                MyDatePickerDialog(convertDateToString(data), {
                    data = convertStringToDate(it)
                }, label="data depozit", modifier = defaultFieldModifier)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = defaultFieldModifier) {
                    Checkbox(
                        checked = pietre,
                        onCheckedChange = { pietre= it }
                    )
                    Text(
                        text="pietre",
                    )
                }
                ImagePicker(imageUrl, { imageUrl = it })
            }
            if (itemUiState.submitResult is Result.Error) {
                Text(
                    text = "Failed to submit item - ${(itemUiState.submitResult as Result.Error).exception?.message}",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewItemScreen() {
    ItemScreen(itemId = "0", onClose = {})
}