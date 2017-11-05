package eu.pretix.pretixdesk.ui.helpers

import com.jfoenix.controls.*
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.paint.Color
import javafx.util.StringConverter
import tornadofx.*
import java.time.LocalDate


enum class ColorPickerMode { Button, MenuButton, SplitMenuButton }

fun EventTarget.jfxColorpicker(color: Color? = null, mode: ColorPickerMode = ColorPickerMode.Button, op: (JFXColorPicker.() -> Unit)? = null): JFXColorPicker {
    val picker = JFXColorPicker()
    if (mode == ColorPickerMode.MenuButton) picker.addClass(ColorPicker.STYLE_CLASS_BUTTON)
    else if (mode == ColorPickerMode.SplitMenuButton) picker.addClass(ColorPicker.STYLE_CLASS_SPLIT_BUTTON)
    if (color != null) picker.value = color
    return opcr(this, picker, op)
}

fun EventTarget.jfxTabpane(op: (TabPane.() -> Unit)? = null) = opcr(this, JFXTabPane(), op)

fun EventTarget.jfxTextfield(value: String? = null, op: (JFXTextField.() -> Unit)? = null) = opcr(this, JFXTextField().apply { if (value != null) text = value }, op)


fun EventTarget.jfxTextfield(property: ObservableValue<String>, op: (JFXTextField.() -> Unit)? = null) = jfxTextfield().apply {
    bind(property)
    op?.invoke(this)
}

@JvmName("textfieldNumber")
fun EventTarget.jfxTextfield(property: ObservableValue<Number>, op: (JFXTextField.() -> Unit)? = null) = jfxTextfield().apply {
    bind(property)
    op?.invoke(this)
}

fun EventTarget.jfxPasswordfield(value: String? = null, op: (PasswordField.() -> Unit)? = null) = opcr(this, JFXPasswordField().apply { if (value != null) text = value }, op)
fun EventTarget.jfxPasswordfield(property: ObservableValue<String>, op: (JFXPasswordField.() -> Unit)? = null) = jfxPasswordfield().apply {
    bind(property)
    op?.invoke(this)
}

fun <T> EventTarget.jfxTextfield(property: Property<T>, converter: StringConverter<T>, op: (JFXTextField.() -> Unit)? = null) = jfxTextfield().apply {
    textProperty().bindBidirectional(property, converter)
    ViewModel.register(textProperty(), property)
    op?.invoke(this)
}

fun EventTarget.jfxDatepicker(op: (JFXDatePicker.() -> Unit)? = null) = opcr(this, JFXDatePicker(), op)
fun EventTarget.jfxDatepicker(property: Property<LocalDate>, op: (JFXDatePicker.() -> Unit)? = null) = jfxDatepicker().apply {
    bind(property)
    op?.invoke(this)
}

fun EventTarget.jfxTextarea(value: String? = null, op: (JFXTextArea.() -> Unit)? = null) = opcr(this, JFXTextArea().apply { if (value != null) text = value }, op)
fun EventTarget.jfxTextarea(property: ObservableValue<String>, op: (JFXTextArea.() -> Unit)? = null) = jfxTextarea().apply {
    bind(property)
    op?.invoke(this)
}

fun <T> EventTarget.jfxTextarea(property: Property<T>, converter: StringConverter<T>, op: (JFXTextArea.() -> Unit)? = null) = jfxTextarea().apply {
    textProperty().bindBidirectional(property, converter)
    ViewModel.register(textProperty(), property)
    op?.invoke(this)
}

fun EventTarget.jfxCheckbox(text: String? = null, property: Property<Boolean>? = null, op: (JFXCheckBox.() -> Unit)? = null) = opcr(this, JFXCheckBox(text).apply {
    if (property != null) bind(property)
}, op)


fun EventTarget.jfxProgressbar(initialValue: Double? = null, op: (JFXProgressBar.() -> Unit)? = null) = opcr(this, JFXProgressBar().apply { if (initialValue != null) progress = initialValue }, op)
fun EventTarget.jfxPprogressbar(property: ObservableValue<Number>, op: (JFXProgressBar.() -> Unit)? = null) = jfxProgressbar().apply {
    bind(property)
    op?.invoke(this)
}

fun EventTarget.jfxSlider(min: Number? = null, max: Number? = null, value: Number? = null, orientation: Orientation? = null, op: (JFXSlider.() -> Unit)? = null) = opcr(this, JFXSlider().apply {
    if (min != null) this.min = min.toDouble()
    if (max != null) this.max = max.toDouble()
    if (value != null) this.value = value.toDouble()
    if (orientation != null) this.orientation = orientation
}, op)

// Buttons
fun EventTarget.jfxButton(text: String = "", graphic: Node? = null, op: (JFXButton.() -> Unit)? = null): JFXButton {
    val button = JFXButton(text)
    if (graphic != null) button.graphic = graphic
    return opcr(this, button, op)
}

fun EventTarget.jfxButton(text: ObservableValue<String>, graphic: Node? = null, op: (JFXButton.() -> Unit)? = null): JFXButton {
    val button = JFXButton()
    button.textProperty().bind(text)
    if (graphic != null) button.graphic = graphic
    return opcr(this, button, op)
}

fun ToolBar.button(text: String = "", graphic: Node? = null, op: (JFXButton.() -> Unit)? = null): JFXButton {
    val button = JFXButton(text)
    if (graphic != null)
        button.graphic = graphic
    items.add(button)
    op?.invoke(button)
    return button
}

fun ToolBar.jfxButton(text: ObservableValue<String>, graphic: Node? = null, op: (JFXButton.() -> Unit)? = null): JFXButton {
    val button = JFXButton()
    button.textProperty().bind(text)
    if (graphic != null)
        button.graphic = graphic
    items.add(button)
    op?.invoke(button)
    return button
}

fun ButtonBar.jfxButton(text: String = "", type: ButtonBar.ButtonData? = null, graphic: Node? = null, op: (JFXButton.() -> Unit)? = null): JFXButton {
    val button = JFXButton(text)
    if (type != null)
        ButtonBar.setButtonData(button, type)
    if (graphic != null)
        button.graphic = graphic
    buttons.add(button)
    op?.invoke(button)
    return button
}

fun ButtonBar.jfxButton(text: ObservableValue<String>, type: ButtonBar.ButtonData? = null, graphic: Node? = null, op: (JFXButton.() -> Unit)? = null): JFXButton {
    val button = JFXButton()
    button.textProperty().bind(text)
    if (type != null)
        ButtonBar.setButtonData(button, type)
    if (graphic != null)
        button.graphic = graphic
    buttons.add(button)
    op?.invoke(button)
    return button
}

fun Node.jfxTogglebutton(text: String? = null, group: ToggleGroup? = getToggleGroup(), selectFirst: Boolean = true, value: Any? = null, op: (JFXToggleButton.() -> Unit)? = null) =
        opcr(this, JFXToggleButton().apply {
            this.text = if (value != null && text == null) value.toString() else text ?: ""
            properties["tornadofx.toggleGroupValue"] = value ?: text
            if (group != null) toggleGroup = group
            if (toggleGroup?.selectedToggle == null && selectFirst) isSelected = true
        }, op)


fun Node.jfxRadiobutton(text: String? = null, group: ToggleGroup? = getToggleGroup(), value: Any? = null, op: (JFXRadioButton.() -> Unit)? = null)
        = opcr(this, JFXRadioButton().apply {
    this.text = if (value != null && text == null) value.toString() else text ?: ""
    properties["tornadofx.toggleGroupValue"] = value ?: text
    if (group != null) toggleGroup = group
}, op)