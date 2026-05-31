import '../parent_models.dart';

abstract interface class ParentRepository {
  Future<ParentDashboardData> loadDashboard({required int parentId});

  Future<ChatMessage> sendMessage({
    required int conversationId,
    required String text,
    String? attachmentName,
  });
}
