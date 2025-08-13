package com.example.timer

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.runtime.mutableLongStateOf

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null // Zil sesi için MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Timer uygulamasını başlatıyoruz
            TimerApp(onTimerFinished = {
                playAlarmSound() // Timer bittiğinde ses çal
            })
        }
    }

    // Zil sesini çalma fonksiyonu
    private fun playAlarmSound() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm_short_c)
            mediaPlayer?.setOnCompletionListener {
                it.release() // Ses bittiğinde kaynakları temizle
                mediaPlayer = null
            }
        }
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Aktivite kapanınca MediaPlayer'ı temizle
        mediaPlayer = null
    }
}

@Composable
fun TimerApp(onTimerFinished: () -> Unit) {
    var totalTime by remember { mutableLongStateOf(60000L) } // Default 1 dakika
    var timeLeft by remember { mutableLongStateOf(totalTime) }
    var running by remember { mutableStateOf(false) } // Timer çalışıyor mu?
    val context = LocalContext.current

    var input by remember { mutableStateOf("1") } // Kullanıcının dakika girişi

    // Timer çalıştırma bloğu
    LaunchedEffect(running) {
        while (running && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1000
        }
        if (timeLeft <= 0 && running) {
            running = false
            Toast.makeText(context, "Timer finished!", Toast.LENGTH_SHORT).show()
            onTimerFinished()
        }
    }

    val minutes = (timeLeft / 60000) % 60
    val seconds = (timeLeft / 1000) % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center, // Tüm içerik dikey ortada
        horizontalAlignment = Alignment.CenterHorizontally // Yatay ortala
    ) {
        // Sayaç göstergesi
        Text(
            text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Önerilen dakikalar butonları
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val suggestions = listOf(1, 5, 10, 15, 30)
            suggestions.forEach { minute ->
                Button(onClick = {
                    input = minute.toString()
                    totalTime = minute * 60000L
                    if (!running) {
                        timeLeft = totalTime
                    }
                }) {
                    Text("$minute min")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Set Timer yazısı + sayı kutusu
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Set Timer Duration (minutes):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = input,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() }) {
                        input = it
                        val minutesInput = it.toLongOrNull() ?: 1L
                        totalTime = minutesInput * 60000L
                        if (!running) {
                            timeLeft = totalTime
                        }
                    }
                },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start / Stop / Reset butonları
        Row {
            Button(onClick = { running = true }, enabled = timeLeft > 0 && !running) {
                Text("Start")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { running = false }, enabled = running) {
                Text("Stop")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                running = false
                timeLeft = totalTime
            }) {
                Text("Reset")
            }
        }
    }
}
