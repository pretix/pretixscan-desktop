package eu.pretix.desktop.app.ui


import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.composables.core.*
import com.composeunstyled.Button
import kotlin.time.Duration.Companion.seconds


@Composable
fun SimpleCombobox(
    state: SimpleComboboxState,
    modifier: Modifier = Modifier,
    content: @Composable MenuScope.() -> Unit
) {
    val scope = remember(state.expanded) { MenuScope(state) }

    Box(modifier.onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        when (event.key) {
            Key.DirectionDown -> {
                if (scope.menuState.expanded.not()) {
                    scope.menuState.expanded = true
                    true
                } else {
                    false
                }
            }

            else -> false
        }
    }) {
        state.currentFocusManager = LocalFocusManager.current
        scope.content()
    }
}

@Stable
class SimpleComboboxState(
    initialOptions: List<SelectableValue>,
    initialSelectedValue: String?,
    initialExpanded: Boolean = false
) {
    var options = mutableStateListOf<SelectableValue>().apply { addAll(initialOptions) }
        private set

    var expanded by mutableStateOf(initialExpanded)
    internal val menuFocusRequester = FocusRequester()
    internal var currentFocusManager by mutableStateOf<FocusManager?>(null)
    internal var hasMenuFocus by mutableStateOf(false)

    val focusRequesters = List(options.size) { FocusRequester() }

    var selectedIndex = options.indexOfFirst { it.value == initialSelectedValue }
    fun focusItem(index: Int) {
        focusRequesters.getOrNull(index)?.requestFocus()
    }

    fun focusItem(option: SelectableValue) {
        val index = options.indexOf(option)
        focusRequesters.getOrNull(index)?.requestFocus()
    }
}


@Composable
fun rememberSimpleComboboxState(
    options: List<SelectableValue>,
    selectedValue: String?,
    expanded: Boolean = false
): SimpleComboboxState {
    return remember { SimpleComboboxState(options, selectedValue, expanded) }
}

/**
 * A button component that triggers the menu's expanded state when clicked.
 *
 * @param modifier Modifier to be applied to the button.
 * @param mutableInteractionSource The interaction source for the button.
 * @param indication The indication to be shown when the button is interacted with.
 * @param enabled Whether the button is enabled.
 * @param shape The shape of the button.
 * @param backgroundColor The background color of the button.
 * @param contentColor The color to apply to the contents of the button.
 * @param contentPadding Padding values for the content.
 * @param borderColor The color of the border.
 * @param borderWidth The width of the border.
 * @param horizontalArrangement The horizontal arrangement of the button's children.
 * @param verticalAlignment The vertical alignment of the button's children.
 * @param contents A composable function that defines the content of the button.
 */
@Composable
fun MenuScope.MenuButton(
    modifier: Modifier = Modifier,
    mutableInteractionSource: MutableInteractionSource? = null,
    indication: Indication = LocalIndication.current,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = NoPadding,
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    contents: @Composable () -> Unit
) {
    Button(
        onClick = {
            menuState.expanded = menuState.expanded.not()
        },
        role = Role.DropdownList,
        enabled = enabled,
        contentColor = contentColor,
        contentPadding = contentPadding,
        borderColor = borderColor,
        borderWidth = borderWidth,
        interactionSource = mutableInteractionSource,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        indication = indication,
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
    ) {
        contents()
    }
}

@Stable
class MenuScope internal constructor(state: SimpleComboboxState) {
    internal var menuState by mutableStateOf(state)
}


// Code modified from Material 3 DropdownMenu.kt
// https://github.com/JetBrains/compose-multiplatform-core/blob/e62838f496d592c019a3539669a9fbfd33928121/compose/material/material/src/commonMain/kotlin/androidx/compose/material/Menu.kt
@Immutable
internal data class MenuContentPositionProvider(val density: Density, val alignment: Alignment.Horizontal) :
    PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize
    ): IntOffset { // The min margin above and below the menu, relative to the screen.
        // The content offset specified using the dropdown offset parameter.

        // Compute horizontal position.
        val toRight = anchorBounds.left
        val toLeft = anchorBounds.right - popupContentSize.width

        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0

        val x = (if (alignment == Alignment.Start) {
            sequenceOf(
                toRight, toLeft,
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else if (alignment == Alignment.End) {
            sequenceOf(
                toLeft, toRight, // If the anchor gets outside of the window on the right, we want to position
                // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
            )
        } else { // middle
            sequenceOf(anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2)
        }).firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft

        // Compute vertical position.
        val toBottom = maxOf(anchorBounds.bottom, 0)
        val toTop = anchorBounds.top - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height
        val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
            it >= 0 && it + popupContentSize.height <= windowSize.height
        } ?: toTop

        return IntOffset(x, y)
    }
}

fun KeyEvent.getInputChar(): Char? {
    if (type != KeyEventType.KeyDown) return null
    val shift = isShiftPressed
    return when (key) {
        Key.A -> if (shift) 'A' else 'a'
        Key.B -> if (shift) 'B' else 'b'
        Key.C -> if (shift) 'C' else 'c'
        Key.D -> if (shift) 'D' else 'd'
        Key.E -> if (shift) 'E' else 'e'
        Key.F -> if (shift) 'F' else 'f'
        Key.G -> if (shift) 'G' else 'g'
        Key.H -> if (shift) 'H' else 'h'
        Key.I -> if (shift) 'I' else 'i'
        Key.J -> if (shift) 'J' else 'j'
        Key.K -> if (shift) 'K' else 'k'
        Key.L -> if (shift) 'L' else 'l'
        Key.M -> if (shift) 'M' else 'm'
        Key.N -> if (shift) 'N' else 'n'
        Key.O -> if (shift) 'O' else 'o'
        Key.P -> if (shift) 'P' else 'p'
        Key.Q -> if (shift) 'Q' else 'q'
        Key.R -> if (shift) 'R' else 'r'
        Key.S -> if (shift) 'S' else 's'
        Key.T -> if (shift) 'T' else 't'
        Key.U -> if (shift) 'U' else 'u'
        Key.V -> if (shift) 'V' else 'v'
        Key.W -> if (shift) 'W' else 'w'
        Key.X -> if (shift) 'X' else 'x'
        Key.Y -> if (shift) 'Y' else 'y'
        Key.Z -> if (shift) 'Z' else 'z'
        Key.Zero -> '0'
        Key.One -> '1'
        Key.Two -> '2'
        Key.Three -> '3'
        Key.Four -> '4'
        Key.Five -> '5'
        Key.Six -> '6'
        Key.Seven -> '7'
        Key.Eight -> '8'
        Key.Nine -> '9'
        Key.Spacebar -> ' '
        Key.Tab -> '\t'
        Key.Enter -> '\n'
        else -> null
    }
}

/**
 * The content container for the menu items. This composable handles the positioning and animation
 * of the menu content when it is expanded.
 *
 * @param modifier Modifier to be applied to the content container.
 * @param enter The enter transition for the content.
 * @param exit The exit transition for the content.
 * @param alignment The horizontal alignment of the content relative to the menu button.
 * @param contents A composable function that defines the content of the menu.
 */
@Composable
fun MenuScope.MenuContent(
    modifier: Modifier = Modifier,
    enter: EnterTransition = AppearInstantly,
    exit: ExitTransition = DisappearInstantly,
    alignment: Alignment.Horizontal = Alignment.Start,
    contents: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val positionProvider = MenuContentPositionProvider(density, alignment)
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = menuState.expanded
    menuState.currentFocusManager = LocalFocusManager.current

    val scrollState = rememberScrollState()
    val state = rememberScrollAreaState(scrollState)

    if (expandedState.currentState || expandedState.targetState || !expandedState.isIdle) {
        Popup(
            properties = PopupProperties(
                focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true
            ),
            onDismissRequest = {
                menuState.expanded = false
                menuState.currentFocusManager?.clearFocus()
            },
            popupPositionProvider = positionProvider,
        ) {
            menuState.currentFocusManager = LocalFocusManager.current
            AnimatedVisibility(
                visibleState = expandedState,
                enter = enter,
                exit = exit,
                modifier = Modifier.onFocusChanged {
                    menuState.hasMenuFocus = it.hasFocus
                }.onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false

                    return@onKeyEvent when (event.key) {
                        Key.DirectionDown -> {
                            // for some reason moving focus once is not enough, maybe a container is focusable?
                            menuState.currentFocusManager!!.moveFocus(FocusDirection.Next)
                            menuState.currentFocusManager!!.moveFocus(FocusDirection.Next)
                            true
                        }

                        Key.DirectionUp -> {
                            // for some reason moving focus once is not enough, maybe a container is focusable?
                            menuState.currentFocusManager!!.moveFocus(FocusDirection.Previous)
                            menuState.currentFocusManager!!.moveFocus(FocusDirection.Previous)
                            true
                        }

                        Key.Escape -> {
                            menuState.expanded = false
                            true
                        }

                        else -> {
                            val char = event.getInputChar()
                            if (char != null) {
                                val matchingOption = menuState.options.firstOrNull {
                                    // first match exactly with case, if none is found, match case insensitive
                                    (it.label.trim().startsWith(char) || it.label.trim().lowercase()
                                        .startsWith(char.lowercase()))
                                }
                                if (matchingOption != null) {
                                    val matchIndex = menuState.options.indexOf(matchingOption)
                                    println("found match for $char at index ${matchIndex} for label ${matchingOption.label} and value ${matchingOption.value}, requesting focus")
                                    menuState.focusItem(matchingOption)
                                    true
                                } else false
                            } else false
                        }
                    }
                }
            ) {
                ScrollArea(state = state) {
                    Box {
                        Column(modifier.focusRequester(menuState.menuFocusRequester).verticalScroll(scrollState)) {
                            LaunchedEffect(Unit) {
                                menuState.menuFocusRequester.requestFocus()
                            }
                            contents()
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.TopEnd).heightIn(max = scrollState.maxValue.dp)
                        ) {
                            Thumb(
                                modifier = Modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(100)),
                                thumbVisibility = ThumbVisibility.HideWhileIdle(
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    hideDelay = 1.5.seconds
                                )
                            )
                        }
                    }

                }
            }
        }
    }
}

/**
 * A menu item that can be clicked to perform an action and dismiss the menu.
 *
 * @param onClick The callback to be invoked when the menu item is clicked.
 * @param modifier Modifier to be applied to the menu item.
 * @param enabled Whether the menu item is enabled.
 * @param interactionSource The interaction source for the menu item.
 * @param indication The indication to be shown when the menu item is interacted with.
 * @param contentPadding Padding values for the content.
 * @param shape The shape of the menu item.
 * @param horizontalArrangement The horizontal arrangement of the menu item's children.
 * @param verticalAlignment The vertical alignment of the menu item's children.
 * @param contents A composable function that defines the content of the menu item.
 */
@Composable
fun MenuScope.MenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication = LocalIndication.current,
    contentPadding: PaddingValues = NoPadding,
    shape: Shape = RectangleShape,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    selectionIndex: Int,
    contents: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(Unit) {
        if (menuState.selectedIndex == selectionIndex || menuState.selectedIndex == -1 && selectionIndex == 0) {
            menuState.focusItem(menuState.selectedIndex)
        }
    }
    Box(
        modifier = if (isFocused)
            Modifier.border(2.dp, CustomColor.BrandDark.asColor(), shape).clip(shape)
        else
            Modifier
    ) {

        Button(
            onClick = {
                onClick()
                menuState.expanded = false
                menuState.currentFocusManager?.clearFocus()
            },
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            indication = indication,
            shape = shape,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
        ) {
            contents()
        }
    }
}
