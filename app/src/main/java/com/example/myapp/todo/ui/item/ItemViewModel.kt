package com.example.myapp.todo.ui.item

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.MyApplication
import com.example.myapp.core.Result
import com.example.myapp.core.TAG
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.ItemRepository
import kotlinx.coroutines.launch
import java.util.Date
data class ItemUiState(
    val itemId: String? = null,
    val item: Item = Item(),
    var loadResult: Result<Item>? = null,
    var submitResult: Result<Item>? = null,
)

class ItemViewModel(private val itemId: String?, private val itemRepository: ItemRepository) :
    ViewModel() {

    var uiState: ItemUiState by mutableStateOf(ItemUiState(loadResult = Result.Loading))
        private set

    init {
        Log.d(TAG, "init")
        if (itemId != null) {
            loadItem()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Item()))
        }
    }

    fun loadItem() {
        viewModelScope.launch {
            itemRepository.itemStream.collect { result ->
                if (!(uiState.loadResult is Result.Loading)) {
                    return@collect
                }
                if (result is Result.Success) {
                    val items = result.data
                    val item = items.find { it._id == itemId } ?: Item()
                    uiState = uiState.copy(loadResult = Result.Success(item), item = item)
                } else if (result is Result.Error) {
                    uiState =
                        uiState.copy(loadResult = Result.Error(result.exception))
                }
            }
        }
    }


    fun saveItem(cod:String, categorie:String,pret:Int, pietre:Boolean, data:Date ){
        viewModelScope.launch {
            Log.d(TAG, "save new game!!!");
            try{
                uiState = uiState.copy(submitResult = Result.Loading)
                val item = uiState.item.copy(cod=cod,categorie=categorie, pret=pret, pietre=pietre,data = convertDateToString(data))
                val savedItem: Item = itemRepository.save(item)
                Log.d(TAG, "save game succeeeded!!!!");
                uiState = uiState.copy(submitResult = Result.Success(savedItem))
            }catch (e: Exception){
                Log.d(TAG, "saveOrUpdateItem failed");
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    fun UpdateItem(cod:String, categorie:String,pret:Int, pietre:Boolean, data:Date) {
        viewModelScope.launch {
            Log.d(TAG, "update game!!!");
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val item = uiState.item.copy(cod=cod,categorie=categorie, pret=pret, pietre=pietre,data = convertDateToString(data))
                val savedItem: Item = itemRepository.update(item)
                Log.d(TAG, "UpdateItem succeeeded");
                uiState = uiState.copy(submitResult = Result.Success(savedItem))
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateItem failed");
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    companion object {
        fun Factory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ItemViewModel(itemId, app.container.itemRepository)
            }
        }
    }
}
