import 'package:flutter/material.dart';

import '../models/parent_models.dart';

class AnnouncementsController extends ChangeNotifier {
  String _selectedPriority = 'Todas';
  String _selectedSender = 'Todos';

  String get selectedPriority => _selectedPriority;
  String get selectedSender => _selectedSender;

  void setPriority(String value) {
    if (_selectedPriority == value) return;
    _selectedPriority = value;
    notifyListeners();
  }

  void setSender(String value) {
    if (_selectedSender == value) return;
    _selectedSender = value;
    notifyListeners();
  }

  List<String> priorities(List<Announcement> announcements) {
    return ['Todas', ...announcements.map((item) => item.priority).toSet()];
  }

  List<String> senders(List<Announcement> announcements) {
    return ['Todos', ...announcements.map((item) => item.sender).toSet()];
  }

  List<Announcement> filtered(List<Announcement> announcements) {
    return announcements.where((announcement) {
      final matchesPriority = _selectedPriority == 'Todas' ||
          announcement.priority == _selectedPriority;
      final matchesSender =
          _selectedSender == 'Todos' || announcement.sender == _selectedSender;
      return matchesPriority && matchesSender;
    }).toList();
  }
}
