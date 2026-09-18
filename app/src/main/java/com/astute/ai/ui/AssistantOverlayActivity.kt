package com.astute.ai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.core.content.ContextCompat
import com.astute.ai.service.DeviceAutomationService
import com.astute.ai.ui.components.PlasmaOrbView
import java.util.Locale

class AssistantOverlayActivity : ComponentActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var activeTranscriptState = mutableStateOf("Ready to listen")
    private var executionStepState = mutableStateOf("Tap the orb or speak 'Open YouTube'")
    private var isListeningState = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            initAndListen()
        } else {
            activeTranscriptState.value = "Microphone permission required!"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val transcript by remember { activeTranscriptState }
            val executionStep by remember { executionStepState }
            val isListening by remember { isListeningState }

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
                        text = transcript,
                        color = Color(0xFFE1BEE7),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable { initAndListen() },
                    contentAlignment = Alignment.Center
                ) {
                    PlasmaOrbView(isListening = isListening)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 50.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = executionStep,
                        color = Color(0xFFB39DDB),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { initAndListen() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                        ) {
                            Text("Retry Mic", color = Color.White)
                        }
                        Button(
                            onClick = { finish() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF311B92))
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Handler(Looper.getMainLooper()).postDelayed({
                initAndListen()
            }, 300)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun initAndListen() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null

            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                activeTranscriptState.value = "Speech recognition unavailable"
                return
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListeningState.value = true
                    activeTranscriptState.value = "Listening... Speak now!"
                }

                override fun onBeginningOfSpeech() {
                    activeTranscriptState.value = "Hearing you..."
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListeningState.value = false
                    activeTranscriptState.value = "Processing..."
                }

                override fun onError(error: Int) {
                    isListeningState.value = false
                    activeTranscriptState.value = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Tap orb to try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout. Tap orb to speak."
                        else -> "Mic status ($error). Tap Retry."
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        activeTranscriptState.value = "\"$spokenText\""
                        handleCommand(spokenText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!partial.isNullOrEmpty()) {
                        activeTranscriptState.value = partial[0]
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            activeTranscriptState.value = "Error: ${e.message}"
        }
    }

    private fun handleCommand(command: String) {
        val lower = command.lowercase(Locale.ROOT)
        executionStepState.value = "Running: $command"

        when {
            lower.contains("youtube") -> {
                executionStepState.value = "Opening YouTube..."
                DeviceAutomationService.instance?.launchApp("com.google.android.youtube")
                finish()
            }
            lower.contains("settings") -> {
                executionStepState.value = "Opening Settings..."
                DeviceAutomationService.instance?.launchApp("com.android.settings")
                finish()
            }
            else -> {
                executionStepState.value = "Command: $command"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}
