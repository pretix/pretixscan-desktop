package screen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import navigation.Route
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.*
import ui.CustomColor
import ui.parseColor

@Composable
@Preview
fun WelcomeScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    var acceptedTerms by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier.fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.headline_setup))
                Text(stringResource(Res.string.welcome_text), textAlign = TextAlign.Center)
            }
        }

        Row(
            Modifier.fillMaxHeight()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .weight(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_white),
                contentDescription = "Pretix logo",
                modifier = Modifier.background(Color(parseColor(CustomColor.BrandDark.hex)))
            )
        }

        Row(
            Modifier.fillMaxWidth()
                .height(56.dp)
                .toggleable(
                    value = acceptedTerms,
                    onValueChange = { acceptedTerms = !acceptedTerms },
                    role = Role.Checkbox
                )
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptedTerms,
                onCheckedChange = null // null recommended for accessibility with screenreaders
            )
            Text(
                text = stringResource(Res.string.welcome_disclaimer1),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Row(
            Modifier.fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(parseColor(CustomColor.BrandGreen.hex))),
                enabled = acceptedTerms,
                onClick = {
                    navHostController.navigate(route = Route.Setup.route)
                }) {
                Text(stringResource(Res.string.cont))
            }
        }
    }
}