package com.medieval.village.ui.place

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.medieval.village.model.InteriorRoom
import com.medieval.village.model.PlaceId

/** 장소별 탁자·침대·선반 등 실내 소품. */
fun DrawScope.drawInteriorFurniture(id: PlaceId) {
    val w = InteriorRoom.WORLD_W
    val h = InteriorRoom.WORLD_H
    when (id) {
        PlaceId.INN -> drawInnFurniture(w, h)
        PlaceId.HOME -> drawHomeFurniture(w, h)
        PlaceId.SHOP -> drawShopFurniture(w, h)
        PlaceId.WEAPON_SHOP -> drawWeaponShopFurniture(w, h)
        PlaceId.HOSPITAL -> drawHospitalFurniture(w, h)
        PlaceId.CHURCH -> drawChurchFurniture(w, h)
        PlaceId.MAGIC_SCHOOL -> drawMagicFurniture(w, h)
        PlaceId.ARENA -> drawArenaFurniture(w, h)
        PlaceId.BLACKSMITH -> drawBlacksmithFurniture(w, h)
        PlaceId.MERCENARY -> drawMercFurniture(w, h)
        else -> drawGenericCounter(w * 0.55f, h * 0.52f, w * 0.38f, h * 0.12f, Color(0xFF6A5040))
    }
}

private fun DrawScope.drawInnFurniture(w: Float, h: Float) {
    // 접수 카운터
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFF7A5535))
    // 식탁 2개
    drawRoundTable(260f, 420f)
    drawRoundTable(480f, 520f)
    // 침대 2개 (안쪽)
    drawBed(120f, 300f, Color(0xFF6B8FB0))
    drawBed(120f, 420f, Color(0xFF8B6B8F))
    // 맥주통
    drawBarrel(820f, 620f)
    drawBarrel(880f, 640f)
    // 벽 간판
    drawSign(520f, 220f, "INN")
}

private fun DrawScope.drawHomeFurniture(w: Float, h: Float) {
    drawBed(140f, 340f, Color(0xFF8A6A4A))
    drawRectTable(520f, 480f, 220f, 90f)
    drawChair(470f, 520f)
    drawChair(700f, 520f)
    drawShelf(780f, 250f, shelves = 3)
    drawFireplaceProp(120f, 220f)
}

private fun DrawScope.drawShopFurniture(w: Float, h: Float) {
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFF8A5A32))
    drawShelf(120f, 260f, shelves = 4)
    drawShelf(250f, 260f, shelves = 4)
    drawCrate(160f, 520f)
    drawCrate(240f, 560f)
    drawRectTable(380f, 500f, 160f, 70f)
    drawBasket(400f, 470f)
}

private fun DrawScope.drawWeaponShopFurniture(w: Float, h: Float) {
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFFA05030))
    drawWeaponRack(140f, 280f)
    drawWeaponRack(260f, 280f)
    drawAnvil(380f, 520f)
    drawCrate(200f, 560f)
    drawShield(900f, 300f)
}

private fun DrawScope.drawHospitalFurniture(w: Float, h: Float) {
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFF6A7A88))
    drawBed(140f, 360f, Color(0xFFE8E0D0))
    drawBed(140f, 500f, Color(0xFFE8E0D0))
    drawShelf(860f, 260f, shelves = 3, bottle = true)
    drawRectTable(420f, 540f, 140f, 60f)
}

private fun DrawScope.drawChurchFurniture(w: Float, h: Float) {
    // 제단
    drawRoundRect(Color(0xFFC9B27A), Offset(w * 0.58f, h * 0.42f), Size(w * 0.32f, h * 0.10f), CornerRadius(8f, 8f))
    drawRoundRect(Color(0xFF5A4030), Offset(w * 0.58f, h * 0.42f), Size(w * 0.32f, h * 0.10f), CornerRadius(8f, 8f), style = Stroke(3f))
    // 기도 벤치
    drawPew(160f, 420f)
    drawPew(160f, 500f)
    drawPew(160f, 580f)
    drawCandle(720f, 400f)
    drawCandle(860f, 400f)
}

private fun DrawScope.drawMagicFurniture(w: Float, h: Float) {
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFF4A3A6A))
    drawBookshelf(120f, 250f)
    drawBookshelf(250f, 250f)
    drawRectTable(360f, 480f, 200f, 80f)
    drawCrystal(420f, 450f)
    drawBookshelf(820f, 250f)
}

private fun DrawScope.drawArenaFurniture(w: Float, h: Float) {
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFF5A4030))
    drawWeaponRack(150f, 300f)
    drawPew(200f, 520f)
    drawPew(200f, 600f)
    drawTarget(400f, 400f)
}

private fun DrawScope.drawBlacksmithFurniture(w: Float, h: Float) {
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFF6A4030))
    drawForge(140f, 280f)
    drawAnvil(360f, 480f)
    drawWeaponRack(820f, 280f)
    drawBarrel(200f, 560f)
    drawCrate(280f, 600f)
}

private fun DrawScope.drawMercFurniture(w: Float, h: Float) {
    drawGenericCounter(w * 0.55f, h * 0.48f, w * 0.38f, h * 0.11f, Color(0xFF4A5038))
    drawRectTable(280f, 460f, 240f, 100f)
    drawChair(250f, 520f)
    drawChair(460f, 520f)
    drawSign(520f, 220f, "HIRE")
    drawWeaponRack(140f, 300f)
    drawMapBoard(820f, 280f)
}

// --- primitives ---

private fun DrawScope.drawGenericCounter(x: Float, y: Float, cw: Float, ch: Float, color: Color) {
    drawRoundRect(color, Offset(x, y), Size(cw, ch), CornerRadius(10f, 10f))
    drawRoundRect(Color(0xFF3A2818), Offset(x, y), Size(cw, ch), CornerRadius(10f, 10f), style = Stroke(3f))
    drawRoundRect(Color(0x33FFFFFF), Offset(x + 8f, y + 6f), Size(cw - 16f, 10f), CornerRadius(4f, 4f))
}

private fun DrawScope.drawRoundTable(cx: Float, cy: Float) {
    drawOval(Color(0xFF3C261A), Offset(cx - 90f, cy - 14f), Size(180f, 60f))
    drawOval(Color(0xFF89542F), Offset(cx - 90f, cy - 26f), Size(180f, 54f))
    drawRect(Color(0xFF4A2D1D), Offset(cx - 10f, cy + 18f), Size(20f, 50f))
    drawCircle(Color(0xFFD9B15D), 8f, Offset(cx - 30f, cy - 4f))
    drawRect(Color(0xFFD3B887), Offset(cx + 16f, cy - 14f), Size(28f, 18f))
}

private fun DrawScope.drawRectTable(x: Float, y: Float, tw: Float, th: Float) {
    drawRoundRect(Color(0xFF6B4428), Offset(x, y), Size(tw, th), CornerRadius(6f, 6f))
    drawRoundRect(Color(0xFF3A2414), Offset(x, y), Size(tw, th), CornerRadius(6f, 6f), style = Stroke(2.5f))
    drawRect(Color(0xFF4A2D1D), Offset(x + 12f, y + th), Size(14f, 28f))
    drawRect(Color(0xFF4A2D1D), Offset(x + tw - 26f, y + th), Size(14f, 28f))
}

private fun DrawScope.drawChair(x: Float, y: Float) {
    drawRoundRect(Color(0xFF5A3A22), Offset(x, y), Size(36f, 28f), CornerRadius(4f, 4f))
    drawRect(Color(0xFF4A2D1D), Offset(x + 4f, y + 28f), Size(8f, 22f))
    drawRect(Color(0xFF4A2D1D), Offset(x + 24f, y + 28f), Size(8f, 22f))
    drawRect(Color(0xFF6B4428), Offset(x + 2f, y - 28f), Size(32f, 30f))
}

private fun DrawScope.drawBed(x: Float, y: Float, blanket: Color) {
    drawRoundRect(Color(0xFF5A3A22), Offset(x, y), Size(200f, 90f), CornerRadius(8f, 8f))
    drawRoundRect(blanket, Offset(x + 50f, y + 10f), Size(140f, 70f), CornerRadius(6f, 6f))
    drawRoundRect(Color(0xFFE8D9B8), Offset(x + 8f, y + 18f), Size(44f, 54f), CornerRadius(8f, 8f))
    drawRoundRect(Color(0xFF3A2414), Offset(x, y), Size(200f, 90f), CornerRadius(8f, 8f), style = Stroke(2.5f))
}

private fun DrawScope.drawShelf(x: Float, y: Float, shelves: Int, bottle: Boolean = false) {
    val sh = 28f + shelves * 36f
    drawRoundRect(Color(0xFF6B4428), Offset(x, y), Size(100f, sh), CornerRadius(4f, 4f))
    for (i in 0 until shelves) {
        val sy = y + 20f + i * 36f
        drawLine(Color(0xFF3A2414), Offset(x + 6f, sy), Offset(x + 94f, sy), 3f)
        if (bottle) {
            drawRect(Color(0xFF56806B), Offset(x + 16f, sy - 22f), Size(12f, 20f))
            drawRect(Color(0xFF9B6A43), Offset(x + 40f, sy - 22f), Size(12f, 20f))
            drawRect(Color(0xFF5B6F92), Offset(x + 64f, sy - 22f), Size(12f, 20f))
        } else {
            drawRect(Color(0xFFC9A876), Offset(x + 14f, sy - 18f), Size(22f, 14f))
            drawRect(Color(0xFF8FCF7A), Offset(x + 42f, sy - 18f), Size(18f, 14f))
            drawRect(Color(0xFFD9A441), Offset(x + 68f, sy - 16f), Size(16f, 12f))
        }
    }
}

private fun DrawScope.drawBookshelf(x: Float, y: Float) {
    drawRoundRect(Color(0xFF4A3220), Offset(x, y), Size(110f, 160f), CornerRadius(4f, 4f))
    val colors = listOf(Color(0xFF8B3A3A), Color(0xFF3A5A8B), Color(0xFF3A7A4A), Color(0xFF8B6A2A), Color(0xFF6A3A7A))
    for (row in 0..3) {
        val sy = y + 28f + row * 34f
        drawLine(Color(0xFF2A1A10), Offset(x + 6f, sy), Offset(x + 104f, sy), 3f)
        for (b in 0..4) {
            drawRect(colors[(row + b) % colors.size], Offset(x + 10f + b * 18f, sy - 26f), Size(14f, 24f))
        }
    }
}

private fun DrawScope.drawCrate(x: Float, y: Float) {
    drawRoundRect(Color(0xFF8A6A3A), Offset(x, y), Size(56f, 48f), CornerRadius(4f, 4f))
    drawLine(Color(0xFF4A3820), Offset(x + 4f, y + 24f), Offset(x + 52f, y + 24f), 2f)
    drawLine(Color(0xFF4A3820), Offset(x + 28f, y + 4f), Offset(x + 28f, y + 44f), 2f)
}

private fun DrawScope.drawBarrel(x: Float, y: Float) {
    drawOval(Color(0xFF6B4428), Offset(x - 28f, y - 40f), Size(56f, 70f))
    drawOval(Color(0xFF8A5A32), Offset(x - 22f, y - 48f), Size(44f, 20f))
    drawLine(Color(0xFF3A2414), Offset(x - 24f, y - 10f), Offset(x + 24f, y - 10f), 3f)
    drawLine(Color(0xFF3A2414), Offset(x - 24f, y + 10f), Offset(x + 24f, y + 10f), 3f)
}

private fun DrawScope.drawBasket(x: Float, y: Float) {
    drawOval(Color(0xFFC9A876), Offset(x, y), Size(48f, 28f))
    drawArc(Color(0xFF8A6A3A), 200f, 140f, false, Offset(x + 4f, y - 8f), Size(40f, 30f), style = Stroke(3f))
}

private fun DrawScope.drawWeaponRack(x: Float, y: Float) {
    drawRect(Color(0xFF5A3A22), Offset(x, y), Size(70f, 120f))
    drawLine(Color(0xFFAAB0B8), Offset(x + 18f, y + 10f), Offset(x + 18f, y + 100f), 4f)
    drawLine(Color(0xFFC9A876), Offset(x + 35f, y + 16f), Offset(x + 35f, y + 96f), 4f)
    drawLine(Color(0xFF8A9098), Offset(x + 52f, y + 12f), Offset(x + 52f, y + 100f), 4f)
    drawCircle(Color(0xFFD9A441), 6f, Offset(x + 18f, y + 8f))
}

private fun DrawScope.drawAnvil(x: Float, y: Float) {
    drawRect(Color(0xFF4A4A52), Offset(x, y + 20f), Size(70f, 36f))
    drawRect(Color(0xFF6A6A72), Offset(x - 10f, y), Size(90f, 28f))
    drawRect(Color(0xFF3A3A42), Offset(x + 20f, y + 56f), Size(30f, 24f))
}

private fun DrawScope.drawForge(x: Float, y: Float) {
    drawRoundRect(Color(0xFF5A5048), Offset(x, y), Size(140f, 120f), CornerRadius(8f, 8f))
    drawRoundRect(Color(0xFF2B1A13), Offset(x + 30f, y + 40f), Size(80f, 70f), CornerRadius(6f, 6f))
    val flame = Path().apply {
        moveTo(x + 50f, y + 100f)
        quadraticBezierTo(x + 60f, y + 50f, x + 70f, y + 95f)
        quadraticBezierTo(x + 85f, y + 45f, x + 95f, y + 100f)
        close()
    }
    drawPath(flame, Color(0xFFE8582C))
    drawCircle(Color(0x55FFB23E), 40f, Offset(x + 70f, y + 70f))
}

private fun DrawScope.drawShield(x: Float, y: Float) {
    drawOval(Color(0xFF6A7A8A), Offset(x, y), Size(50f, 64f))
    drawOval(Color(0xFFD9A441), Offset(x + 12f, y + 16f), Size(26f, 32f))
}

private fun DrawScope.drawPew(x: Float, y: Float) {
    drawRoundRect(Color(0xFF6B4428), Offset(x, y), Size(280f, 28f), CornerRadius(4f, 4f))
    drawRect(Color(0xFF5A3A22), Offset(x, y - 36f), Size(280f, 36f))
    drawRect(Color(0xFF4A2D1D), Offset(x + 10f, y + 28f), Size(12f, 20f))
    drawRect(Color(0xFF4A2D1D), Offset(x + 258f, y + 28f), Size(12f, 20f))
}

private fun DrawScope.drawCandle(x: Float, y: Float) {
    drawRect(Color(0xFFE8D9B8), Offset(x, y), Size(10f, 28f))
    drawCircle(Color(0xFFFFC857), 7f, Offset(x + 5f, y - 4f))
    drawCircle(Color(0x44FFB23E), 22f, Offset(x + 5f, y))
}

private fun DrawScope.drawCrystal(x: Float, y: Float) {
    val p = Path().apply {
        moveTo(x + 16f, y)
        lineTo(x + 32f, y + 28f)
        lineTo(x + 16f, y + 48f)
        lineTo(x, y + 28f)
        close()
    }
    drawPath(p, Color(0xAA6A90D0))
    drawPath(p, Color(0xFF3A5080), style = Stroke(2f))
}

private fun DrawScope.drawTarget(x: Float, y: Float) {
    drawCircle(Color(0xFFD9C8A4), 36f, Offset(x, y))
    drawCircle(Color(0xFFC0392B), 22f, Offset(x, y))
    drawCircle(Color(0xFFF4D35E), 10f, Offset(x, y))
}

private fun DrawScope.drawFireplaceProp(x: Float, y: Float) {
    drawRect(Color(0xFF786B60), Offset(x, y), Size(140f, 120f))
    drawRect(Color(0xFF2B1A13), Offset(x + 30f, y + 35f), Size(80f, 75f))
    drawCircle(Color(0xFFE8582C), 18f, Offset(x + 70f, y + 90f))
}

private fun DrawScope.drawSign(x: Float, y: Float, text: String) {
    drawRoundRect(Color(0xFF3E2519), Offset(x, y), Size(120f, 44f), CornerRadius(6f, 6f))
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#E2B866")
        textSize = 22f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(text, x + 60f, y + 30f, paint)
}

private fun DrawScope.drawMapBoard(x: Float, y: Float) {
    drawRoundRect(Color(0xFF8A5A32), Offset(x, y), Size(120f, 90f), CornerRadius(4f, 4f))
    drawRoundRect(Color(0xFFD9C8A4), Offset(x + 10f, y + 10f), Size(100f, 70f), CornerRadius(3f, 3f))
    drawLine(Color(0xFF5A4231), Offset(x + 20f, y + 30f), Offset(x + 90f, y + 50f), 2f)
    drawLine(Color(0xFF5A4231), Offset(x + 30f, y + 60f), Offset(x + 80f, y + 25f), 2f)
    drawCircle(Color(0xFFC0392B), 4f, Offset(x + 55f, y + 40f))
}
