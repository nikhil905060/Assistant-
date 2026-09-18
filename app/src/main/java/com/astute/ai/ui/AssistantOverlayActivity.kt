package com.astute.ai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.astute.ai.network.GeminiClient
import com.astute.ai.service.DeviceAutomationService
import com.astute.ai.ui.components.PlasmaOrbView
import kotlinx.coroutines.launch
import java.util.Locale

class AssistantOverlayActivity : ComponentActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var activeTranscriptState = mutableStateOf("Listening...")
    private var executionStepState = mutableStateOf("Say something like 'Open YouTube'")
    private var isListeningState = mutableStateOf(false)

    // Replace with your valid Gemini API Key for processing
    private val geminiClient = GeminiClient(apiKey = "YOUR_GEMINI_API_KEY")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            activeTranscriptState.value = "Microphone permission denied."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initSpeechRecognizer()

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
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier.align(Alignment.Center),
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

        checkAndStartListening()
    }

    private fun checkAndStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListeningState.value = true
                        activeTranscriptState.value = "Listening..."
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListeningState.value = false
                        activeTranscriptState.value = "Processing command..."
                    }

                    override fun onError(error: Int) {
                        isListeningState.value = false
                        activeTranscriptState.value = "Error listening (Code: $error). Tap to retry."
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognizedText = matches[0]
                            activeTranscriptState.value = "\"$recognizedText\""
                            handleSpokenCommand(recognizedText)
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
            }
        } else {
            activeTranscriptState.value = "Speech Recognition not available on device"
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun handleSpokenCommand(command: String) {
        val lower = command.lowercase(Locale.ROOT)
        executionStepState.value = "Executing: $command"

        // Basic local command routing
        when {
            lower.contains("youtube") -> {
                executionStepState.value = "Opening YouTube..."
                DeviceAutomationService.instance?.launchApp("com.google.android.youtube")
            }
            lower.contains("settings") -> {
                executionStepState.value = "Opening Settings..."
                DeviceAutomationService.instance?.launchApp("com.android.settings")
            }
            else -> {
                // Send command to Gemini API for complex intent breakdown
                lifecycleScope.launch {
                    try {
                        val response = geminiClient.analyzeCommand(command)
                        executionStepState.value = "AI Response received"
                    } catch (e: Exception) {
                        executionStepState.value = "Execution failed: ${e.localizedMessage}"
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}
