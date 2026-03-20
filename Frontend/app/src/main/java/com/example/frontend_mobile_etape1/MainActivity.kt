package com.example.frontend_mobile_etape1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.frontend_mobile_etape1.ui.navigation.AppNavGraph
import com.example.frontend_mobile_etape1.ui.theme.FestivalAJouerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestivalAJouerTheme {
                AppNavGraph()
            }
        }
    }
}
