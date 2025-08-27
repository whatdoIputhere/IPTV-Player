package com.whatdoiputhere.iptvplayer.ui.theme

import androidx.compose.ui.graphics.Color

fun colorFromRGBA(rgba: Long): Color {
    val value = rgba and 0xFFFFFFFFL
    return if (value <= 0xFFFFFFL) {
        val red = ((value shr 16) and 0xFF).toInt()
        val green = ((value shr 8) and 0xFF).toInt()
        val blue = (value and 0xFF).toInt()
        val alpha = 0xFF
        Color(red = red, green = green, blue = blue, alpha = alpha)
    } else {
        val red = ((value shr 24) and 0xFF).toInt()
        val green = ((value shr 16) and 0xFF).toInt()
        val blue = ((value shr 8) and 0xFF).toInt()
        val alpha = (value and 0xFF).toInt()
        Color(red = red, green = green, blue = blue, alpha = alpha)
    }
}

val PrimaryLight = colorFromRGBA(0x1976D2FF)
val OnPrimaryLight = colorFromRGBA(0xFFFFFFFF)
val PrimaryContainerLight = colorFromRGBA(0xBBDEFBFF)
val OnPrimaryContainerLight = colorFromRGBA(0x0D47A1FF)
val SecondaryLight = colorFromRGBA(0x424242FF)
val OnSecondaryLight = colorFromRGBA(0xFFFFFFFF)
val SecondaryContainerLight = colorFromRGBA(0xE0E0E0FF)
val OnSecondaryContainerLight = colorFromRGBA(0x212121FF)
val TertiaryLight = colorFromRGBA(0x1976D2FF)
val OnTertiaryLight = colorFromRGBA(0xFFFFFFFF)
val TertiaryContainerLight = colorFromRGBA(0xBBDEFBFF)
val OnTertiaryContainerLight = colorFromRGBA(0x0D47A1FF)
val ErrorLight = colorFromRGBA(0xD32F2FFF)
val OnErrorLight = colorFromRGBA(0xFFFFFFFF)
val ErrorContainerLight = colorFromRGBA(0xFFCDD2FF)
val OnErrorContainerLight = colorFromRGBA(0xB71C1CFF)
val BackgroundLight = colorFromRGBA(0xFFFFFFFF)
val OnBackgroundLight = colorFromRGBA(0x212121FF)
val SurfaceLight = colorFromRGBA(0xFFFFFFFF)
val OnSurfaceLight = colorFromRGBA(0x212121FF)
val SurfaceVariantLight = colorFromRGBA(0xF5F5F5FF)
val OnSurfaceVariantLight = colorFromRGBA(0x424242FF)
val OutlineLight = colorFromRGBA(0xBDBDBDFF)

val PrimaryDark = colorFromRGBA(0xBBDEFBFF)
val OnPrimaryDark = colorFromRGBA(0x0D47A1FF)
val PrimaryContainerDark = colorFromRGBA(0x1976D2FF)
val OnPrimaryContainerDark = colorFromRGBA(0xBBDEFBFF)
val SecondaryDark = colorFromRGBA(0xE0E0E0FF)
val OnSecondaryDark = colorFromRGBA(0x212121FF)
val SecondaryContainerDark = colorFromRGBA(0x424242FF)
val OnSecondaryContainerDark = colorFromRGBA(0xE0E0E0FF)
val TertiaryDark = colorFromRGBA(0xBBDEFBFF)
val OnTertiaryDark = colorFromRGBA(0x0D47A1FF)
val TertiaryContainerDark = colorFromRGBA(0x1976D2FF)
val OnTertiaryContainerDark = colorFromRGBA(0xBBDEFBFF)
val ErrorDark = colorFromRGBA(0xEF5350FF)
val OnErrorDark = colorFromRGBA(0xB71C1CFF)
val ErrorContainerDark = colorFromRGBA(0xD32F2FFF)
val OnErrorContainerDark = colorFromRGBA(0xFFCDD2FF)
val BackgroundDark = colorFromRGBA(0x121212FF)
val OnBackgroundDark = colorFromRGBA(0xFFFFFFFF)
val SurfaceDark = colorFromRGBA(0x1E1E1EFF)
val OnSurfaceDark = colorFromRGBA(0xFFFFFFFF)
val SurfaceVariantDark = colorFromRGBA(0x2C2C2CFF)
val OnSurfaceVariantDark = colorFromRGBA(0xE0E0E0FF)
val OutlineDark = colorFromRGBA(0x555555FF)
