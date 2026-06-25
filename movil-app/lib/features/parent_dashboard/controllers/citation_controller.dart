import 'package:flutter/material.dart';

import '../models/parent_models.dart';
import '../models/repositories/parent_repository.dart';

class CitationController extends ChangeNotifier {
  CitationController({
    required this.repository,
    required this.parentId,
    required this.studentId,
  }) {
    load();
  }

  final ParentRepository repository;
  final int parentId;
  final int studentId;

  bool _isLoading = true;
  bool _isDisposed = false;
  String? _error;
  List<Citation> _citations = [];
  Citation? _selectedCitation;
  List<CitationMessage> _messages = [];

  bool get isLoading => _isLoading;
  String? get error => _error;
  List<Citation> get citations => List.unmodifiable(_citations);
  Citation? get selectedCitation => _selectedCitation;
  List<CitationMessage> get messages => List.unmodifiable(_messages);

  Future<void> load() async {
    _isLoading = true;
    _error = null;
    _safeNotifyListeners();
    try {
      _citations = await repository.loadCitations(
        parentId: parentId,
        studentId: studentId,
      );
      if (_citations.isNotEmpty) {
        await selectCitation(_citations.first, shouldNotify: false);
      }
    } catch (e) {
      _error = '$e';
    } finally {
      _isLoading = false;
      _safeNotifyListeners();
    }
  }

  Future<void> selectCitation(
    Citation citation, {
    bool shouldNotify = true,
  }) async {
    _selectedCitation = citation;
    if (shouldNotify) _safeNotifyListeners();
    try {
      final messages =
          await repository.loadCitationMessages(citationId: citation.id);
      if (_selectedCitation?.id != citation.id) return;
      _messages = messages;
      _error = null;
    } catch (e) {
      if (_selectedCitation?.id != citation.id) return;
      _messages = [];
      _error = '$e';
    }
    _safeNotifyListeners();
  }

  Future<void> acceptSelected() {
    return _updateSelected(CitationStatus.accepted);
  }

  Future<void> rejectSelected(String reason) {
    if (reason.trim().isEmpty) {
      _error = 'Debe escribir una razon para rechazar la citacion.';
      _safeNotifyListeners();
      return Future<void>.value();
    }
    return _updateSelected(CitationStatus.rejected, reason: reason);
  }

  Future<void> confirmSelected() async {
    final citation = _selectedCitation;
    if (citation == null) return;
    final updated =
        await repository.confirmCitation(citationId: citation.id);
    _replaceCitation(updated);
  }

  Future<void> sendMessage(String body) async {
    final citation = _selectedCitation;
    final cleanBody = body.trim();
    if (citation == null || cleanBody.isEmpty) return;
    final message = await repository.sendCitationMessage(
      citationId: citation.id,
      body: cleanBody,
    );
    _messages = [..._messages, message];
    _safeNotifyListeners();
  }

  Future<void> _updateSelected(
    CitationStatus status, {
    String? reason,
  }) async {
    final citation = _selectedCitation;
    if (citation == null) return;
    final updated = await repository.respondToCitation(
      citationId: citation.id,
      status: status,
      reason: reason,
    );
    _replaceCitation(updated);
  }

  void _replaceCitation(Citation updated) {
    _citations = [
      for (final item in _citations)
        if (item.id == updated.id) updated else item,
    ];
    _selectedCitation = updated;
    _safeNotifyListeners();
  }

  void _safeNotifyListeners() {
    if (_isDisposed) return;
    notifyListeners();
  }

  @override
  void dispose() {
    _isDisposed = true;
    super.dispose();
  }
}
