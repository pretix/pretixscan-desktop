package eu.pretix.desktop.app.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import com.composables.core.HorizontalSeparator
import com.composables.core.Icon
import com.composeunstyled.Text
import eu.pretix.desktop.app.ui.modifiers.conditional
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ic_arrow_drop_down_black_24dp
import pretixscan.composeapp.generated.resources.text_action_select

@Composable
fun FieldSpinnerItem(label: String) {
    Text(
        label,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun FieldSpinner(
    modifier: Modifier = Modifier,
    selectedValue: String?,
    availableOptions: List<SelectableValue>,
    onSelect: (SelectableValue?) -> Unit
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val options = availableOptions
        val state = rememberSimpleComboboxState(options, selectedValue)

        val selectedOption = availableOptions.firstOrNull { it.value == selectedValue }
        val hasSelectedOption = selectedOption != null

        LaunchedEffect(selectedOption) {
            state.selectedIndex = availableOptions.indexOf(selectedOption)
        }

        SimpleCombobox(state = state) {
            Box(Modifier.wrapContentWidth()) {
                MenuButton(
                    Modifier.padding(top = 4.dp, end = 4.dp, bottom = 4.dp).clip(RoundedCornerShape(8.dp)),
                    backgroundColor = Color.White,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Column(Modifier.requiredWidthIn(max = 240.dp)) {
                            if (hasSelectedOption) {
                                if (selectedOption.buttonContent != null) {
                                    selectedOption.buttonContent()
                                } else if (selectedOption.content != null) {
                                    selectedOption.content()
                                } else {
                                    FieldSpinnerItem(selectedOption.label)
                                }
                            } else {
                                FieldSpinnerItem(stringResource(Res.string.text_action_select))
                            }
                        }


                        Image(
                            painter = painterResource(Res.drawable.ic_arrow_drop_down_black_24dp),
                            contentDescription = null,
                            Modifier.requiredWidth(24.dp)
                        )
                    }
                }
            }

            MenuContent(
                modifier = Modifier.padding(vertical = 4.dp)
                    .width(240.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                enter = scaleIn(
                    animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing),
                    initialScale = 0.8f,
                    transformOrigin = TransformOrigin(0f, 0f)
                ) + fadeIn(tween(durationMillis = 30)),
                exit = scaleOut(
                    animationSpec = tween(durationMillis = 1, delayMillis = 75),
                    targetScale = 1f
                ) + fadeOut(tween(durationMillis = 75))
            ) {
                options.forEachIndexed { index, item ->
                    MenuItem(
                        modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(8.dp))
                            .focusable()
                            .conditional(index < state.focusRequesters.size, {
                                focusRequester(state.focusRequesters[index])
                            }),
                        onClick = {
//                            state.selectedOption = item
                            onSelect(item)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        selectionIndex = index
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.selectedIndex == index) {
                                // show a checkbox image
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CustomColor.BrandDark.asColor()
                                )
                            }
                            if (item.content != null) {
                                item.content()
                            } else {
                                FieldSpinnerItem(item.label)
                            }
                        }
                    }

                    if (index < options.size - 1) {
                        HorizontalSeparator(color = Color(0xFFBDBDBD))
                    }
                }
            }
        }
    }
}
