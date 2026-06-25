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
  bool _isResponding = false;
  bool _isSendingMessage = false;
  bool _isDisposed = false;
  String? _error;
  List<Citation> _citations = [];
  Citation? _selectedCitation;
  List<CitationMessage> _messages = [];

  bool get isLoading => _isLoading;
  bool get isResponding => _isResponding;
  bool get isSendingMessage => _isSendingMessage;
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

  Future<void> rejectSelected() {
    return _updateSelected(CitationStatus.rejected);
  }

  Future<void> sendMessage(String body) async {
    final citation = _selectedCitation;
    final cleanBody = body.trim();
    if (citation == null || cleanBody.isEmpty) return;
    _isSendingMessage = true;
    _error = null;
    _safeNotifyListeners();
    try {
      final message = await repository.sendCitationMessage(
        citationId: citation.id,
        body: cleanBody,
      );
      _messages = [..._messages, message];
      _error = null;
    } catch (e) {
      _error = '$e';
    } finally {
      _isSendingMessage = false;
      _safeNotifyListeners();
    }
  }

  Future<void> _updateSelected(CitationStatus status) async {
    final citation = _selectedCitation;
    if (citation == null) return;
    _isResponding = true;
    _error = null;
    _safeNotifyListeners();
    try {
      final updated = await repository.respondToCitation(
        citationId: citation.id,
        status: status,
      );
      _replaceCitation(updated);
    } catch (e) {
      _error = '$e';
      _safeNotifyListeners();
    } finally {
      _isResponding = false;
      _safeNotifyListeners();
    }
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
