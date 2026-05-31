import 'package:flutter/material.dart';

import '../models/parent_models.dart';
import '../models/repositories/parent_repository.dart';

class MessagingController extends ChangeNotifier {
  MessagingController({required ParentDashboardData data})
      : _selectedConversation = data.conversations.first,
        _messages = List.of(data.conversations.first.messages);

  Conversation _selectedConversation;
  final List<ChatMessage> _messages;

  Conversation get selectedConversation => _selectedConversation;
  List<ChatMessage> get messages => List.unmodifiable(_messages);

  void selectConversation(Conversation conversation) {
    if (_selectedConversation.id == conversation.id) return;
    _selectedConversation = conversation;
    _messages
      ..clear()
      ..addAll(conversation.messages);
    notifyListeners();
  }

  Future<void> sendMessage({
    required ParentRepository repository,
    required String text,
    String? attachmentName,
  }) async {
    final cleanText = text.trim();
    if (cleanText.isEmpty && attachmentName == null) return;
    final message = await repository.sendMessage(
      conversationId: _selectedConversation.id,
      text: cleanText.isEmpty ? 'Archivo adjunto' : cleanText,
      attachmentName: attachmentName,
    );
    _messages.add(message);
    notifyListeners();
  }
}
