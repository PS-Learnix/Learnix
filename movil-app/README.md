# Learnix Parent App

Aplicacion Flutter ubicada de forma aislada en `movil-app`.

## Alcance implementado

- RF08: progreso academico del estudiante.
- RF09 y RF11: centro de alertas e incidencias.
- RF10: reportes academicos disponibles en PDF y Excel.
- RF12: bandeja de comunicados directos.
- RF13: mensajeria bidireccional padre-docente/coordinador.

## Relacion con la base de datos web

La capa `ParentRepository` consume datos equivalentes a las tablas existentes en `web-app`:

- `students`
- `courses`
- `course_periods`
- `enrollments`
- `attendances`
- `activities`
- `grades`
- `users`

El archivo `database/V14__create_parent_mobile_tables.sql` agrega las tablas necesarias para alertas,
comunicados, conversaciones, mensajes, adjuntos y reportes sin modificar la carpeta `web-app`.

## Ejecutar

```powershell
flutter pub get
flutter run
```
