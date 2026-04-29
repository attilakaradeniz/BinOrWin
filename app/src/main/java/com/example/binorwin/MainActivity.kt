package com.example.binorwin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.binorwin.ui.components.PostCard
import com.example.binorwin.ui.theme.BinOrWinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BinOrWinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Displaying our new custom PostCard component
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PostCard()
                    }
                }
            }
        }
    }
}