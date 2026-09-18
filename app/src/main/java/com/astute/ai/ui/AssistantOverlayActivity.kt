package com.astute.ai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astute.ai.ui.components.PlasmaOrbView

class AssistantOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var activeTranscript by remember { mutableStateOf("Tell me, I'm listening...") }
            var commandStatus by remember { mutableStateOf("Say 'Open YouTube' or a custom task") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xF0120024), Color(0xFA240046), Color(0xFF090014))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Astute AI Assistant",
                        color = Color.White,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = activeTranscript,
                        color = Color(0xFFE1BEE7),
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    PlasmaOrbView(isListening = true)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 50.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = commandStatus,
                        color = Color(0xFFB39DDB),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                    Button(
                        onClick = { finish() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}
