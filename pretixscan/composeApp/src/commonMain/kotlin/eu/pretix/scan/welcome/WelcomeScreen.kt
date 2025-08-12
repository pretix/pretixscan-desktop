package eu.pretix.scan.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.navigation.Route
import eu.pretix.desktop.app.ui.CheckboxWithLabel
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.ScreenContentRoot
import eu.pretix.desktop.app.ui.asColor
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.*

@Composable
@Preview
fun WelcomeScreen(
    navHostController: NavHostController,
) {
    var acceptedTerms by remember { mutableStateOf(false) }

    ScreenContentRoot {
        Row(
            modifier = Modifier.fillMaxSize().padding(64.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.padding(top = 64.dp)) {

                Image(
                    painter = painterResource(Res.drawable.pretix_logo_dark_angled),
                    contentDescription = "Pretix logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(320.dp)
                )

                Column(modifier = Modifier.padding(horizontal = 64.dp)) {
                    Text(stringResource(Res.string.headline_setup), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(Res.string.welcome_text), style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                    CheckboxWithLabel(
                        label = stringResource(Res.string.welcome_disclaimer1),
                        description = "",
                        checked = acceptedTerms,
                    ) { acceptedTerms = it }
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = CustomColor.BrandGreen.asColor()),
                            enabled = acceptedTerms,
                            onClick = {
                                navHostController.navigate(route = Route.Setup.route)
                            }) {
                            Text(stringResource(Res.string.cont))
                        }
                    }
                }

            }
        }

    }
}