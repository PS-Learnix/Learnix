import 'package:flutter/material.dart';

ThemeData buildLearnixTheme({Brightness brightness = Brightness.light}) {
  const seed = Color(0xFF1E40AF);
  final isDark = brightness == Brightness.dark;

  return ThemeData(
    useMaterial3: true,
    brightness: brightness,
    colorScheme: ColorScheme.fromSeed(seedColor: seed, brightness: brightness),
    scaffoldBackgroundColor:
        isDark ? const Color(0xFF0F172A) : const Color(0xFFF8F9FA),
    appBarTheme: AppBarTheme(
      centerTitle: true,
      backgroundColor:
          isDark ? const Color(0xFF0F172A) : const Color(0xFFF8F9FA),
      foregroundColor: isDark ? Colors.white : const Color(0xFF111827),
      elevation: 0,
    ),
    cardTheme: CardThemeData(
      elevation: 0,
      color: isDark ? const Color(0xFF111827) : Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
        side: BorderSide(
            color: isDark ? const Color(0xFF334155) : const Color(0xFFE5E7EB)),
      ),
    ),
    navigationBarTheme: NavigationBarThemeData(
      backgroundColor: isDark ? const Color(0xFF111827) : Colors.white,
      indicatorColor: seed.withValues(alpha: 0.12),
      labelTextStyle: WidgetStateProperty.all(
        const TextStyle(fontSize: 11, fontWeight: FontWeight.w600),
      ),
    ),
  );
}
