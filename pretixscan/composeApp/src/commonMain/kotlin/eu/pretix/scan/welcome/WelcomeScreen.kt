package eu.pretix.scan.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import eu.pretix.desktop.app.navigation.Route
import eu.pretix.desktop.app.ui.*
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

    Column {
        Toolbar()

        ScreenContentRoot {

            Box {
                Row {
                    Column(
                        modifier = Modifier.fillMaxHeight().padding(start = 64.dp, end = 32.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.pretix_logo_dark_angled),
                            contentDescription = "Pretix logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.width(320.dp).padding(bottom = 64.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxHeight().padding(start = 32.dp, end = 64.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                stringResource(Res.string.headline_setup),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        Row(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(stringResource(Res.string.welcome_text), style = MaterialTheme.typography.bodyLarge)
                        }

                        Row(modifier = Modifier.padding(vertical = 16.dp)) {
                            CheckboxWithLabel(
                                label = stringResource(Res.string.welcome_disclaimer1),
                                description = "",
                                checked = acceptedTerms,
                            ) { acceptedTerms = it }
                        }

                        Row(
                            Modifier.fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
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
}


@Composable
private fun Toolbar(
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CustomColor.BrandDark.asColor())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Logo()
        Spacer(Modifier.weight(1f))
    }
}