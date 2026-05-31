import 'package:flutter/material.dart';

import '../../models/parent_models.dart';

class MessageBubble extends StatelessWidget {
  const MessageBubble({super.key, required this.message});

  final ChatMessage message;

  @override
  Widget build(BuildContext context) {
    final isMine = message.isFromParent;
    final color = isMine ? Theme.of(context).colorScheme.primary : Colors.white;
    final foreground = isMine ? Colors.white : const Color(0xFF111827);

    return Align(
      alignment: isMine ? Alignment.centerRight : Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 280),
        child: Container(
          margin: const EdgeInsets.symmetric(vertical: 5),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(8),
            border: isMine ? null : Border.all(color: const Color(0xFFE5E7EB)),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(message.text, style: TextStyle(color: foreground)),
              if (message.attachmentName != null) ...[
                const SizedBox(height: 8),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.attach_file, size: 16, color: foreground),
                    const SizedBox(width: 4),
                    Flexible(
                      child: Text(
                        message.attachmentName!,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(color: foreground),
                      ),
                    ),
                  ],
                ),
              ],
              const SizedBox(height: 6),
              Text(
                message.isRead ? 'Leido' : 'Enviado',
                style: TextStyle(
                    color: foreground.withValues(alpha: 0.72), fontSize: 11),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
