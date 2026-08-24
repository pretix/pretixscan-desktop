package eu.pretix.desktop.app.ui


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.composeunstyled.Thumb
import com.composeunstyled.ToggleSwitch


@Composable
fun CheckboxWithLabel(
    label: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .selectable(
                selected = checked,
                onClick = { onCheckedChange(!checked) },
                indication = LocalIndication.current,
                interactionSource = null,
                role = Role.Switch
            ).padding(top = 4.dp, end = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (!description.isNullOrBlank()) {
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
        val animatedColor by animateColorAsState(
            if (checked) CustomColor.BrandDark.asColor() else Color(0xFFE0E0E0)
        )
        ToggleSwitch(
            toggled = checked,
            shape = RoundedCornerShape(100),
            backgroundColor = animatedColor,
            modifier = Modifier.width(58.dp),
            contentPadding = PaddingValues(4.dp),
        ) {
            Thumb(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.shadow(elevation = 4.dp, CircleShape),
            )
        }
    }

//
//    Row(
//        modifier = Modifier.toggleable(
//            value = checked,
//            onValueChange = onCheckedChange,
//            role = Role.Checkbox
//        )
//    ) {
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                label,
//                fontWeight = FontWeight.SemiBold
//            )
//            if (!description.isNullOrBlank()) {
//                Text(
//                    description,
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Light
//                )
//            }
//        }
//        Checkbox(
//            checked = checked,
//            onCheckedChange = null, // recommended for accessibility reasons, the .toggleable modifier on the row handles checks
//            modifier = Modifier.padding(start = 16.dp).padding(vertical = 16.dp)
//        )
//    }
}
