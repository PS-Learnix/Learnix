import 'package:flutter/material.dart';

import '../../models/parent_models.dart';

class AlertTile extends StatelessWidget {
  const AlertTile({super.key, required this.alert});

  final ParentAlert alert;

  @override
  Widget build(BuildContext context) {
    final color = switch (alert.severity) {
      AlertSeverity.info => const Color(0xFF2563EB),
      AlertSeverity.warning => const Color(0xFFD97706),
      AlertSeverity.critical => const Color(0xFFDC2626),
    };

    return Card(
      child: ListTile(
        leading: Icon(Icons.notifications_active_outlined, color: color),
        title: Text(
          alert.title,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(fontWeight: FontWeight.w800),
        ),
        subtitle: Text(
          '${alert.detail}\n${_date(alert.date)}',
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
        trailing: Chip(
          label: Text(_status(alert.status)),
          visualDensity: VisualDensity.compact,
        ),
      ),
    );
  }

  String _status(AlertStatus status) {
    return switch (status) {
      AlertStatus.newAlert => 'Nueva',
      AlertStatus.read => 'Leida',
      AlertStatus.archived => 'Archivada',
    };
  }

  String _date(DateTime date) {
    return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
  }
}
