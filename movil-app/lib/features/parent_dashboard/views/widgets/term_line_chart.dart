import 'package:flutter/material.dart';

import '../../models/parent_models.dart';

class TermLineChart extends StatelessWidget {
  const TermLineChart({super.key, required this.items});

  final List<TermProgress> items;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 180,
      child: CustomPaint(
        painter: _TermLineChartPainter(
          items: items,
          color: Theme.of(context).colorScheme.primary,
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.end,
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: items
              .map(
                (item) => Padding(
                  padding: const EdgeInsets.only(bottom: 4),
                  child: Text(item.term,
                      style: Theme.of(context).textTheme.labelSmall),
                ),
              )
              .toList(),
        ),
      ),
    );
  }
}

class _TermLineChartPainter extends CustomPainter {
  const _TermLineChartPainter({required this.items, required this.color});

  final List<TermProgress> items;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    if (items.isEmpty) return;

    final gridPaint = Paint()
      ..color = const Color(0xFFE5E7EB)
      ..strokeWidth = 1;
    final linePaint = Paint()
      ..color = color
      ..strokeWidth = 3
      ..style = PaintingStyle.stroke;
    final pointPaint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    for (var i = 0; i < 4; i++) {
      final y = 20 + i * ((size.height - 50) / 3);
      canvas.drawLine(Offset(0, y), Offset(size.width, y), gridPaint);
    }

    final path = Path();
    for (var i = 0; i < items.length; i++) {
      final x = items.length == 1
          ? size.width / 2
          : i * size.width / (items.length - 1);
      final y = 20 + (1 - items[i].average / 20) * (size.height - 60);
      final point = Offset(x, y);
      if (i == 0) {
        path.moveTo(point.dx, point.dy);
      } else {
        path.lineTo(point.dx, point.dy);
      }
      canvas.drawCircle(point, 5, pointPaint);
    }
    canvas.drawPath(path, linePaint);
  }

  @override
  bool shouldRepaint(covariant _TermLineChartPainter oldDelegate) {
    return oldDelegate.items != items || oldDelegate.color != color;
  }
}
