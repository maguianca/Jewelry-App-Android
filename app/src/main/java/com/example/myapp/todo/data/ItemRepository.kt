package com.example.myapp.todo.data

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.myapp.core.Result
import com.example.myapp.core.TAG
import com.example.myapp.core.data.remote.Api
import com.example.myapp.todo.data.remote.ItemEvent
import com.example.myapp.todo.data.remote.ItemService
import com.example.myapp.todo.data.remote.ItemWsClient
import com.example.myapp.todo.ui.item.convertDateToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.String
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.myapp.todo.PendingWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class ItemRepository(private val itemService: ItemService, private val itemWsClient: ItemWsClient,
                     private val database: ItemDatabase, private val context: Context
) {
    private var items: List<Item> = listOf();

    private var itemsFlow: MutableSharedFlow<Result<List<Item>>> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val itemStream: Flow<Result<List<Item>>> = itemsFlow

    init {
        Log.d(TAG, "init")
    }

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            items = itemService.find(authorization = getBearerToken())
            database.itemDao().clear()
            for(i in items) {
                i.requiresUpdate = false
                i.requiresCreate = false
                database.itemDao().insert(i)
            }
            Log.d(TAG, "refresh succeeded")
            itemsFlow.emit(Result.Success(items))
        } catch (e: Exception) {
            Log.d(TAG, "refresh failed", e)
            items  =database.itemDao().getAll()
            itemsFlow.emit(Result.Success(items))
            //itemsFlow.emit(Result.Error(e))
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d(TAG, "Item event collected $it")
                if (it is Result.Success) {
                    val itemEvent = it.data;
                    when (itemEvent.event) {
                        "created" -> handleItemCreated(itemEvent.payload.updatedItem)
                        "updated" -> handleItemUpdated(itemEvent.payload.updatedItem)
                        "deleted" -> handleItemDeleted(itemEvent.payload.updatedItem)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            itemWsClient.closeSocket()
        }
    }

    suspend fun getItemEvents(): Flow<Result<ItemEvent>> = callbackFlow {
        Log.d(TAG, "getItemEvents started")
        itemWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    Log.d(TAG, "onEvent trySend $it")
                    trySend(Result.Success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() });
        awaitClose { itemWsClient.closeSocket() }
    }

    suspend fun update(item: Item): Item {
        try {
            item.requiresUpdate=false
            Log.d(TAG, "update $item...")
            val updatedItem = itemService.update(authorization = getBearerToken(), item._id, item)
            Log.d(TAG, "update $item succeeded")
            handleItemUpdated(updatedItem)
            return updatedItem
        }
        catch (ex:Exception){
            Log.d(TAG, "failed update $item")
            item.requiresUpdate=true
            handleItemUpdated(item)

            Handler(Looper.getMainLooper()).post({
                Toast.makeText(context, "Server unreachable. Saved locally", Toast.LENGTH_LONG).show()
            })
            scheduleSync()
            return item
        }
    }

    suspend fun save(item: Item): Item {
        try {
            Log.d(TAG, "save $item...")
            item.requiresCreate=false
            val createdItem = itemService.create(authorization = getBearerToken(), item)
            Log.d(TAG, "save $item succeeded")
            Log.d(TAG, "handle created $createdItem")
            handleItemCreated(createdItem)
            return createdItem
        }
        catch (ex:Exception){
            val createdItem = Item(
                cod=item.cod,
                categorie=item.categorie,
                pret=item.pret,
                pietre=item.pietre,
                data=item.data,
                imageUrl = item.imageUrl,
                requiresCreate = true,
                requiresUpdate = false
            )
            Log.d(TAG, "failed create on the server $item")
            handleItemCreated(createdItem)

            Handler(Looper.getMainLooper()).post({
                Toast.makeText(context, "Server unreachable. Saved locally", Toast.LENGTH_LONG).show()
            })
            scheduleSync()
                return createdItem
        }
    }

    private fun scheduleSync() {
        val workManager = WorkManager.getInstance(context)

        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequest.Builder(PendingWorker::class.java)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(request)
        Log.d(TAG, "Sync work request enqueued with network constraints.")
        print("Sync work request enqueued with network constraints.")
    }
    private suspend fun handleItemDeleted(item: Item) {
        Log.d(TAG, "handleItemDeleted - todo $item")
    }

    private suspend fun handleItemUpdated(item: Item) {
        Log.d(TAG, "handleItemUpdated...: $item")
        items = items.map { if (it._id == item._id) item else it }
        database.itemDao().update(item)
        itemsFlow.emit(Result.Success(items))
    }

    private suspend fun handleItemCreated(item: Item) {
        Log.d(TAG, "handleItemCreated...: $item")
        if(!items.contains(item)) {
            items = items.plus(item)
            database.itemDao().insert(item)
        }
        itemsFlow.emit(Result.Success(items))
    }

    fun quite_remove(item: Item){
        items = items.minus(item)
        database.itemDao().deleteById(item._id)
    }

    fun setToken(token: String) {
        itemWsClient.authorize(token)
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"
}