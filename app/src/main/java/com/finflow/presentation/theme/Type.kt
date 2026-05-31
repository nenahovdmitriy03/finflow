package com.finflow.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.finflow.R

private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val nunito = GoogleFont("Nunito")
private val inter = GoogleFont("Inter")

val DisplayFontFamily = FontFamily(
    Font(googleFont = nunito, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = nunito, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = nunito, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = nunito, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
    Font(googleFont = nunito, fontProvider = googleFontsProvider, weight = FontWeight.ExtraBold),
)

val BodyFontFamily = FontFamily(
    Font(googleFont = inter, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = inter, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = inter, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = inter, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
)

val FinFlowTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp),
    displayMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
)
