import '../parent_models.dart';
import 'parent_repository.dart';

class MockParentRepository implements ParentRepository {
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

  @override
  Future<ParentDashboardData> loadDashboard({required int parentId}) async {
    await Future<void>.delayed(const Duration(milliseconds: 350));

    return ParentDashboardData(
      student: const StudentSummary(
        id: 1,
        fullName: 'Juan Perez',
        gradeSection: '3ro A',
        generalAverage: 17,
        attendancePercentage: 92,
        status: AcademicStatus.good,
      ),
      courseAverages: const [
        CourseAverage(courseName: 'Matematica', average: 18),
        CourseAverage(courseName: 'Comunicacion', average: 16),
        CourseAverage(courseName: 'Ciencia', average: 17),
      ],
      termProgress: const [
        TermProgress(term: 'B1', average: 15),
        TermProgress(term: 'B2', average: 16),
        TermProgress(term: 'B3', average: 17),
        TermProgress(term: 'B4', average: 18),
      ],
      activities: [
        StudentActivity(
          name: 'Examen de Suma y Resta',
          courseName: 'Matematica',
          term: 'B1',
          date: DateTime(2026, 5, 10),
          grade: '18',
          status: 'Calificada',
        ),
        StudentActivity(
          name: 'Redaccion de Cuento',
          courseName: 'Comunicacion',
          term: 'B2',
          date: DateTime(2026, 5, 12),
          grade: '16',
          status: 'Calificada',
        ),
        StudentActivity(
          name: 'Proyecto de Plantas',
          courseName: 'Ciencia',
          term: 'B3',
          date: DateTime(2026, 5, 15),
          grade: '17',
          status: 'Entregada',
        ),
        StudentActivity(
          name: 'Practica de fracciones',
          courseName: 'Matematica',
          term: 'B2',
          date: DateTime(2026, 5, 22),
          grade: '19',
          status: 'Calificada',
        ),
        StudentActivity(
          name: 'Informe de lectura',
          courseName: 'Comunicacion',
          term: 'B4',
          date: DateTime(2026, 6, 2),
          grade: '15',
          status: 'Pendiente',
        ),
        StudentActivity(
          name: 'Practica de laboratorio',
          courseName: 'Ciencia',
          term: 'B4',
          date: DateTime(2026, 6, 3),
          grade: 'Sin calificar',
          status: 'Pendiente',
        ),
      ],
      attendanceStats: AttendanceStats(
        attendedDays: 22,
        absentDays: 2,
        lateDays: 1,
        days: List.generate(25, (index) {
          final day = DateTime(2026, 5, index + 1);
          if (index == 8 || index == 18) {
            return AttendanceDay(date: day, status: AttendanceStatus.absent);
          }
          if (index == 13) {
            return AttendanceDay(date: day, status: AttendanceStatus.late);
          }
          return AttendanceDay(date: day, status: AttendanceStatus.attended);
        }),
      ),
      alerts: [
        ParentAlert(
          title: 'Actividad pendiente',
          detail: 'Debe completar practica de lectura.',
          date: DateTime(2026, 6, 2),
          status: AlertStatus.newAlert,
          severity: AlertSeverity.warning,
          category: AlertCategory.activityDue,
        ),
        ParentAlert(
          title: 'Citacion proxima',
          detail: 'Reunion con tutor el 08/06/2026.',
          date: DateTime(2026, 6, 8),
          status: AlertStatus.read,
          severity: AlertSeverity.info,
          category: AlertCategory.citation,
        ),
        ParentAlert(
          title: 'Incidencia disciplinaria',
          detail: 'Observacion registrada en recreo.',
          date: DateTime(2026, 5, 24),
          status: AlertStatus.archived,
          severity: AlertSeverity.critical,
          category: AlertCategory.incident,
        ),
      ],
      announcements: [
        Announcement(
          title: 'Reunion de padres',
          sender: 'Direccion',
          date: DateTime(2026, 5, 30),
          priority: 'Alta',
        ),
        Announcement(
          title: 'Suspension de clases',
          sender: 'Coordinacion',
          date: DateTime(2026, 5, 26),
          priority: 'Media',
        ),
        Announcement(
          title: 'Entrega de libretas',
          sender: 'Secretaria Academica',
          date: DateTime(2026, 5, 20),
          priority: 'Normal',
        ),
      ],
      conversations: _conversations,
      reports: const [
        AcademicReport(
          title: 'Rendimiento academico',
          description: 'Promedios y progreso por curso.',
          formats: [ReportFormat.pdf, ReportFormat.excel],
        ),
        AcademicReport(
          title: 'Historial de notas',
          description: 'Detalle de actividades calificadas.',
          formats: [ReportFormat.pdf, ReportFormat.excel],
        ),
        AcademicReport(
          title: 'Asistencia',
          description: 'Asistencias, faltas y tardanzas.',
          formats: [ReportFormat.pdf, ReportFormat.excel],
        ),
        AcademicReport(
          title: 'Observaciones del docente',
          description: 'Comentarios academicos y conductuales.',
          formats: [ReportFormat.pdf],
        ),
      ],
    );
  }

  @override
  Future<bool> login({required String email, required String password}) async {
    await Future<void>.delayed(const Duration(milliseconds: 300));
    return true;
  }

  @override
  Future<ChatMessage> sendMessage({
    required int conversationId,
    required String text,
    String? attachmentName,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 150));
    return ChatMessage(
      text: text.trim(),
      sentAt: DateTime.now(),
      isFromParent: true,
      isRead: false,
      attachmentName: attachmentName,
    );
  }

  @override
  Future<void> updatePreferences({required int parentId, required bool darkMode}) async {
    await Future<void>.delayed(const Duration(milliseconds: 100));
  }
}
