import 'package:flutter_test/flutter_test.dart';
import 'package:learnix_parent_app/app/learnix_parent_app.dart';
import 'package:learnix_parent_app/features/parent_dashboard/models/repositories/mock_parent_repository.dart';

void main() {
  testWidgets('muestra dashboard de padres', (tester) async {
    await tester.pumpWidget(
      LearnixParentApp(repository: MockParentRepository()),
    );
    await tester.pumpAndSettle();

    expect(find.text('Ingresar'), findsOneWidget);
    await tester.tap(find.text('Ingresar'));
    await tester.pumpAndSettle();

    expect(find.text('Learnix'), findsOneWidget);
    expect(find.text('Juan Perez'), findsWidgets);
    expect(find.text('Progreso'), findsOneWidget);
    expect(find.text('Ver asistencia'), findsOneWidget);
  });
}
