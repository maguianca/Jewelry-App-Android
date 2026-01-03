package com.example.myapp.todo.ui.items

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.OneTimeWorkRequestBuilder
import com.example.myapp.R
import com.example.myapp.core.Result
import com.example.myapp.todo.ConnectionState
import com.example.myapp.todo.PendingWorker
import com.example.myapp.todo.ProximitySensorMonitor
import com.example.myapp.todo.connectivityState
import com.example.myapp.todo.data.Item
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun ItemsScreen(
    onItemClick: (id: String?) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAddItem: () -> Unit,
    onLogout: () -> Unit
) {
    Log.d("ItemsScreen", "recompose")
    val itemsViewModel = viewModel<ItemsViewModel>(factory = ItemsViewModel.Factory)
    val itemsUiState by itemsViewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    ProximitySensorMonitor(
        onNear = {
            Log.d("Proximity", "Hand detected! Scrolling to top.")
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    )

    val networkConnectivity by connectivityState()
    var isOffline by remember { mutableStateOf(networkConnectivity == ConnectionState.Unavailable) }

    LaunchedEffect(networkConnectivity) {
        val wasOffline = isOffline
        isOffline = networkConnectivity == ConnectionState.Unavailable

        if (wasOffline && networkConnectivity == ConnectionState.Available) {
            Log.d("ItemsScreen", "Network connection RESTORED. Enqueuing background worker.")
            val request = OneTimeWorkRequestBuilder<PendingWorker>().build()
            // workManager.enqueue(request) // Decomentează dacă ai acces la workManager
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.items)) },
                actions = {
                    Text(if (networkConnectivity == ConnectionState.Available) "Online " else "Offline ")
                    Text("•",
                        color = if (networkConnectivity == ConnectionState.Available) Color.Green else Color.Red,
                        modifier = Modifier.padding(end = 20.dp))
                    Button(onClick = onLogout) { Text("Logout") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("ItemsScreen", "add")
                    onAddItem()
                },
            ) { Icon(Icons.Rounded.Add, "Add") }
        }
    ) { padding ->
        when (itemsUiState) {
            is Result.Success ->
                ItemList(
                    itemList = (itemsUiState as Result.Success<List<Item>>).data,
                    onItemClick = onItemClick,
                    modifier = Modifier.padding(padding),
                    listState = listState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            is Result.Loading -> CircularProgressIndicator(modifier = Modifier.padding(padding))
            is Result.Error -> Text(
                text = "Failed to load items - ${(itemsUiState as Result.Error).exception?.message}",
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun PreviewItemsScreen() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            ItemsScreen(
                onItemClick = {},
                onAddItem = {},
                onLogout = {},
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this
            )
        }
    }
}
