package com.example.nb_frontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.R

val Jua = FontFamily(
    Font(R.font.jua_regular, FontWeight.Normal)
)

val Roboto = FontFamily(
    Font(R.font.roboto_semibold, FontWeight.SemiBold),
    Font(R.font.roboto_bold, FontWeight.Bold)
)

val Typography = Typography(
    // Jua
    displayLarge = TextStyle(fontFamily = Jua, fontSize = 40.sp),
    headlineLarge = TextStyle(fontFamily = Jua, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = Jua, fontSize = 20.sp),

    // Roboto Bold
    titleMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp),

    // Roboto SemiBold
    bodyMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
)