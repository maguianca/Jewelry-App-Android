package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.core.TAG
import com.example.myapp.todo.LightSensorMonitor
import com.example.myapp.todo.ShakeDetector
import com.example.myapp.ui.theme.MyAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Log.d(TAG, "onCreate")
            val myAppViewModel = viewModel<MyAppViewModel>(factory = MyAppViewModel.Factory)
            MyApp(myAppViewModel = myAppViewModel) {
                MyAppNavHost(myAppViewModel = myAppViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            (application as MyApplication).container.itemRepository.openWsClient()
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            (application as MyApplication).container.itemRepository.closeWsClient()
        }
    }
}

@Composable
fun MyApp(
    myAppViewModel: MyAppViewModel,
    content: @Composable () -> Unit
) {
    Log.d("MyApp", "recompose")
    val context = LocalContext.current
    var isSensorDark by remember { mutableStateOf(false) }
    LightSensorMonitor { lux ->
        isSensorDark = lux < 10000f
        Log.d("LightSensor", "Lux: $lux | DarkMode: $isSensorDark")
    }

    DisposableEffect(Unit) {
        val shakeDetector = ShakeDetector(context) {
            Log.d("Shake", "Shake detected! Triggering Logout in ViewModel...")
            myAppViewModel.logout()
        }
        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
        }
    }

    MyAppTheme(darkTheme = isSensorDark) {
        Surface {
            content()
        }
    }
}

@Preview
@Composable
fun PreviewMyApp() {
    val previewViewModel = viewModel<MyAppViewModel>(factory = MyAppViewModel.Factory)

    MyApp(myAppViewModel = previewViewModel) {
        MyAppNavHost(myAppViewModel = previewViewModel)
    }
}