package com.medieval.village.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** 양피지 / 목재 / 이끼 톤의 중세 팔레트 */
object Palette {
    val Ink = Color(0xFF231A10)
    val Parchment = Color(0xFFEFE0C0)
    val ParchmentDim = Color(0xFFD9C8A4)
    val Wood = Color(0xFF4A3524)
    val WoodDark = Color(0xFF2E2015)
    val WoodLight = Color(0xFF6B4E33)
    val Gold = Color(0xFFD9A441)
    val Blood = Color(0xFF9B3B2E)
    val Moss = Color(0xFF4E6B3A)
    val Sky = Color(0xFF4C6FA5)
    val Mana = Color(0xFF4A7FC1)
    val Health = Color(0xFFB5453A)
    val Exp = Color(0xFFC9A227)

    val Grass = Color(0xFF6F9A54)
    val GrassDark = Color(0xFF5E8748)
    val Dirt = Color(0xFFC2A16B)
    val DirtDark = Color(0xFFA9884F)
    val Stone = Color(0xFF9E9A90)
    val Water = Color(0xFF5B92C4)
}

private val scheme = darkColorScheme(
    primary = Palette.Gold,
    onPrimary = Palette.Ink,
    secondary = Palette.WoodLight,
    onSecondary = Palette.Parchment,
    background = Palette.WoodDark,
    onBackground = Palette.Parchment,
    surface = Palette.Wood,
    onSurface = Palette.Parchment,
    surfaceVariant = Palette.WoodLight,
    onSurfaceVariant = Palette.Parchment,
    error = Palette.Blood,
    outline = Palette.Gold
)

@Composable
fun MedievalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        content = content
    )
}
