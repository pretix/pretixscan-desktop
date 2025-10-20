package eu.pretix.desktop.app.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pretixscan.composeapp.generated.resources.*

@Composable
fun SyncProgressView(syncState: SyncState) {
    val statusHelper = koinInject<SyncStatusHelper>()

    when (syncState) {
        is SyncState.Error -> {
            SyncError(syncState)
        }

        is SyncState.InProgress -> {
            Text("Sync ${syncState.message}", color = statusHelper.getColor())
            LinearProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
        }

        is SyncState.Success -> {
            SyncedTimeAgo(
                statusHelper.isNever(),
                statusHelper.daysAgo(),
                statusHelper.hoursAgo(),
                statusHelper.minutesAgo()
            )
        }

        SyncState.Idle -> {
            SyncedTimeAgo(
                statusHelper.isNever(),
                statusHelper.daysAgo(),
                statusHelper.hoursAgo(),
                statusHelper.minutesAgo()
            )
        }
    }
}

@Composable
fun SyncProgressPanel(modifier: Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(bottomStart = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .background(CustomColor.White.asColor())
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}


@Composable
fun SyncedTimeAgo(never: Boolean, daysAgo: Int, hoursAgo: Int, minutesAgo: Int) {
    val statusHelper = koinInject<SyncStatusHelper>()

    when {
        never -> {
            Text(stringResource(Res.string.sync_status_never), color = statusHelper.getColor())
        }

        statusHelper.isDaysAgo() -> {
            println("days ago ${statusHelper.daysAgo()}")
            Text(
                pluralStringResource(
                    Res.plurals.sync_status_time_days,
                    daysAgo,
                    daysAgo
                ), color = statusHelper.getColor()
            )
        }

        statusHelper.isHoursAgo() -> {
            Text(
                pluralStringResource(
                    Res.plurals.sync_status_time_hours,
                    hoursAgo,
                    hoursAgo
                ), color = statusHelper.getColor()
            )
        }

        statusHelper.isMinutesAgo() -> {
            Text(
                pluralStringResource(
                    Res.plurals.sync_status_time_minutes,
                    minutesAgo,
                    minutesAgo
                ), color = statusHelper.getColor()
            )
        }

        else -> {
            Text(stringResource(Res.string.sync_status_now), color = statusHelper.getColor())
        }
    }
}

@Composable
fun SyncError(state: SyncState.Error) {
    Text(
        "Last synchronization failed: ${state.message}",
        color = CustomColor.BrandRed.asColor()
    )
}