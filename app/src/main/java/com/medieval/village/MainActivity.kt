package com.medieval.village

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.medieval.village.ui.GameRoot
import com.medieval.village.ui.theme.MedievalTheme
import com.medieval.village.ui.theme.Palette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App() {
    MedievalTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Palette.WoodDark) {
            GameRoot(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Palette.WoodDark)
                    .windowInsetsPadding(WindowInsets.systemBars)
            )
        }
    }
}
