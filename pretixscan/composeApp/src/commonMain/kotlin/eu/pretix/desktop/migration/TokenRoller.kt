package eu.pretix.desktop.migration

import eu.pretix.desktop.cache.DataStoreConfig
import eu.pretix.libpretixsync.api.HttpClientFactory
import eu.pretix.libpretixsync.api.PretixApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.logging.Logger

class TokenRoller(
    private val dataStoreConfig: DataStoreConfig,
    private val httpFactory: HttpClientFactory
) {
    private val logger = Logger.getLogger(TokenRoller::class.java.name)

    /**
     * Roll the device API token by calling /device/roll endpoint.
     * Returns Result with new API key or exception.
     */
    suspend fun rollApiToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            logger.info("Rolling device API token via /device/roll endpoint")

            // Manually create PretixApi instance using migrated config
            val api = PretixApi(
                dataStoreConfig.getApiUrl(),
                dataStoreConfig.getApiKey(),
                dataStoreConfig.getOrganizerSlug(),
                dataStoreConfig.getApiVersion(),
                httpFactory
            )

            val response = api.postResource(
                api.apiURL("device/roll"),
                JSONObject()
            )

            val newKey = response.data?.getString("api_token")
                ?: throw IllegalStateException("No api_token in response")

            logger.info("Token rolled successfully, new key received")
            Result.success(newKey)
        } catch (e: Exception) {
            logger.warning("Token rolling failed (non-fatal): ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Update DataStore with the new API key after successful rolling.
     */
    suspend fun updateApiKey(newKey: String) {
        dataStoreConfig.setApiKey(newKey)
        logger.info("DataStore updated with new API key")
    }
}
