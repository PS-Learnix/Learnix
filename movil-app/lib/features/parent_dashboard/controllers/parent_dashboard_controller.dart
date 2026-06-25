import 'package:flutter/material.dart';

import '../models/parent_models.dart';
import '../models/repositories/parent_repository.dart';

class ReminderItem {
  const ReminderItem({
    required this.title,
    required this.detail,
    required this.icon,
    required this.color,
    required this.category,
  });

  final String title;
  final String detail;
  final IconData icon;
  final Color color;
  final AlertCategory category;
}

class ParentDashboardController extends ChangeNotifier {
  ParentDashboardController({required this.repository}) {
    load();
  }

  final ParentRepository repository;
  late Future<ParentDashboardData> dashboardFuture;

  void load() {
    dashboardFuture = repository.loadDashboard(parentId: 1);
  }

  Future<void> refresh() async {
    load();
    notifyListeners();
    await dashboardFuture;
  }

  int _selectedIndex = 0;
  bool _isProfileOpen = false;

  int get selectedIndex => _selectedIndex;
  bool get isProfileOpen => _isProfileOpen;

  void selectTab(int index) {
    _selectedIndex = index;
    _isProfileOpen = false;
    notifyListeners();
  }

  void showAttendanceTab() {
    selectTab(0);
  }

  void toggleProfile() {
    _isProfileOpen = !_isProfileOpen;
    notifyListeners();
  }

  void closeProfile() {
    if (!_isProfileOpen) return;
    _isProfileOpen = false;
    notifyListeners();
  }

  List<ReminderItem> reminders(ParentDashboardData data) {
    final today = DateTime.now();
    final todayOnly = DateTime(today.year, today.month, today.day);
    final activityReminders = data.activities.where((activity) {
      final activityDate = DateTime(
        activity.date.year,
        activity.date.month,
        activity.date.day,
      );
      final daysLeft = activityDate.difference(todayOnly).inDays;
      return activity.status.toLowerCase() == 'pendiente' &&
          daysLeft >= 1 &&
          daysLeft <= 4;
    }).map(
      (activity) => ReminderItem(
        title: activity.name,
        detail:
            '${activity.courseName} vence el ${_date(activity.date)} (${activity.term}).',
        icon: Icons.assignment_late_outlined,
        color: const Color(0xFFFFC107),
        category: AlertCategory.activityDue,
      ),
    );

    final citationReminders = data.alerts.where((alert) {
      if (alert.category != AlertCategory.citation) return false;
      final alertDate =
          DateTime(alert.date.year, alert.date.month, alert.date.day);
      final daysLeft = alertDate.difference(todayOnly).inDays;
      return daysLeft >= 7 && daysLeft <= 11;
    }).map(
      (alert) => ReminderItem(
        title: alert.title,
        detail: '${alert.detail} Fecha: ${_date(alert.date)}.',
        icon: Icons.event_note_outlined,
        color: const Color(0xFF1E40AF),
        category: AlertCategory.citation,
      ),
    );

    return [...activityReminders, ...citationReminders];
  }

  String _date(DateTime date) {
    return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
  }
}
