package screen.setup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.setup.SetupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel: ViewModel() {
    private val _text = MutableStateFlow("Not started")
    val text: Flow<String> = _text

    fun verifyToken(token: String, url: String) {
        _text.update { "Connecting ..." }
        val httpFactory = DefaultHttpClientFactory()
        val manager = SetupManager(
            "brand", "model", "os", "version", "brand", "version", httpFactory
        )
        try {
            val result = manager.initialize(
                url,
                token
            )
            _text.update { "New device token: ${result.api_token}" }
        } catch (e: Exception) {
            _text.update { "Failed: ${e.localizedMessage}\n${e.stackTraceToString()}" }
        }
    }
}