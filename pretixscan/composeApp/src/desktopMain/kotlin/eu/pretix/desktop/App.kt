package eu.pretix.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.setup.SetupManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        var code by remember { mutableStateOf("") }
        val focusRequester = FocusRequester()

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )

            Button(onClick = {
                coroutineScope.launch {
                    submitCode(code)
                }
            }) {
                Text("Sign in")
            }
        }
    }
}

suspend fun submitCode(code: String) {
    println("Connecting...")
    val httpFactory = DefaultHttpClientFactory()
    val manager = SetupManager(
        "brand", "model", "os", "version", "brand", "version", httpFactory
    )
    val result = manager.initialize(
        "https://pretix.eu",
        code
    )
    println("New device token: ${result.api_token}")
}