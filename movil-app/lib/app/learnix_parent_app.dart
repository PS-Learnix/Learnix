import 'package:flutter/material.dart';

import '../features/parent_dashboard/controllers/theme_controller.dart';
import '../features/parent_dashboard/models/repositories/mock_parent_repository.dart';
import '../features/parent_dashboard/views/login_screen.dart';
import 'theme.dart';

class LearnixParentApp extends StatefulWidget {
  const LearnixParentApp({super.key});

  @override
  State<LearnixParentApp> createState() => _LearnixParentAppState();
}

class _LearnixParentAppState extends State<LearnixParentApp> {
  final ThemeController _themeController = ThemeController();

  @override
  void initState() {
    super.initState();
    _themeController.addListener(_onThemeChanged);
  }

  @override
  void dispose() {
    _themeController
      ..removeListener(_onThemeChanged)
      ..dispose();
    super.dispose();
  }

  void _onThemeChanged() => setState(() {});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Learnix Padres',
      debugShowCheckedModeBanner: false,
      theme: buildLearnixTheme(),
      darkTheme: buildLearnixTheme(brightness: Brightness.dark),
      themeMode: _themeController.themeMode,
      home: LoginScreen(
        repository: MockParentRepository(),
        isDarkMode: _themeController.isDarkMode,
        onDarkModeChanged: _themeController.setDarkMode,
      ),
    );
  }
}
