import '../parent_models.dart';

abstract interface class ParentRepository {
  Future<bool> login({required String email, required String password});

  Future<ParentDashboardData> loadDashboard({required int parentId});

  Future<ChatMessage> sendMessage({
    required int conversationId,
    required String text,
    String? attachmentName,
  });

  Future<List<Citation>> loadCitations({
    required int parentId,
    required int studentId,
  });

  Future<Citation> respondToCitation({
    required int citationId,
    required CitationStatus status,
    String? reason,
  });

  Future<Citation> confirmCitation({required int citationId});

  Future<List<CitationMessage>> loadCitationMessages({
    required int citationId,
  });

  Future<CitationMessage> sendCitationMessage({
    required int citationId,
    required String body,
  });

  Future<void> updatePreferences(
      {required int parentId, required bool darkMode});
}
