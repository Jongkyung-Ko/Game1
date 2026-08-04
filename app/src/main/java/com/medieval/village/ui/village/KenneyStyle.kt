package com.medieval.village.ui.village

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/** Kenney식 청크 스프라이트: 두꺼운 외곽선 + 납작한 면색 */
object Kenney {
    val Outline = Color(0xFF2B2118)
    val Grass = Color(0xFF6DB35A)
    val GrassDark = Color(0xFF4F8F42)
    val GrassLight = Color(0xFF8FCF72)
    val Dirt = Color(0xFFD2A86A)
    val DirtDark = Color(0xFFB08648)
    val DirtLight = Color(0xFFE4C28A)
    val Wood = Color(0xFF8B5A2B)
    val WoodDark = Color(0xFF6B4020)
    val RoofRed = Color(0xFFC45A45)
    val RoofBlue = Color(0xFF5A7EAE)
    val RoofBrown = Color(0xFF8A5A2B)
    val WallCream = Color(0xFFF0E0B8)
    val WallStone = Color(0xFFC8C2B4)
    val Shadow = Color(0x33000000)
}

fun DrawScope.kRect(
    color: Color,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    outline: Boolean = true,
    stroke: Float = 3.5f
) {
    drawRect(color, Offset(x, y), Size(w, h))
    if (outline) drawRect(Kenney.Outline, Offset(x, y), Size(w, h), style = Stroke(stroke))
}

fun DrawScope.kRound(
    color: Color,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    r: Float = 8f,
    outline: Boolean = true,
    stroke: Float = 3.5f
) {
    drawRoundRect(color, Offset(x, y), Size(w, h), CornerRadius(r, r))
    if (outline) {
        drawRoundRect(Kenney.Outline, Offset(x, y), Size(w, h), CornerRadius(r, r), style = Stroke(stroke))
    }
}

fun DrawScope.kOval(
    color: Color,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    outline: Boolean = true,
    stroke: Float = 3f
) {
    drawOval(color, Offset(x, y), Size(w, h))
    if (outline) drawOval(Kenney.Outline, Offset(x, y), Size(w, h), style = Stroke(stroke))
}

fun DrawScope.kCircle(color: Color, r: Float, c: Offset, outline: Boolean = true, stroke: Float = 3f) {
    drawCircle(color, r, c)
    if (outline) drawCircle(Kenney.Outline, r, c, style = Stroke(stroke))
}

fun DrawScope.kPath(color: Color, path: Path, outline: Boolean = true, stroke: Float = 3.5f) {
    drawPath(path, color, style = Fill)
    if (outline) drawPath(path, Kenney.Outline, style = Stroke(stroke))
}
