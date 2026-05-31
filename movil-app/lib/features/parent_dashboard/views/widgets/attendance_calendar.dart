import 'package:flutter/material.dart';

import '../../models/parent_models.dart';

class AttendanceCalendar extends StatelessWidget {
  const AttendanceCalendar({super.key, required this.days});

  final List<AttendanceDay> days;

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: days.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 7,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
      ),
      itemBuilder: (context, index) {
        final day = days[index];
        return Tooltip(
          message: _statusText(day.status),
          child: Container(
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: _statusColor(day.status),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              '${day.date.day}',
              style: const TextStyle(
                  color: Colors.white, fontWeight: FontWeight.w800),
            ),
          ),
        );
      },
    );
  }

  Color _statusColor(AttendanceStatus status) {
    return switch (status) {
      AttendanceStatus.attended => const Color(0xFF16A34A),
      AttendanceStatus.absent => const Color(0xFFDC2626),
      AttendanceStatus.late => const Color(0xFFEAB308),
    };
  }

  String _statusText(AttendanceStatus status) {
    return switch (status) {
      AttendanceStatus.attended => 'Asistio',
      AttendanceStatus.absent => 'Falto',
      AttendanceStatus.late => 'Tardanza',
    };
  }
}
