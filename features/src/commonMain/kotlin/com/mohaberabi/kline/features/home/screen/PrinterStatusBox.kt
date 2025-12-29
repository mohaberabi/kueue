package com.mohaberabi.kline.features.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PrinterStatusBox(
    icon: ImageVector,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        Modifier.wrapContentSize()
            .clip(RoundedCornerShape(12.dp))
            .size(50.dp)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            "",
            tint = Color.White
        )

    }

}