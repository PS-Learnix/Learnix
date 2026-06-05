import 'dart:convert';
import 'dart:io' show Platform;
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

import '../parent_models.dart';
import 'parent_repository.dart';

class HttpParentRepository implements ParentRepository {
  // static String get _defaultUrl {
  //   if (kIsWeb) return 'http://localhost:8080';
  //   try {
  //     if (Platform.isAndroid) return 'http://10.0.2.2:8080';
  //   } catch (_) {}
  //   return 'http://localhost:8080';
  // }

  // final String baseUrl = _defaultUrl;
  final String baseUrl = 'https://learnix.yoshua-cloud.dedyn.io';
  
  String? token;
  int? parentId;
  int? studentId;
  List<dynamic> studentList = [];

  // Mocks for chat, since the backend has no database/endpoints for chats.
  final List<Conversation> _conversations = [
    Conversation(
      id: 1,
      participantName: 'Ana Gomez',
      role: 'Docente de Matematica',
      messages: [
        ChatMessage(
          text: 'Buenas tardes, Juan mejoro en la ultima actividad.',
          sentAt: DateTime(2026, 5, 28, 15, 20),
          isFromParent: false,
          isRead: true,
        ),
        ChatMessage(
          text: 'Gracias profesora. Revisaremos la tarea pendiente hoy.',
          sentAt: DateTime(2026, 5, 28, 15, 32),
          isFromParent: true,
          isRead: true,
        ),
      ],
    ),
    Conversation(
      id: 2,
      participantName: 'Coordinacion General',
      role: 'Coordinador',
      messages: [
        ChatMessage(
          text: 'Recordatorio: reunion de padres el viernes.',
          sentAt: DateTime(2026, 5, 29, 9, 10),
          isFromParent: false,
          isRead: false,
        ),
      ],
    ),
  ];

  Map<String, String> get _headers => {
        'Content-Type': 'application/json; charset=UTF-8',
        if (token != null) 'Authorization': 'Bearer $token',
      };

  @override
  Future<bool> login({required String email, required String password}) async {
    final uri = Uri.parse('$baseUrl/api/mobile/auth/login');
    try {
      debugPrint('HttpParentRepository: Logging in $email');
      final response = await http.post(
        uri,
        headers: {'Content-Type': 'application/json; charset=UTF-8'},
        body: jsonEncode({'email': email, 'password': password}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(utf8.decode(response.bodyBytes));
        token = data['token'] as String?;
        final parentData = data['parent'];
        if (parentData != null) {
          parentId = parentData['id'] as int?;
        }
        
        final studentsData = data['students'];
        if (studentsData is List) {
          studentList = studentsData;
          if (studentsData.isNotEmpty) {
            studentId = studentsData.first['id'] as int?;
          }
        }
        
        debugPrint('HttpParentRepository: Login successful. Parent ID: $parentId, Student ID: $studentId');
        return true;
      } else {
        debugPrint('HttpParentRepository: Login failed with code ${response.statusCode}');
        return false;
      }
    } catch (e) {
      debugPrint('HttpParentRepository: Login error: $e');
      rethrow;
    }
  }

  @override
  Future<ParentDashboardData> loadDashboard({required int parentId}) async {
    final actualParentId = this.parentId ?? parentId;
    final actualStudentId = this.studentId ?? 1;

    debugPrint('HttpParentRepository: Loading dashboard for Parent $actualParentId, Student $actualStudentId');

    // URLs for concurrent requests
    final dashboardUri = Uri.parse('$baseUrl/api/mobile/parents/$actualParentId/students/$actualStudentId/dashboard');
    final progressUri = Uri.parse('$baseUrl/api/mobile/students/$actualStudentId/progress');
    final attendanceUri = Uri.parse('$baseUrl/api/mobile/students/$actualStudentId/attendance');
    final remindersUri = Uri.parse('$baseUrl/api/mobile/parents/$actualParentId/students/$actualStudentId/reminders');
    final incidentsUri = Uri.parse('$baseUrl/api/mobile/parents/$actualParentId/students/$actualStudentId/incidents');
    final announcementsUri = Uri.parse('$baseUrl/api/mobile/parents/$actualParentId/announcements');

    // Execute requests concurrently
    final responses = await Future.wait([
      http.get(dashboardUri, headers: _headers),
      http.get(progressUri, headers: _headers),
      http.get(attendanceUri, headers: _headers),
      http.get(remindersUri, headers: _headers),
      http.get(incidentsUri, headers: _headers),
      http.get(announcementsUri, headers: _headers),
    ]);

    final dashboardRes = responses[0];
    final progressRes = responses[1];
    final attendanceRes = responses[2];
    final remindersRes = responses[3];
    final incidentsRes = responses[4];
    final announcementsRes = responses[5];

    // Check status codes
    if (dashboardRes.statusCode != 200) {
      throw Exception('Failed to load dashboard: Status ${dashboardRes.statusCode}');
    }
    if (progressRes.statusCode != 200) {
      throw Exception('Failed to load progress: Status ${progressRes.statusCode}');
    }
    if (attendanceRes.statusCode != 200) {
      throw Exception('Failed to load attendance: Status ${attendanceRes.statusCode}');
    }
    if (remindersRes.statusCode != 200) {
      throw Exception('Failed to load reminders: Status ${remindersRes.statusCode}');
    }
    if (incidentsRes.statusCode != 200) {
      throw Exception('Failed to load incidents: Status ${incidentsRes.statusCode}');
    }
    if (announcementsRes.statusCode != 200) {
      throw Exception('Failed to load announcements: Status ${announcementsRes.statusCode}');
    }

    // 1. Parse Dashboard Response
    final dashboardData = jsonDecode(utf8.decode(dashboardRes.bodyBytes));
    final studentJson = dashboardData['student'];
    if (studentJson == null) {
      throw Exception('No student data returned from dashboard');
    }

    final student = StudentSummary(
      id: studentJson['id'] as int? ?? actualStudentId,
      fullName: studentJson['fullName'] as String? ?? 'Estudiante',
      gradeSection: studentJson['gradeSection'] as String? ?? '',
      generalAverage: (studentJson['generalAverage'] as num?)?.toDouble() ?? 0.0,
      attendancePercentage: (studentJson['attendancePercentage'] as num?)?.toDouble() ?? 100.0,
      status: _parseAcademicStatus(studentJson['academicStatus'] as String?),
    );

    final averagesJson = dashboardData['courseAverages'] as List? ?? [];
    final courseAverages = averagesJson.map((item) {
      return CourseAverage(
        courseName: item['courseName'] as String? ?? '',
        average: (item['average'] as num?)?.toDouble() ?? 0.0,
      );
    }).toList();

    final activitiesJson = dashboardData['activities'] as List? ?? [];
    final activities = activitiesJson.map((item) {
      return StudentActivity(
        name: item['name'] as String? ?? '',
        courseName: item['courseName'] as String? ?? '',
        term: item['term'] as String? ?? '',
        date: _parseDate(item['date']),
        grade: item['grade']?.toString() ?? '',
        status: item['status'] as String? ?? '',
      );
    }).toList();

    final reportsJson = dashboardData['reports'] as List? ?? [];
    final reports = reportsJson.map((item) {
      final formatsList = item['formats'] as List? ?? [];
      return AcademicReport(
        title: item['title'] as String? ?? '',
        description: item['description'] as String? ?? '',
        formats: formatsList.map((f) => _parseReportFormat(f as String?)).toList(),
      );
    }).toList();

    // 2. Parse Progress Response (term progress)
    final progressData = jsonDecode(utf8.decode(progressRes.bodyBytes));
    final termProgressJson = progressData['termProgress'] as List? ?? [];
    final termProgress = termProgressJson.map((item) {
      return TermProgress(
        term: item['term'] as String? ?? '',
        average: (item['average'] as num?)?.toDouble() ?? 0.0,
      );
    }).toList();

    // 3. Parse Attendance Response
    final attendanceData = jsonDecode(utf8.decode(attendanceRes.bodyBytes));
    final statsJson = attendanceData['stats'];
    final daysJson = attendanceData['days'] as List? ?? [];

    int attended = 0;
    int absent = 0;
    int lateCount = 0;
    if (statsJson != null) {
      attended = (statsJson['attendedDays'] as num?)?.toInt() ?? 0;
      absent = (statsJson['absentDays'] as num?)?.toInt() ?? 0;
      lateCount = (statsJson['lateDays'] as num?)?.toInt() ?? 0;
    }

    final days = daysJson.map((item) {
      return AttendanceDay(
        date: _parseDate(item['date']),
        status: _parseAttendanceStatus(item['status'] as String?),
      );
    }).toList();

    final attendanceStats = AttendanceStats(
      attendedDays: attended,
      absentDays: absent,
      lateDays: lateCount,
      days: days,
    );

    // 4. Parse Alerts (Reminders + Incidents)
    final List<ParentAlert> alerts = [];

    // Reminders
    final remindersData = jsonDecode(utf8.decode(remindersRes.bodyBytes));
    final remindersItems = remindersData['items'] as List? ?? [];
    for (final item in remindersItems) {
      alerts.add(ParentAlert(
        title: item['title'] as String? ?? '',
        detail: item['detail'] as String? ?? '',
        date: _parseDate(item['date']),
        status: AlertStatus.newAlert,
        severity: _parseAlertSeverity(item['severity'] as String?),
        category: _parseAlertCategory(item['type'] as String?, item['title'] as String? ?? ''),
      ));
    }

    // Incidents
    final incidentsData = jsonDecode(utf8.decode(incidentsRes.bodyBytes));
    final incidentsItems = incidentsData['items'] as List? ?? [];
    for (final item in incidentsItems) {
      alerts.add(ParentAlert(
        title: item['title'] as String? ?? '',
        detail: item['detail'] as String? ?? '',
        date: _parseDate(item['date']),
        status: _parseAlertStatus(item['status'] as String?),
        severity: _parseAlertSeverity(item['severity'] as String?),
        category: AlertCategory.incident,
      ));
    }

    // 5. Parse Announcements
    final announcementsData = jsonDecode(utf8.decode(announcementsRes.bodyBytes));
    final announcementsItems = announcementsData['items'] as List? ?? [];
    final announcements = announcementsItems.map((item) {
      return Announcement(
        title: item['title'] as String? ?? '',
        sender: item['sender'] as String? ?? '',
        date: _parseDate(item['date']),
        priority: item['priority'] as String? ?? 'Normal',
      );
    }).toList();

    return ParentDashboardData(
      student: student,
      courseAverages: courseAverages,
      termProgress: termProgress,
      activities: activities,
      attendanceStats: attendanceStats,
      alerts: alerts,
      announcements: announcements,
      conversations: _conversations,
      reports: reports,
    );
  }

  @override
  Future<ChatMessage> sendMessage({
    required int conversationId,
    required String text,
    String? attachmentName,
  }) async {
    // Return a mock chat message response immediately
    await Future<void>.delayed(const Duration(milliseconds: 150));
    final msg = ChatMessage(
      text: text.trim(),
      sentAt: DateTime.now(),
      isFromParent: true,
      isRead: false,
      attachmentName: attachmentName,
    );
    
    // Find matching conversation and append message to simulate ongoing conversation
    final idx = _conversations.indexWhere((c) => c.id == conversationId);
    if (idx != -1) {
      final oldList = _conversations[idx].messages;
      _conversations[idx] = Conversation(
        id: _conversations[idx].id,
        participantName: _conversations[idx].participantName,
        role: _conversations[idx].role,
        messages: [...oldList, msg],
      );
    }

    return msg;
  }

  @override
  Future<void> updatePreferences({required int parentId, required bool darkMode}) async {
    final actualParentId = this.parentId ?? parentId;
    final uri = Uri.parse('$baseUrl/api/mobile/parents/$actualParentId/preferences');
    try {
      debugPrint('HttpParentRepository: Updating preferences to darkMode=$darkMode');
      final response = await http.patch(
        uri,
        headers: _headers,
        body: jsonEncode({'darkMode': darkMode}),
      );
      if (response.statusCode != 200) {
        debugPrint('HttpParentRepository: Preference update failed with status ${response.statusCode}');
      }
    } catch (e) {
      debugPrint('HttpParentRepository: Error updating preferences: $e');
    }
  }

  // Helper parsers
  AcademicStatus _parseAcademicStatus(String? status) {
    if (status == null) return AcademicStatus.good;
    switch (status.toLowerCase()) {
      case 'excellent':
      case 'excelente':
        return AcademicStatus.excellent;
      case 'atrisk':
      case 'at_risk':
      case 'riesgo':
      case 'en riesgo':
        return AcademicStatus.atRisk;
      case 'good':
      case 'bueno':
      default:
        return AcademicStatus.good;
    }
  }

  AttendanceStatus _parseAttendanceStatus(String? status) {
    if (status == null) return AttendanceStatus.attended;
    switch (status.toLowerCase()) {
      case 'absent':
      case 'falto':
      case 'inasistencia':
      case 'falta':
        return AttendanceStatus.absent;
      case 'late':
      case 'tardanza':
        return AttendanceStatus.late;
      case 'attended':
      case 'asistio':
      case 'asistencia':
      default:
        return AttendanceStatus.attended;
    }
  }

  AlertStatus _parseAlertStatus(String? status) {
    if (status == null) return AlertStatus.newAlert;
    switch (status.toLowerCase()) {
      case 'read':
      case 'leido':
        return AlertStatus.read;
      case 'archived':
      case 'archivado':
        return AlertStatus.archived;
      case 'new':
      case 'nuevo':
      default:
        return AlertStatus.newAlert;
    }
  }

  AlertSeverity _parseAlertSeverity(String? severity) {
    if (severity == null) return AlertSeverity.info;
    switch (severity.toLowerCase()) {
      case 'warning':
      case 'advertencia':
      case 'media':
        return AlertSeverity.warning;
      case 'critical':
      case 'critica':
      case 'alta':
        return AlertSeverity.critical;
      case 'info':
      case 'informacion':
      case 'normal':
      case 'baja':
      default:
        return AlertSeverity.info;
    }
  }

  AlertCategory _parseAlertCategory(String? category, String title) {
    if (category == null) {
      if (title.toLowerCase().contains('incidencia') || title.toLowerCase().contains('disciplin')) {
        return AlertCategory.incident;
      }
      if (title.toLowerCase().contains('citacion') || title.toLowerCase().contains('reunion')) {
        return AlertCategory.citation;
      }
      return AlertCategory.activityDue;
    }
    switch (category.toLowerCase()) {
      case 'citation':
      case 'citacion':
        return AlertCategory.citation;
      case 'incident':
      case 'incidencia':
        return AlertCategory.incident;
      case 'activitydue':
      case 'activity_due':
      case 'actividad':
      default:
        return AlertCategory.activityDue;
    }
  }

  ReportFormat _parseReportFormat(String? format) {
    if (format == null) return ReportFormat.pdf;
    switch (format.toLowerCase()) {
      case 'excel':
      case 'xlsx':
      case 'xls':
        return ReportFormat.excel;
      case 'pdf':
      default:
        return ReportFormat.pdf;
    }
  }

  DateTime _parseDate(dynamic dateValue) {
    if (dateValue == null) return DateTime.now();
    if (dateValue is String) {
      return DateTime.tryParse(dateValue) ?? DateTime.now();
    }
    return DateTime.now();
  }
}
