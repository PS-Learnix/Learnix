import 'package:flutter/material.dart';

import '../controllers/citation_controller.dart';
import '../models/parent_models.dart';
import '../models/repositories/parent_repository.dart';

class CitationsScreen extends StatefulWidget {
  const CitationsScreen({
    super.key,
    required this.repository,
    required this.parentId,
    required this.studentId,
  });

  final ParentRepository repository;
  final int parentId;
  final int studentId;

  @override
  State<CitationsScreen> createState() => _CitationsScreenState();
}

class _CitationsScreenState extends State<CitationsScreen> {
  final TextEditingController _messageController = TextEditingController();
  late final CitationController _controller = CitationController(
    repository: widget.repository,
    parentId: widget.parentId,
    studentId: widget.studentId,
  );

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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Citaciones virtuales'),
        actions: [
          IconButton(
            tooltip: 'Actualizar',
            onPressed: _controller.load,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: _controller.isLoading
          ? const Center(child: CircularProgressIndicator())
          : _controller.citations.isEmpty
              ? (_controller.error == null
                  ? const _EmptyState()
                  : _ErrorState(message: _controller.error!))
              : _buildContent(context),
    );
  }

  Widget _buildContent(BuildContext context) {
    final selected = _controller.selectedCitation;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        SizedBox(
          height: 126,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: _controller.citations.length,
            separatorBuilder: (_, __) => const SizedBox(width: 10),
            itemBuilder: (context, index) {
              final citation = _controller.citations[index];
              final isSelected = selected?.id == citation.id;
              return _CitationChip(
                citation: citation,
                isSelected: isSelected,
                onTap: () => _controller.selectCitation(citation),
              );
            },
          ),
        ),
        const SizedBox(height: 12),
        if (selected != null) ...[
          if (_controller.error != null) ...[
            _InlineErrorBanner(message: _controller.error!),
            const SizedBox(height: 12),
          ],
          _CitationDetailCard(
            citation: selected,
            isResponding: _controller.isResponding,
            onAccept: () => _acceptCitation(context),
            onReject: () => _rejectCitation(context),
          ),
          const SizedBox(height: 12),
          _MessagesCard(
            messages: _controller.messages,
            controller: _messageController,
            isSending: _controller.isSendingMessage,
            onSend: _sendMessage,
          ),
        ],
      ],
    );
  }

  Future<void> _rejectCitation(BuildContext context) async {
    await _controller.rejectSelected();
    if (!context.mounted) return;
    if (_controller.error == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Citacion rechazada.')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_controller.error!)),
      );
    }
  }

  Future<void> _acceptCitation(BuildContext context) async {
    await _controller.acceptSelected();
    if (!context.mounted) return;
    if (_controller.error == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Citacion aceptada.')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_controller.error!)),
      );
    }
  }

  Future<void> _sendMessage() async {
    final text = _messageController.text;
    await _controller.sendMessage(_messageController.text);
    if (!mounted) return;
    if (_controller.error == null && text.trim().isNotEmpty) {
      _messageController.clear();
    } else if (_controller.error != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_controller.error!)),
      );
    }
  }
}

class _CitationChip extends StatelessWidget {
  const _CitationChip({
    required this.citation,
    required this.isSelected,
    required this.onTap,
  });

  final Citation citation;
  final bool isSelected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final color = _statusColor(citation.status);
    return InkWell(
      borderRadius: BorderRadius.circular(12),
      onTap: onTap,
      child: Container(
        width: 236,
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isSelected
              ? Theme.of(context).colorScheme.primaryContainer
              : Theme.of(context).cardColor,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected
                ? Theme.of(context).colorScheme.primary
                : Theme.of(context).dividerColor,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.event_note_outlined, color: color),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    citation.title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontWeight: FontWeight.w900),
                  ),
                ),
              ],
            ),
            const Spacer(),
            Text(_dateTime(citation.scheduledAt)),
            const SizedBox(height: 4),
            Text(_statusLabel(citation.status)),
          ],
        ),
      ),
    );
  }
}

class _CitationDetailCard extends StatelessWidget {
  const _CitationDetailCard({
    required this.citation,
    required this.isResponding,
    required this.onAccept,
    required this.onReject,
  });

  final Citation citation;
  final bool isResponding;
  final VoidCallback onAccept;
  final VoidCallback onReject;

  @override
  Widget build(BuildContext context) {
    final canRespond = citation.status == CitationStatus.pending;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    citation.title,
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(fontWeight: FontWeight.w900),
                  ),
                ),
                Chip(label: Text(_statusLabel(citation.status))),
              ],
            ),
            const SizedBox(height: 8),
            Text(citation.detail),
            const SizedBox(height: 12),
            _InfoRow(
              icon: Icons.person_outline,
              label: 'Responsable',
              value: citation.teacherName,
            ),
            _InfoRow(
              icon: Icons.schedule,
              label: 'Fecha',
              value: _dateTime(citation.scheduledAt),
            ),
            _InfoRow(
              icon: citation.mode == CitationMode.virtual
                  ? Icons.video_call_outlined
                  : Icons.location_on_outlined,
              label: 'Modalidad',
              value: citation.mode == CitationMode.virtual
                  ? 'Virtual'
                  : 'Presencial',
            ),
            if (citation.meetingUrl != null)
              _InfoRow(
                icon: Icons.link,
                label: 'Enlace',
                value: citation.meetingUrl!,
              ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                FilledButton.icon(
                  onPressed: canRespond && !isResponding ? onAccept : null,
                  icon: isResponding
                      ? const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.check),
                  label: const Text('Aceptar'),
                ),
                OutlinedButton.icon(
                  onPressed: canRespond && !isResponding ? onReject : null,
                  icon: const Icon(Icons.close),
                  label: const Text('Rechazar'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _MessagesCard extends StatelessWidget {
  const _MessagesCard({
    required this.messages,
    required this.controller,
    required this.isSending,
    required this.onSend,
  });

  final List<CitationMessage> messages;
  final TextEditingController controller;
  final bool isSending;
  final VoidCallback onSend;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Comunicacion de la citacion',
              style: Theme.of(context)
                  .textTheme
                  .titleMedium
                  ?.copyWith(fontWeight: FontWeight.w900),
            ),
            const SizedBox(height: 12),
            Container(
              height: 300,
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(12),
              ),
              child: messages.isEmpty
                  ? const Center(child: Text('No hay mensajes aun.'))
                  : ListView.separated(
                      itemCount: messages.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 8),
                      itemBuilder: (context, index) =>
                          _CitationMessageBubble(message: messages[index]),
                    ),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: controller,
                    decoration: const InputDecoration(
                      hintText: 'Escribe una respuesta',
                      border: OutlineInputBorder(),
                    ),
                    minLines: 1,
                    maxLines: 3,
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  tooltip: 'Enviar',
                  onPressed: isSending ? null : onSend,
                  icon: isSending
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.send),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _InlineErrorBanner extends StatelessWidget {
  const _InlineErrorBanner({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Theme.of(context).colorScheme.errorContainer,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Icon(
              Icons.error_outline,
              color: Theme.of(context).colorScheme.onErrorContainer,
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                message,
                style: TextStyle(
                  color: Theme.of(context).colorScheme.onErrorContainer,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CitationMessageBubble extends StatelessWidget {
  const _CitationMessageBubble({required this.message});

  final CitationMessage message;

  @override
  Widget build(BuildContext context) {
    final alignment =
        message.isFromParent ? Alignment.centerRight : Alignment.centerLeft;
    final color = message.isFromParent
        ? Theme.of(context).colorScheme.primary
        : Theme.of(context).colorScheme.surface;
    final textColor = message.isFromParent
        ? Theme.of(context).colorScheme.onPrimary
        : Theme.of(context).colorScheme.onSurface;

    return Align(
      alignment: alignment,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 280),
        child: DecoratedBox(
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Padding(
            padding: const EdgeInsets.all(10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${message.senderName} - ${message.senderRole}',
                  style: TextStyle(
                    color: textColor,
                    fontWeight: FontWeight.w800,
                    fontSize: 12,
                  ),
                ),
                const SizedBox(height: 4),
                Text(message.body, style: TextStyle(color: textColor)),
                const SizedBox(height: 4),
                Text(
                  _time(message.sentAt),
                  style: TextStyle(color: textColor, fontSize: 11),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({
    required this.icon,
    required this.label,
    required this.value,
  });

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 20),
          const SizedBox(width: 8),
          Expanded(
            child: Text('$label: $value'),
          ),
        ],
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState();

  @override
  Widget build(BuildContext context) {
    return const Center(child: Text('No hay citaciones registradas.'));
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

Color _statusColor(CitationStatus status) {
  switch (status) {
    case CitationStatus.accepted:
      return const Color(0xFF198754);
    case CitationStatus.rejected:
    case CitationStatus.cancelled:
      return const Color(0xFFDC3545);
    case CitationStatus.pending:
      return const Color(0xFFFFC107);
  }
}

String _statusLabel(CitationStatus status) {
  switch (status) {
    case CitationStatus.accepted:
      return 'Aceptada';
    case CitationStatus.rejected:
      return 'Rechazada';
    case CitationStatus.cancelled:
      return 'Cancelada';
    case CitationStatus.pending:
      return 'Pendiente';
  }
}

String _dateTime(DateTime date) {
  final day = date.day.toString().padLeft(2, '0');
  final month = date.month.toString().padLeft(2, '0');
  final hour = date.hour.toString().padLeft(2, '0');
  final minute = date.minute.toString().padLeft(2, '0');
  return '$day/$month/${date.year} $hour:$minute';
}

String _time(DateTime date) {
  final hour = date.hour.toString().padLeft(2, '0');
  final minute = date.minute.toString().padLeft(2, '0');
  return '$hour:$minute';
}
