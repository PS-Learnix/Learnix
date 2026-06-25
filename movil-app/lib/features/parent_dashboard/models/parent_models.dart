enum AcademicStatus { excellent, good, atRisk }

enum AlertStatus { newAlert, read, archived }

enum AlertSeverity { info, warning, critical }

enum AlertCategory { activityDue, citation, incident }

enum AttendanceStatus { attended, absent, late }

enum ReportFormat { pdf, excel }

enum CitationStatus { pending, accepted, rejected, cancelled }

enum CitationMode { virtual, inPerson }

class StudentSummary {
  const StudentSummary({
    required this.id,
    required this.fullName,
    required this.gradeSection,
    required this.generalAverage,
    required this.attendancePercentage,
    required this.status,
  });

  final int id;
  final String fullName;
  final String gradeSection;
  final double generalAverage;
  final double attendancePercentage;
  final AcademicStatus status;
}

class CourseAverage {
  const CourseAverage({required this.courseName, required this.average});

  final String courseName;
  final double average;
}

class TermProgress {
  const TermProgress({required this.term, required this.average});

  final String term;
  final double average;
}

class StudentActivity {
  const StudentActivity({
    required this.name,
    required this.courseName,
    required this.term,
    required this.date,
    required this.grade,
    required this.status,
  });

  final String name;
  final String courseName;
  final String term;
  final DateTime date;
  final String grade;
  final String status;
}

class AttendanceStats {
  const AttendanceStats({
    required this.attendedDays,
    required this.absentDays,
    required this.lateDays,
    required this.days,
  });

  final int attendedDays;
  final int absentDays;
  final int lateDays;
  final List<AttendanceDay> days;
}

class AttendanceDay {
  const AttendanceDay({required this.date, required this.status});

  final DateTime date;
  final AttendanceStatus status;
}

class ParentAlert {
  const ParentAlert({
    required this.title,
    required this.detail,
    required this.date,
    required this.status,
    required this.severity,
    required this.category,
  });

  final String title;
  final String detail;
  final DateTime date;
  final AlertStatus status;
  final AlertSeverity severity;
  final AlertCategory category;
}

class Announcement {
  const Announcement({
    required this.title,
    required this.sender,
    required this.date,
    required this.priority,
  });

  final String title;
  final String sender;
  final DateTime date;
  final String priority;
}

class Conversation {
  const Conversation({
    required this.id,
    required this.participantName,
    required this.role,
    required this.messages,
  });

  final int id;
  final String participantName;
  final String role;
  final List<ChatMessage> messages;
}

class ChatMessage {
  const ChatMessage({
    required this.text,
    required this.sentAt,
    required this.isFromParent,
    required this.isRead,
    this.attachmentName,
  });

  final String text;
  final DateTime sentAt;
  final bool isFromParent;
  final bool isRead;
  final String? attachmentName;
}

class AcademicReport {
  const AcademicReport({
    required this.title,
    required this.description,
    required this.formats,
  });

  final String title;
  final String description;
  final List<ReportFormat> formats;
}

class Citation {
  const Citation({
    required this.id,
    required this.title,
    required this.detail,
    required this.teacherName,
    required this.scheduledAt,
    required this.status,
    required this.mode,
    this.meetingUrl,
  });

  final int id;
  final String title;
  final String detail;
  final String teacherName;
  final DateTime scheduledAt;
  final CitationStatus status;
  final CitationMode mode;
  final String? meetingUrl;
}

class CitationMessage {
  const CitationMessage({
    required this.id,
    required this.citationId,
    required this.senderName,
    required this.senderRole,
    required this.body,
    required this.sentAt,
    required this.isFromParent,
    required this.isRead,
  });

  final int id;
  final int citationId;
  final String senderName;
  final String senderRole;
  final String body;
  final DateTime sentAt;
  final bool isFromParent;
  final bool isRead;
}

class ParentDashboardData {
  const ParentDashboardData({
    required this.student,
    required this.courseAverages,
    required this.termProgress,
    required this.activities,
    required this.attendanceStats,
    required this.alerts,
    required this.announcements,
    required this.conversations,
    required this.reports,
  });

  final StudentSummary student;
  final List<CourseAverage> courseAverages;
  final List<TermProgress> termProgress;
  final List<StudentActivity> activities;
  final AttendanceStats attendanceStats;
  final List<ParentAlert> alerts;
  final List<Announcement> announcements;
  final List<Conversation> conversations;
  final List<AcademicReport> reports;
}
