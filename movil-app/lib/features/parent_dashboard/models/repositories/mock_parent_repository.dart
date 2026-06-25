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

  final List<Citation> _citations = [
    Citation(
      id: 101,
      title: 'Citacion virtual con tutoria',
      detail: 'Revision de avance academico y acuerdos de apoyo en casa.',
      teacherName: 'Ana Gomez',
      scheduledAt: DateTime(2026, 6, 25, 17, 0),
      status: CitationStatus.pending,
      mode: CitationMode.virtual,
      meetingUrl: 'https://meet.learnix.edu/cita-101',
    ),
    Citation(
      id: 102,
      title: 'Seguimiento de asistencia',
      detail: 'Coordinacion solicita confirmar recepcion de la citacion.',
      teacherName: 'Coordinacion General',
      scheduledAt: DateTime(2026, 6, 28, 16, 30),
      status: CitationStatus.accepted,
      mode: CitationMode.virtual,
      meetingUrl: 'https://meet.learnix.edu/cita-102',
    ),
  ];

  final Map<int, List<CitationMessage>> _citationMessages = {
    101: [
      CitationMessage(
        id: 1,
        citationId: 101,
        senderName: 'Ana Gomez',
        senderRole: 'Docente',
        body: 'Buenas tardes, solicito una reunion para revisar el avance.',
        sentAt: DateTime(2026, 6, 17, 10, 30),
        isFromParent: false,
        isRead: true,
      ),
    ],
    102: [
      CitationMessage(
        id: 2,
        citationId: 102,
        senderName: 'Coordinacion General',
        senderRole: 'Coordinador',
        body: 'Por favor confirme su asistencia a la citacion virtual.',
        sentAt: DateTime(2026, 6, 17, 11, 0),
        isFromParent: false,
        isRead: false,
      ),
    ],
  };

  @override
  Future<ParentDashboardData> loadDashboard({required int parentId}) async {
    await Future<void>.delayed(const Duration(milliseconds: 350));

    return ParentDashboardData(
      student: const StudentSummary(
        id: 1,
        fullName: 'Juan Perez',
        gradeSection: 'A',
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
  Future<List<Citation>> loadCitations({
    required int parentId,
    required int studentId,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 250));
    return List.unmodifiable(_citations);
  }

  @override
  Future<Citation> respondToCitation({
    required int citationId,
    required CitationStatus status,
    String? reason,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 180));
    final index = _citations.indexWhere((item) => item.id == citationId);
    if (index == -1) throw StateError('Citacion no encontrada');
    final current = _citations[index];
    final updated = Citation(
      id: current.id,
      title: current.title,
      detail: reason == null || reason.trim().isEmpty
          ? current.detail
          : '${current.detail}\nMotivo: ${reason.trim()}',
      teacherName: current.teacherName,
      scheduledAt: current.scheduledAt,
      status: status,
      mode: current.mode,
      meetingUrl: current.meetingUrl,
    );
    _citations[index] = updated;
    return updated;
  }

  @override
  Future<Citation> confirmCitation({required int citationId}) {
    return respondToCitation(
      citationId: citationId,
      status: CitationStatus.confirmed,
    );
  }

  @override
  Future<List<CitationMessage>> loadCitationMessages({
    required int citationId,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 180));
    return List.unmodifiable(_citationMessages[citationId] ?? []);
  }

  @override
  Future<CitationMessage> sendCitationMessage({
    required int citationId,
    required String body,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 160));
    final message = CitationMessage(
      id: DateTime.now().millisecondsSinceEpoch,
      citationId: citationId,
      senderName: 'Padre de familia',
      senderRole: 'Padre',
      body: body.trim(),
      sentAt: DateTime.now(),
      isFromParent: true,
      isRead: false,
    );
    _citationMessages.putIfAbsent(citationId, () => []).add(message);
    return message;
  }

  @override
  Future<void> updatePreferences(
      {required int parentId, required bool darkMode}) async {
    await Future<void>.delayed(const Duration(milliseconds: 100));
  }
}
