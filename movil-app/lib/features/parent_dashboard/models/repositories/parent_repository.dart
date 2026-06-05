import '../parent_models.dart';

abstract interface class ParentRepository {
  Future<bool> login({required String email, required String password});

  Future<ParentDashboardData> loadDashboard({required int parentId});

  Future<ChatMessage> sendMessage({
    required int conversationId,
    required String text,
    String? attachmentName,
  });

  Future<void> updatePreferences({required int parentId, required bool darkMode});
}
