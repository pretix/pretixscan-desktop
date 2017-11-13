package eu.pretix.pretixdesk.ui.helpers


import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXTreeView
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.ListView
import javafx.scene.control.TreeItem
import tornadofx.*

fun <T> EventTarget.jfxCombobox(property: Property<T>? = null, values: List<T>? = null, op: (JFXComboBox<T>.() -> Unit)? = null) = opcr(this, JFXComboBox<T>().apply {
    if (values != null) items = values as? ObservableList<T> ?: values.observable()
    if (property != null) bind(property)
}, op)


fun <T> EventTarget.listview(values: ObservableList<T>? = null, op: (JFXListView<T>.() -> Unit)? = null) = opcr(this, JFXListView<T>().apply {
    if (values != null) {
        if (values is SortedFilteredList<T>) values.bindTo(this)
        else items = values
    }
}, op)

fun <T> EventTarget.jfxListview(values: ReadOnlyListProperty<T>, op: (ListView<T>.() -> Unit)? = null) = jfxListview(values as ObservableValue<ObservableList<T>>, op)

fun <T> EventTarget.jfxListview(values: ObservableValue<ObservableList<T>>, op: (ListView<T>.() -> Unit)? = null) = opcr(this, JFXListView<T>().apply {
    fun rebinder() {
        (items as? SortedFilteredList<T>)?.bindTo(this)
    }
    itemsProperty().bind(values)
    rebinder()
    itemsProperty().onChange {
        rebinder()
    }
}, op)


fun <T> EventTarget.jfxTreeview(root: TreeItem<T>? = null, op: (JFXTreeView<T>.() -> Unit)? = null): JFXTreeView<T> {
    val treeview = JFXTreeView<T>()
    if (root != null) treeview.root = root
    return opcr(this, treeview, op)
}