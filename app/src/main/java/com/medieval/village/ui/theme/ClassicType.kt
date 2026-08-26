package com.medieval.village.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 고전 영문(비문/필사본) 느낌의 서체 세트.
 * 별도 폰트 에셋 없이 세리프 + 넓은 자간으로 각인된 느낌을 낸다.
 */
object ClassicType {
    val Family = FontFamily.Serif

    /** 버튼 각인용 — 넓은 자간의 세리프 대문자 */
    val Button = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 1.4.sp,
    )

    /** 표제 — 던전 이름 등 */
    val Title = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.2.sp,
    )

    /** 양피지 본문 — 작은 상태 설명 */
    val Scroll = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.2.sp,
    )

    /** 양피지 표제 — 상태창 머리글 */
    val ScrollHead = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.8.sp,
    )

    /** 칩·라벨 */
    val Label = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.6.sp,
    )
}

/** 층수를 로마 숫자로 — I..XL 범위면 충분하다. */
fun romanNumeral(value: Int): String {
    if (value <= 0) return "0"
    val table = listOf(
        1000 to "M", 900 to "CM", 500 to "D", 400 to "CD",
        100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
        10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I",
    )
    var left = value
    return buildString {
        table.forEach { (n, sym) ->
            while (left >= n) {
                append(sym)
                left -= n
            }
        }
    }
}
