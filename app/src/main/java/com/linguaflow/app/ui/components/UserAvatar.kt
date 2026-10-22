package com.linguaflow.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.ui.theme.PrimaryGreen
import java.io.File

@Composable
fun UserAvatar(
    avatar: String,
    isCustomPhoto: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    textSize: TextUnit = 24.sp,
    borderColor: Color = PrimaryGreen,
    borderWidth: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center
    ) {
        if (isCustomPhoto && avatar.isNotBlank()) {
            val bitmap = remember(avatar) {
                try {
                    val file = File(avatar)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Avatar de Usuario",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = "👤", fontSize = textSize)
            }
        } else {
            Text(
                text = if (avatar.isNotBlank()) avatar else "👤",
                fontSize = textSize
            )
        }
    }
}
