import 'package:flutter/material.dart';

import '../controllers/announcements_controller.dart';
import '../controllers/messaging_controller.dart';
import '../controllers/parent_dashboard_controller.dart';
import '../controllers/progress_filter_controller.dart';
import '../models/parent_models.dart';
import '../models/repositories/parent_repository.dart';
import 'citations_screen.dart';
import 'login_screen.dart';
import 'widgets/alert_tile.dart';
import 'widgets/attendance_calendar.dart';
import 'widgets/message_bubble.dart';
import 'widgets/progress_bar.dart';
import 'widgets/stat_card.dart';
import 'widgets/term_line_chart.dart';

class ParentDashboardScreen extends StatefulWidget {
  const ParentDashboardScreen({
    super.key,
    required this.repository,
    required this.isDarkMode,
    required this.onDarkModeChanged,
  });

  final ParentRepository repository;
  final bool isDarkMode;
  final ValueChanged<bool> onDarkModeChanged;

  @override
  State<ParentDashboardScreen> createState() => _ParentDashboardScreenState();
}

class _ParentDashboardScreenState extends State<ParentDashboardScreen> {
  late final ParentDashboardController _controller;

  @override
  void initState() {
    super.initState();
    _controller = ParentDashboardController(repository: widget.repository)
      ..addListener(_onControllerChanged);
  }

  @override
  void dispose() {
    _controller
      ..removeListener(_onControllerChanged)
      ..dispose();
    super.dispose();
  }

  void _onControllerChanged() => setState(() {});

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<ParentDashboardData>(
      future: _controller.dashboardFuture,
      builder: (context, snapshot) {
        final data = snapshot.data;

        return Scaffold(
          appBar: AppBar(
            leading: data == null
                ? null
                : IconButton(
                    tooltip: 'Perfil',
                    icon: const Icon(Icons.account_circle_outlined),
                    onPressed: _controller.toggleProfile,
                  ),
            title: const Text(
              'Learnix',
              style: TextStyle(fontWeight: FontWeight.w900),
            ),
            actions: [
              if (data != null) ...[
                Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: _AlertAction(
                    count: _controller.reminders(data).length,
                    onPressed: () => _openReminders(context, data),
                  ),
                ),
                IconButton(
                  tooltip: 'Recargar datos',
                  icon: const Icon(Icons.refresh),
                  onPressed: () async {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Recargando datos...'),
                        duration: Duration(milliseconds: 500),
                      ),
                    );
                    try {
                      await _controller.refresh();
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('Datos actualizados'),
                            duration: Duration(milliseconds: 800),
                          ),
                        );
                      }
                    } catch (e) {
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Error al recargar: $e')),
                        );
                      }
                    }
                  },
                ),
                IconButton(
                  tooltip: 'Cerrar sesion',
                  icon: const Icon(Icons.logout),
                  onPressed: () {
                    Navigator.of(context).pushReplacement(
                      MaterialPageRoute(
                        builder: (_) => LoginScreen(
                          repository: widget.repository,
                          isDarkMode: widget.isDarkMode,
                          onDarkModeChanged: widget.onDarkModeChanged,
                        ),
                      ),
                    );
                  },
                ),
              ],
            ],
          ),
          body: SafeArea(
            child: switch (snapshot.connectionState) {
              ConnectionState.waiting =>
                const Center(child: CircularProgressIndicator()),
              _ when snapshot.hasError =>
                _ErrorState(message: '${snapshot.error}'),
              _ when data != null => _buildShell(data),
              _ => const _ErrorState(message: 'No se pudo cargar informacion.'),
            },
          ),
          bottomNavigationBar: NavigationBar(
            selectedIndex: _controller.selectedIndex,
            onDestinationSelected: _controller.selectTab,
            destinations: const [
              NavigationDestination(
                icon: Icon(Icons.dashboard_outlined),
                label: 'Inicio',
              ),
              NavigationDestination(
                icon: Icon(Icons.trending_up),
                label: 'Progreso',
              ),
              NavigationDestination(
                icon: Icon(Icons.chat_bubble_outline),
                label: 'Chat',
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildShell(ParentDashboardData data) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final panelWidth =
            constraints.maxWidth < 430 ? constraints.maxWidth * 0.88 : 360.0;

        return Stack(
          children: [
            _buildBody(data),
            IgnorePointer(
              ignoring: !_controller.isProfileOpen,
              child: AnimatedOpacity(
                duration: const Duration(milliseconds: 220),
                opacity: _controller.isProfileOpen ? 1 : 0,
                child: GestureDetector(
                  onTap: _controller.closeProfile,
                  child: Container(color: Colors.black.withValues(alpha: 0.28)),
                ),
              ),
            ),
            AnimatedPositioned(
              duration: const Duration(milliseconds: 280),
              curve: Curves.easeOutCubic,
              top: 0,
              bottom: 0,
              left: _controller.isProfileOpen ? 0 : -panelWidth,
              width: panelWidth,
              child: Material(
                elevation: 14,
                color: Theme.of(context).cardTheme.color,
                borderRadius: const BorderRadius.only(
                  topRight: Radius.circular(22),
                  bottomRight: Radius.circular(22),
                ),
                clipBehavior: Clip.antiAlias,
                child: _ProfileSheet(
                  data: data,
                  repository: widget.repository,
                  onDarkModeChanged: widget.onDarkModeChanged,
                  onClose: _controller.closeProfile,
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildBody(ParentDashboardData data) {
    final pages = [
      _OverviewTab(
        data: data,
        onShowAttendance: _controller.showAttendanceTab,
      ),
      _ProgressTab(data: data),
      _MessagingTab(data: data, repository: widget.repository),
    ];

    return IndexedStack(index: _controller.selectedIndex, children: pages);
  }

  void _openReminders(BuildContext context, ParentDashboardData data) {
    final reminders = _controller.reminders(data);
    showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Recordatorios'),
        content: SizedBox(
          width: double.maxFinite,
          child: reminders.isEmpty
              ? const Text('No hay recordatorios proximos.')
              : ListView.separated(
                  shrinkWrap: true,
                  itemBuilder: (context, index) {
                    final reminder = reminders[index];
                    return ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: Icon(reminder.icon, color: reminder.color),
                      title: Text(reminder.title),
                      subtitle: Text(reminder.detail),
                    );
                  },
                  separatorBuilder: (_, __) => const Divider(),
                  itemCount: reminders.length,
                ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cerrar'),
          ),
        ],
      ),
    );
  }
}

class _OverviewTab extends StatefulWidget {
  const _OverviewTab({required this.data, required this.onShowAttendance});

  final ParentDashboardData data;
  final VoidCallback onShowAttendance;

  @override
  State<_OverviewTab> createState() => _OverviewTabState();
}

class _OverviewTabState extends State<_OverviewTab> {
  bool _showAttendance = false;

  @override
  Widget build(BuildContext context) {
    final student = widget.data.student;
    return _PageScaffold(
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  student.fullName,
                  style: Theme.of(context)
                      .textTheme
                      .headlineSmall
                      ?.copyWith(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 6),
                Text('Grado y seccion: ${student.gradeSection}'),
              ],
            ),
          ),
        ),
        InkWell(
          borderRadius: BorderRadius.circular(18),
          onTap: () {
            setState(() => _showAttendance = true);
            widget.onShowAttendance();
          },
          child: StatCard(
            title: 'Asistencia',
            value: '${student.attendancePercentage.toStringAsFixed(0)}%',
            icon: Icons.event_available,
            color: const Color(0xFF198754),
          ),
        ),
        Center(
          child: OutlinedButton.icon(
            onPressed: () => setState(() => _showAttendance = !_showAttendance),
            icon: Icon(_showAttendance ? Icons.expand_less : Icons.visibility),
            label:
                Text(_showAttendance ? 'Ocultar asistencia' : 'Ver asistencia'),
          ),
        ),
        if (_showAttendance)
          _AttendancePanel(stats: widget.data.attendanceStats),
        _SectionCard(
          title: 'Rendimiento por curso',
          child: Column(
            children: widget.data.courseAverages
                .map(
                  (course) => _CoursePerformanceTile(
                    course: course,
                    activities: widget.data.activities
                        .where((item) => item.courseName == course.courseName)
                        .toList(),
                  ),
                )
                .toList(),
          ),
        ),
        _SectionCard(
          title: 'Reportes academicos',
          child: Column(
            children: widget.data.reports
                .map(
                  (report) => ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: const Icon(Icons.description_outlined),
                    title: Text(report.title),
                    subtitle: Text(report.description),
                    trailing: Wrap(
                      spacing: 4,
                      children: report.formats
                          .map(
                            (format) => Chip(
                              label: Text(
                                format == ReportFormat.pdf ? 'PDF' : 'Excel',
                              ),
                            ),
                          )
                          .toList(),
                    ),
                  ),
                )
                .toList(),
          ),
        ),
      ],
    );
  }
}

class _CoursePerformanceTile extends StatelessWidget {
  const _CoursePerformanceTile({
    required this.course,
    required this.activities,
  });

  final CourseAverage course;
  final List<StudentActivity> activities;

  @override
  Widget build(BuildContext context) {
    return ExpansionTile(
      tilePadding: EdgeInsets.zero,
      childrenPadding: const EdgeInsets.only(bottom: 12),
      title: CourseProgressBar(label: course.courseName, value: course.average),
      subtitle: const Text('Mostrar mas a detalle'),
      children: [
        _ActivitiesTable(activities: activities),
      ],
    );
  }
}

class _ProgressTab extends StatefulWidget {
  const _ProgressTab({required this.data});

  final ParentDashboardData data;

  @override
  State<_ProgressTab> createState() => _ProgressTabState();
}

class _ProgressTabState extends State<_ProgressTab> {
  final ProgressFilterController _controller = ProgressFilterController();

  @override
  void initState() {
    super.initState();
    _controller.addListener(_onControllerChanged);
  }

  @override
  void dispose() {
    _controller
      ..removeListener(_onControllerChanged)
      ..dispose();
    super.dispose();
  }

  void _onControllerChanged() => setState(() {});

  @override
  Widget build(BuildContext context) {
    final terms = _controller.terms(widget.data);
    final courses = _controller.courses(widget.data);
    final activities = _controller.activities(widget.data);
    final visibleProgress = _controller.visibleProgress(widget.data);

    return _PageScaffold(
      children: [
        _SectionCard(
          title: 'Filtros',
          child: Column(
            children: [
              DropdownButtonFormField<String>(
                initialValue: _controller.selectedTerm,
                decoration: const InputDecoration(
                  labelText: 'Bimestre',
                  border: OutlineInputBorder(),
                ),
                items: terms
                    .map((term) =>
                        DropdownMenuItem(value: term, child: Text(term)))
                    .toList(),
                onChanged: (value) => _controller.setTerm(value ?? 'Todos'),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                initialValue: _controller.selectedCourse,
                decoration: const InputDecoration(
                  labelText: 'Curso',
                  border: OutlineInputBorder(),
                ),
                items: courses
                    .map(
                      (course) =>
                          DropdownMenuItem(value: course, child: Text(course)),
                    )
                    .toList(),
                onChanged: (value) => _controller.setCourse(value ?? 'Todos'),
              ),
            ],
          ),
        ),
        _SectionCard(
          title: 'Evolucion temporal',
          child: TermLineChart(items: visibleProgress),
        ),
        _SectionCard(
          title: 'Actividades filtradas',
          child: _ActivitiesTable(activities: activities),
        ),
      ],
    );
  }
}

class _AttendancePanel extends StatelessWidget {
  const _AttendancePanel({required this.stats});

  final AttendanceStats stats;

  @override
  Widget build(BuildContext context) {
    return _SectionCard(
      title: 'Detalle de asistencia',
      child: Column(
        children: [
          LayoutBuilder(
            builder: (context, constraints) {
              final compact = constraints.maxWidth < 420;
              final cards = [
                StatCard(
                  title: 'Asistidos',
                  value: '${stats.attendedDays}',
                  icon: Icons.check_circle_outline,
                  color: const Color(0xFF198754),
                ),
                StatCard(
                  title: 'Faltas',
                  value: '${stats.absentDays}',
                  icon: Icons.cancel_outlined,
                  color: const Color(0xFFDC3545),
                ),
                StatCard(
                  title: 'Tardanzas',
                  value: '${stats.lateDays}',
                  icon: Icons.schedule,
                  color: const Color(0xFFFFC107),
                ),
              ];

              if (compact) {
                return Column(
                  children: [
                    for (final card in cards) ...[
                      card,
                      const SizedBox(height: 8)
                    ],
                  ],
                );
              }

              return Row(
                children: [
                  for (final card in cards) ...[
                    Expanded(child: card),
                    if (card != cards.last) const SizedBox(width: 8),
                  ],
                ],
              );
            },
          ),
          const SizedBox(height: 12),
          const Align(
            alignment: Alignment.centerLeft,
            child: Text('Verde asistio, rojo falto, amarillo tardanza'),
          ),
          const SizedBox(height: 10),
          AttendanceCalendar(days: stats.days),
        ],
      ),
    );
  }
}

class _MessagingTab extends StatefulWidget {
  const _MessagingTab({required this.data, required this.repository});

  final ParentDashboardData data;
  final ParentRepository repository;

  @override
  State<_MessagingTab> createState() => _MessagingTabState();
}

class _MessagingTabState extends State<_MessagingTab> {
  final TextEditingController _messageController = TextEditingController();
  late final MessagingController _controller =
      MessagingController(data: widget.data);

  @override
  void initState() {
    super.initState();
    _controller.addListener(_onControllerChanged);
  }

  @override
  void dispose() {
    _controller
      ..removeListener(_onControllerChanged)
      ..dispose();
    _messageController.dispose();
    super.dispose();
  }

  void _onControllerChanged() => setState(() {});

  @override
  Widget build(BuildContext context) {
    return _PageScaffold(
      children: [
        SizedBox(
          height: 92,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: widget.data.conversations.length,
            separatorBuilder: (_, __) => const SizedBox(width: 12),
            itemBuilder: (context, index) {
              final conversation = widget.data.conversations[index];
              final selected =
                  conversation.id == _controller.selectedConversation.id;
              return InkWell(
                borderRadius: BorderRadius.circular(42),
                onTap: () => _controller.selectConversation(conversation),
                child: SizedBox(
                  width: 78,
                  child: Column(
                    children: [
                      CircleAvatar(
                        radius: 28,
                        backgroundColor: selected
                            ? Theme.of(context).colorScheme.primary
                            : const Color(0xFFDBEAFE),
                        child: Text(
                          _initials(conversation.participantName),
                          style: TextStyle(
                            color: selected
                                ? Colors.white
                                : Theme.of(context).colorScheme.primary,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        conversation.participantName.split(' ').first,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontSize: 12),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
        _SectionCard(
          title: _controller.selectedConversation.participantName,
          trailing: Text(_controller.selectedConversation.role),
          child: Column(
            children: [
              Container(
                height: 360,
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: const Color(0xFFF9FAFB),
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(color: const Color(0xFFE5E7EB)),
                ),
                child: ListView(
                  children: _controller.messages
                      .map((message) => MessageBubble(message: message))
                      .toList(),
                ),
              ),
              const SizedBox(height: 10),
              Row(
                children: [
                  IconButton(
                    tooltip: 'Adjuntar archivo',
                    onPressed: () =>
                        _sendMessage(attachmentName: 'constancia.pdf'),
                    icon: const Icon(Icons.attach_file),
                  ),
                  Expanded(
                    child: TextField(
                      controller: _messageController,
                      decoration: const InputDecoration(
                        hintText: 'Escribe un mensaje',
                        border: OutlineInputBorder(),
                      ),
                      minLines: 1,
                      maxLines: 3,
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton.filled(
                    tooltip: 'Enviar',
                    onPressed: _sendMessage,
                    icon: const Icon(Icons.send),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }

  Future<void> _sendMessage({String? attachmentName}) async {
    await _controller.sendMessage(
      repository: widget.repository,
      text: _messageController.text,
      attachmentName: attachmentName,
    );
    _messageController.clear();
  }

  String _initials(String name) {
    final parts = name.trim().split(RegExp(r'\s+'));
    if (parts.length == 1) return parts.first.substring(0, 1).toUpperCase();
    return '${parts.first[0]}${parts.last[0]}'.toUpperCase();
  }
}

class _ProfileSheet extends StatelessWidget {
  const _ProfileSheet({
    required this.data,
    required this.repository,
    required this.onDarkModeChanged,
    required this.onClose,
  });

  final ParentDashboardData data;
  final ParentRepository repository;
  final ValueChanged<bool> onDarkModeChanged;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final isDarkMode = Theme.of(context).brightness == Brightness.dark;

    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
        children: [
          ListTile(
            contentPadding: EdgeInsets.zero,
            leading: CircleAvatar(
              backgroundColor: const Color(0xFFDBEAFE),
              child: Icon(
                Icons.person_outline,
                color: Theme.of(context).colorScheme.primary,
              ),
            ),
            title: const Text('Perfil de padre'),
            subtitle: Text(data.student.fullName),
            trailing: IconButton(
              tooltip: 'Cerrar perfil',
              onPressed: onClose,
              icon: const Icon(Icons.close),
            ),
          ),
          _ProfileSection(
            title: 'Bandeja de comunicados',
            icon: Icons.campaign_outlined,
            child: Column(
              children: [
                ...data.announcements.take(2).map(
                      (announcement) => ListTile(
                        contentPadding: EdgeInsets.zero,
                        leading: const Icon(Icons.campaign_outlined),
                        title: Text(announcement.title),
                        subtitle: Text(
                          '${announcement.sender} - ${_date(announcement.date)}',
                        ),
                      ),
                    ),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton.icon(
                    onPressed: () {
                      onClose();
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => _AnnouncementsScreen(
                            announcements: data.announcements,
                          ),
                        ),
                      );
                    },
                    icon: const Icon(Icons.open_in_new),
                    label: const Text('Ver comunicados'),
                  ),
                ),
              ],
            ),
          ),
          _ProfileSection(
            title: 'Citaciones virtuales',
            icon: Icons.video_call_outlined,
            child: Column(
              children: [
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.event_note_outlined),
                  title: const Text('Gestionar citaciones'),
                  subtitle: const Text(
                    'Aceptar, rechazar, confirmar y responder mensajes.',
                  ),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () {
                    onClose();
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => CitationsScreen(
                          repository: repository,
                          parentId: 1,
                          studentId: data.student.id,
                        ),
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
          _ProfileSection(
            title: 'Incidencias',
            icon: Icons.report_problem_outlined,
            child: Column(
              children: data.alerts
                  .where((alert) => alert.category == AlertCategory.incident)
                  .map(
                    (alert) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.report_problem_outlined),
                      title: Text(alert.title),
                      subtitle: Text(alert.detail),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () {
                        onClose();
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => _IncidentsScreen(
                              incidents: data.alerts
                                  .where(
                                    (item) =>
                                        item.category == AlertCategory.incident,
                                  )
                                  .toList(),
                            ),
                          ),
                        );
                      },
                    ),
                  )
                  .toList(),
            ),
          ),
          _ProfileSection(
            title: 'Configuracion',
            icon: Icons.settings_outlined,
            child: SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Modo oscuro'),
              value: isDarkMode,
              onChanged: onDarkModeChanged,
              secondary: const Icon(Icons.dark_mode_outlined),
            ),
          ),
        ],
      ),
    );
  }
}

class _IncidentsScreen extends StatelessWidget {
  const _IncidentsScreen({required this.incidents});

  final List<ParentAlert> incidents;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Incidencias')),
      body: incidents.isEmpty
          ? const Center(child: Text('No hay incidencias registradas.'))
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemBuilder: (context, index) =>
                  AlertTile(alert: incidents[index]),
              separatorBuilder: (_, __) => const SizedBox(height: 8),
              itemCount: incidents.length,
            ),
    );
  }
}

class _AnnouncementsScreen extends StatefulWidget {
  const _AnnouncementsScreen({required this.announcements});

  final List<Announcement> announcements;

  @override
  State<_AnnouncementsScreen> createState() => _AnnouncementsScreenState();
}

class _AnnouncementsScreenState extends State<_AnnouncementsScreen> {
  final AnnouncementsController _controller = AnnouncementsController();

  @override
  void initState() {
    super.initState();
    _controller.addListener(_onControllerChanged);
  }

  @override
  void dispose() {
    _controller
      ..removeListener(_onControllerChanged)
      ..dispose();
    super.dispose();
  }

  void _onControllerChanged() => setState(() {});

  @override
  Widget build(BuildContext context) {
    final priorities = _controller.priorities(widget.announcements);
    final senders = _controller.senders(widget.announcements);
    final filtered = _controller.filtered(widget.announcements);

    return Scaffold(
      appBar: AppBar(title: const Text('Comunicados')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  DropdownButtonFormField<String>(
                    initialValue: _controller.selectedPriority,
                    decoration: const InputDecoration(
                      labelText: 'Prioridad',
                      border: OutlineInputBorder(),
                    ),
                    items: priorities
                        .map(
                          (priority) => DropdownMenuItem(
                            value: priority,
                            child: Text(priority),
                          ),
                        )
                        .toList(),
                    onChanged: (value) =>
                        _controller.setPriority(value ?? 'Todas'),
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<String>(
                    initialValue: _controller.selectedSender,
                    decoration: const InputDecoration(
                      labelText: 'Remitente',
                      border: OutlineInputBorder(),
                    ),
                    items: senders
                        .map(
                          (sender) => DropdownMenuItem(
                            value: sender,
                            child: Text(sender),
                          ),
                        )
                        .toList(),
                    onChanged: (value) =>
                        _controller.setSender(value ?? 'Todos'),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          if (filtered.isEmpty)
            const Padding(
              padding: EdgeInsets.all(20),
              child:
                  Center(child: Text('No hay comunicados para este filtro.')),
            )
          else
            ...filtered.map(
              (announcement) => Card(
                child: ListTile(
                  leading: const Icon(Icons.campaign_outlined),
                  title: Text(announcement.title),
                  subtitle: Text(
                    '${announcement.sender} - ${_date(announcement.date)}',
                  ),
                  trailing: Chip(label: Text(announcement.priority)),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _ProfileSection extends StatelessWidget {
  const _ProfileSection({
    required this.title,
    required this.icon,
    required this.child,
  });

  final String title;
  final IconData icon;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ExpansionTile(
        leading: Icon(icon),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
        childrenPadding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
        children: [child],
      ),
    );
  }
}

class _ActivitiesTable extends StatelessWidget {
  const _ActivitiesTable({required this.activities});

  final List<StudentActivity> activities;

  @override
  Widget build(BuildContext context) {
    if (activities.isEmpty) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 16),
        child: Text('No hay actividades para este filtro.'),
      );
    }

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: DataTable(
        columns: const [
          DataColumn(label: Text('Actividad')),
          DataColumn(label: Text('Curso')),
          DataColumn(label: Text('Bim.')),
          DataColumn(label: Text('Fecha')),
          DataColumn(label: Text('Nota')),
          DataColumn(label: Text('Estado')),
        ],
        rows: activities
            .map(
              (activity) => DataRow(
                cells: [
                  DataCell(Text(activity.name)),
                  DataCell(Text(activity.courseName)),
                  DataCell(Text(activity.term)),
                  DataCell(Text(_date(activity.date))),
                  DataCell(Text(activity.grade)),
                  DataCell(Text(activity.status)),
                ],
              ),
            )
            .toList(),
      ),
    );
  }
}

class _AlertAction extends StatelessWidget {
  const _AlertAction({required this.count, required this.onPressed});

  final int count;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return Badge(
      isLabelVisible: count > 0,
      label: Text('$count'),
      child: IconButton(
        tooltip: 'Recordatorios',
        onPressed: onPressed,
        icon: const Icon(Icons.notifications_none),
      ),
    );
  }
}

class _PageScaffold extends StatelessWidget {
  const _PageScaffold({required this.children});

  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
      children: [
        for (final child in children) ...[
          child,
          const SizedBox(height: 12),
        ],
      ],
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({required this.title, required this.child, this.trailing});

  final String title;
  final Widget child;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Text(
                    title,
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(fontWeight: FontWeight.w900),
                  ),
                ),
                if (trailing != null)
                  Flexible(
                    child: DefaultTextStyle.merge(
                      style: Theme.of(context).textTheme.labelSmall,
                      textAlign: TextAlign.right,
                      child: trailing!,
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 12),
            child,
          ],
        ),
      ),
    );
  }
}

class _ErrorState extends StatelessWidget {
  const _ErrorState({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Text(message, textAlign: TextAlign.center),
      ),
    );
  }
}

String _date(DateTime date) {
  return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
}
