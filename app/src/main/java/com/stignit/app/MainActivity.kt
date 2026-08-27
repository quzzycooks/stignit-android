package com.stignit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.stignit.app.ui.nav.StignItNavHost
import com.stignit.app.ui.theme.StignItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StignItTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StignItNavHost()
                }
            }
        }
    }
}
