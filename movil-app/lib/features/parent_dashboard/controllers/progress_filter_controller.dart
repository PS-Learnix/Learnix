import 'package:flutter/material.dart';

import '../models/parent_models.dart';

class ProgressFilterController extends ChangeNotifier {
  String _selectedTerm = 'Todos';
  String _selectedCourse = 'Todos';

  String get selectedTerm => _selectedTerm;
  String get selectedCourse => _selectedCourse;

  void setTerm(String value) {
    if (_selectedTerm == value) return;
    _selectedTerm = value;
    notifyListeners();
  }

  void setCourse(String value) {
    if (_selectedCourse == value) return;
    _selectedCourse = value;
    notifyListeners();
  }

  List<String> terms(ParentDashboardData data) {
    return ['Todos', ...data.termProgress.map((item) => item.term)];
  }

  List<String> courses(ParentDashboardData data) {
    return ['Todos', ...data.courseAverages.map((item) => item.courseName)];
  }

  List<StudentActivity> activities(ParentDashboardData data) {
    return data.activities.where((activity) {
      final matchesTerm =
          _selectedTerm == 'Todos' || activity.term == _selectedTerm;
      final matchesCourse =
          _selectedCourse == 'Todos' || activity.courseName == _selectedCourse;
      return matchesTerm && matchesCourse;
    }).toList();
  }

  List<TermProgress> visibleProgress(ParentDashboardData data) {
    if (_selectedTerm == 'Todos') return data.termProgress;
    return data.termProgress
        .where((item) => item.term == _selectedTerm)
        .toList();
  }
}
