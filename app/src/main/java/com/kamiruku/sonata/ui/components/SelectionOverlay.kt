package com.kamiruku.sonata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.SelectionOverlay(
    isSelected: Boolean
) {
    Box(
        Modifier
            .align(Alignment.CenterStart)
            .padding(35.dp)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
    ) {
        Icon(
            imageVector =
                if (isSelected) Icons.Outlined.CheckBox
                else Icons.Outlined.CheckBoxOutlineBlank,
            contentDescription =
                if (isSelected) "selected"
                else "not_selected",
            tint = MaterialTheme.colorScheme.primary,
        )
    }

    if (isSelected) {
        Box(
            Modifier
                .padding(vertical = 16.dp)
                .padding(horizontal = 25.dp)
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(0.3f))
        )
    }
}