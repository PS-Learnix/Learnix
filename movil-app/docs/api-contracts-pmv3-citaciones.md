# Contratos API - PMV3 Modulo de Citaciones Virtuales

Este documento valida y documenta los contratos necesarios para que `movil-app` renderice el modulo PMV3 sin modificar `web-app`.

## Alcance PMV3

Requerimientos revisados del documento actualizado:

- `RF13` Comunicacion bidireccional: padre y docente deben poder intercambiar mensajes trazables.
- `RF14` Agendar citas: el docente programa fecha, motivo y modalidad.
- `RF15` Aceptar/rechazar citas: el padre gestiona la invitacion desde la app movil.
- `RF16` Enviar citaciones masivas: el colegio envia citaciones a una seccion o grupo.
- `RF17` Confirmar citaciones: el padre confirma recepcion y/o asistencia.

La app movil quedo preparada con `Citation`, `CitationMessage`, `CitationController`, `CitationsScreen` y metodos de `ParentRepository` para consumir estos endpoints cuando el backend este disponible.

## Convenciones

- Base path sugerido: `/api/mobile`
- Autenticacion: `Authorization: Bearer <token>`
- Fechas: ISO 8601 (`YYYY-MM-DDTHH:mm:ss`)
- Estados de citacion: `pending`, `accepted`, `rejected`, `confirmed`, `cancelled`
- Modalidades: `virtual`, `in_person`
- Cuerpo de error sugerido:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "La citacion no puede confirmarse en su estado actual.",
  "details": []
}
```

Codigos comunes: `200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`, `422`, `500`.

## Endpoints

## 1. Listar citaciones del padre

**Nombre:** Listar citaciones PMV3  
**Metodo HTTP:** `GET`  
**Ruta sugerida:** `/api/mobile/parents/{parentId}/students/{studentId}/citations`

**Descripcion funcional:** obtiene citaciones individuales o masivas dirigidas al padre para el estudiante seleccionado.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `parentId` | path | int | Si |
| `studentId` | path | int | Si |
| `status` | query | string | No |
| `from` | query | date | No |
| `to` | query | date | No |

**Request esperado:**

```http
GET /api/mobile/parents/1/students/1/citations?status=pending
Authorization: Bearer <token>
```

**Response esperado `200`:**

```json
{
  "items": [
    {
      "id": 101,
      "title": "Citacion virtual con tutoria",
      "detail": "Revision de avance academico y acuerdos de apoyo en casa.",
      "teacherName": "Ana Gomez",
      "scheduledAt": "2026-06-25T17:00:00",
      "status": "pending",
      "mode": "virtual",
      "meetingUrl": "https://meet.learnix.edu/cita-101",
      "scope": "individual",
      "unreadMessages": 1
    }
  ]
}
```

**Codigos de estado posibles:** `200`, `401`, `403`, `404`, `500`.

**Observaciones tecnicas:** debe validar que `parentId` este vinculado al `studentId`. Las citaciones masivas se devuelven si el estudiante pertenece a la seccion destinataria.

**Pantalla/componente movil:** `CitationsScreen`, carrusel superior de citaciones.

## 2. Obtener detalle de citacion

**Nombre:** Detalle de citacion PMV3  
**Metodo HTTP:** `GET`  
**Ruta sugerida:** `/api/mobile/citations/{citationId}`

**Descripcion funcional:** obtiene la informacion completa de una citacion, incluyendo estado del destinatario actual.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `citationId` | path | int | Si |

**Request esperado:**

```http
GET /api/mobile/citations/101
Authorization: Bearer <token>
```

**Response esperado `200`:**

```json
{
  "id": 101,
  "title": "Citacion virtual con tutoria",
  "detail": "Revision de avance academico y acuerdos de apoyo en casa.",
  "teacherName": "Ana Gomez",
  "scheduledAt": "2026-06-25T17:00:00",
  "status": "pending",
  "mode": "virtual",
  "meetingUrl": "https://meet.learnix.edu/cita-101",
  "createdAt": "2026-06-17T10:00:00",
  "updatedAt": "2026-06-17T10:00:00"
}
```

**Codigos de estado posibles:** `200`, `401`, `403`, `404`, `500`.

**Observaciones tecnicas:** el endpoint debe retornar el estado del receptor, no solo el estado global de la citacion.

**Pantalla/componente movil:** `CitationsScreen`, tarjeta de detalle.

## 3. Aceptar o rechazar citacion

**Nombre:** Responder citacion PMV3  
**Metodo HTTP:** `PATCH`  
**Ruta sugerida:** `/api/mobile/citations/{citationId}/response`

**Descripcion funcional:** permite que el padre acepte o rechace una invitacion de citacion.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `citationId` | path | int | Si |
| `status` | body | string | Si |
| `reason` | body | string | No |

**Request esperado:**

```json
{
  "status": "accepted",
  "reason": null
}
```

Para rechazo:

```json
{
  "status": "rejected",
  "reason": "No puedo asistir en ese horario."
}
```

**Response esperado `200`:**

```json
{
  "citation": {
    "id": 101,
    "title": "Citacion virtual con tutoria",
    "detail": "Revision de avance academico y acuerdos de apoyo en casa.",
    "teacherName": "Ana Gomez",
    "scheduledAt": "2026-06-25T17:00:00",
    "status": "accepted",
    "mode": "virtual",
    "meetingUrl": "https://meet.learnix.edu/cita-101"
  },
  "eventId": 9001
}
```

**Codigos de estado posibles:** `200`, `400`, `401`, `403`, `404`, `409`, `422`, `500`.

**Observaciones tecnicas:** solo debe aceptar `accepted` o `rejected`. Si la cita esta `cancelled` o ya vencio, responder `409`.

**Pantalla/componente movil:** botones `Aceptar` y `Rechazar` de `CitationsScreen`.

## 4. Confirmar recepcion o asistencia

**Nombre:** Confirmar citacion PMV3  
**Metodo HTTP:** `PATCH`  
**Ruta sugerida:** `/api/mobile/citations/{citationId}/confirm`

**Descripcion funcional:** registra que el padre confirma recepcion y/o asistencia a la citacion.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `citationId` | path | int | Si |

**Request esperado:**

```json
{
  "confirmed": true
}
```

**Response esperado `200`:**

```json
{
  "citation": {
    "id": 101,
    "title": "Citacion virtual con tutoria",
    "detail": "Revision de avance academico y acuerdos de apoyo en casa.",
    "teacherName": "Ana Gomez",
    "scheduledAt": "2026-06-25T17:00:00",
    "status": "confirmed",
    "mode": "virtual",
    "meetingUrl": "https://meet.learnix.edu/cita-101"
  },
  "confirmedAt": "2026-06-17T15:20:00"
}
```

**Codigos de estado posibles:** `200`, `400`, `401`, `403`, `404`, `409`, `500`.

**Observaciones tecnicas:** recomendado permitir confirmacion despues de `accepted`. Si se interpreta como confirmacion de recepcion, tambien puede permitirse desde `pending`, dejando trazabilidad en eventos.

**Pantalla/componente movil:** boton `Confirmar` de `CitationsScreen`.

## 5. Listar mensajes de una citacion

**Nombre:** Listar mensajes de citacion  
**Metodo HTTP:** `GET`  
**Ruta sugerida:** `/api/mobile/citations/{citationId}/messages`

**Descripcion funcional:** obtiene el hilo de comunicacion bidireccional asociado a una citacion.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `citationId` | path | int | Si |
| `after` | query | datetime | No |

**Request esperado:**

```http
GET /api/mobile/citations/101/messages
Authorization: Bearer <token>
```

**Response esperado `200`:**

```json
{
  "items": [
    {
      "id": 1,
      "citationId": 101,
      "senderName": "Ana Gomez",
      "senderRole": "Docente",
      "body": "Buenas tardes, solicito una reunion para revisar el avance.",
      "sentAt": "2026-06-17T10:30:00",
      "isFromParent": false,
      "isRead": true
    }
  ]
}
```

**Codigos de estado posibles:** `200`, `401`, `403`, `404`, `500`.

**Observaciones tecnicas:** el campo `after` permite sincronizacion incremental. Debe marcar lectura en una transaccion separada o mediante endpoint dedicado si se requiere control estricto.

**Pantalla/componente movil:** bloque `Comunicacion de la citacion` en `CitationsScreen`.

## 6. Enviar mensaje en una citacion

**Nombre:** Enviar mensaje de citacion  
**Metodo HTTP:** `POST`  
**Ruta sugerida:** `/api/mobile/citations/{citationId}/messages`

**Descripcion funcional:** permite que el padre responda al docente o coordinador dentro del contexto de la citacion.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `citationId` | path | int | Si |
| `body` | body | string | Si |

**Request esperado:**

```json
{
  "body": "Confirmo mi disponibilidad para la hora indicada."
}
```

**Response esperado `201`:**

```json
{
  "message": {
    "id": 2,
    "citationId": 101,
    "senderName": "Padre de familia",
    "senderRole": "Padre",
    "body": "Confirmo mi disponibilidad para la hora indicada.",
    "sentAt": "2026-06-17T15:22:00",
    "isFromParent": true,
    "isRead": false
  }
}
```

**Codigos de estado posibles:** `201`, `400`, `401`, `403`, `404`, `409`, `422`, `500`.

**Observaciones tecnicas:** validar longitud maxima y sanitizar contenido. Si la citacion esta `cancelled`, responder `409`.

**Pantalla/componente movil:** campo de texto y boton enviar en `CitationsScreen`.

## 7. Trazabilidad de eventos

**Nombre:** Eventos de citacion  
**Metodo HTTP:** `GET`  
**Ruta sugerida:** `/api/mobile/citations/{citationId}/events`

**Descripcion funcional:** obtiene historial de creacion, envio, lectura, respuesta, confirmacion, reprogramacion y mensajes.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `citationId` | path | int | Si |

**Request esperado:**

```http
GET /api/mobile/citations/101/events
Authorization: Bearer <token>
```

**Response esperado `200`:**

```json
{
  "items": [
    {
      "id": 9001,
      "eventType": "accepted",
      "actorName": "Padre de familia",
      "actorRole": "Padre",
      "createdAt": "2026-06-17T15:20:00",
      "payload": {
        "previousStatus": "pending",
        "newStatus": "accepted"
      }
    }
  ]
}
```

**Codigos de estado posibles:** `200`, `401`, `403`, `404`, `500`.

**Observaciones tecnicas:** este endpoint no es obligatorio para la primera renderizacion, pero deja preparado el flujo trazable y sincronizado solicitado por PMV3.

**Pantalla/componente movil:** futura seccion de auditoria/detalle en `CitationsScreen`.

## 8. Citaciones en recordatorios moviles

**Nombre:** Recordatorios con citaciones proximas  
**Metodo HTTP:** `GET`  
**Ruta sugerida:** `/api/mobile/parents/{parentId}/students/{studentId}/reminders`

**Descripcion funcional:** ya usado por la app para el icono superior derecho. Para PMV3 debe incluir citaciones entre 7 y 11 dias.

**Parametros requeridos:**

| Parametro | Ubicacion | Tipo | Requerido |
| --- | --- | --- | --- |
| `parentId` | path | int | Si |
| `studentId` | path | int | Si |
| `today` | query | date | No |

**Request esperado:**

```http
GET /api/mobile/parents/1/students/1/reminders?today=2026-06-17
Authorization: Bearer <token>
```

**Response esperado `200`:**

```json
{
  "count": 1,
  "items": [
    {
      "id": 101,
      "type": "citation",
      "title": "Citacion virtual con tutoria",
      "detail": "Programada para el 2026-06-25 17:00.",
      "date": "2026-06-25T17:00:00",
      "severity": "info"
    }
  ]
}
```

**Codigos de estado posibles:** `200`, `401`, `403`, `404`, `500`.

**Observaciones tecnicas:** no incluir incidencias en recordatorios; las incidencias viven en Perfil.

**Pantalla/componente movil:** icono de notificaciones del `AppBar`.

## Tablas requeridas o propuestas

Estas tablas se proponen si no existen en la base. No se aplican migraciones en este cambio porque el alcance indica no modificar `web-app`.

## `virtual_citations`

Finalidad: guardar la citacion creada por docente/colegio.

| Campo | Tipo sugerido | Clave | Descripcion |
| --- | --- | --- | --- |
| `id_citation` | INT AUTO_INCREMENT | PK | Identificador |
| `id_student` | INT NULL | FK `students.id_student` | Estudiante para citacion individual |
| `id_course_period` | INT NULL | FK `course_periods.id_course_period` | Seccion/curso para citacion masiva |
| `id_teacher` | INT NULL | FK `users.id_user` | Docente responsable |
| `created_by_user_id` | INT NOT NULL | FK `users.id_user` | Usuario que crea |
| `title` | VARCHAR(150) |  | Titulo |
| `detail` | TEXT |  | Motivo |
| `scheduled_at` | DATETIME |  | Fecha/hora |
| `mode` | VARCHAR(20) |  | `virtual` o `in_person` |
| `meeting_url` | VARCHAR(500) NULL |  | Enlace virtual |
| `scope` | VARCHAR(20) |  | `individual`, `section`, `mass` |
| `status` | VARCHAR(20) |  | `pending`, `cancelled`, `completed` |
| `created_at` | TIMESTAMP |  | Creacion |
| `updated_at` | TIMESTAMP |  | Actualizacion |

Relacion: una citacion tiene muchos receptores, mensajes y eventos.

## `citation_recipients`

Finalidad: guardar el estado de cada padre destinatario.

| Campo | Tipo sugerido | Clave | Descripcion |
| --- | --- | --- | --- |
| `id_recipient` | INT AUTO_INCREMENT | PK | Identificador |
| `id_citation` | INT NOT NULL | FK `virtual_citations.id_citation` | Citacion |
| `id_parent` | INT NOT NULL | FK `parents.id_parent` | Padre receptor |
| `id_student` | INT NOT NULL | FK `students.id_student` | Estudiante asociado |
| `recipient_status` | VARCHAR(20) |  | `pending`, `accepted`, `rejected`, `confirmed` |
| `response_reason` | TEXT NULL |  | Motivo de rechazo |
| `read_at` | DATETIME NULL |  | Lectura |
| `responded_at` | DATETIME NULL |  | Aceptacion/rechazo |
| `confirmed_at` | DATETIME NULL |  | Confirmacion |

Relacion: resuelve citaciones masivas por padre/estudiante y permite trazabilidad por destinatario.

## `citation_messages`

Finalidad: soportar comunicacion bidireccional dentro de cada citacion.

| Campo | Tipo sugerido | Clave | Descripcion |
| --- | --- | --- | --- |
| `id_message` | INT AUTO_INCREMENT | PK | Identificador |
| `id_citation` | INT NOT NULL | FK `virtual_citations.id_citation` | Citacion |
| `sender_type` | VARCHAR(20) |  | `parent`, `user`, `system` |
| `sender_id` | INT NOT NULL |  | ID del padre o usuario |
| `body` | TEXT NOT NULL |  | Mensaje |
| `sent_at` | DATETIME |  | Envio |
| `read_at` | DATETIME NULL |  | Lectura del receptor |

Relacion: pertenece a una citacion y se renderiza en el hilo de `CitationsScreen`.

## `citation_events`

Finalidad: registrar auditoria y sincronizacion del flujo.

| Campo | Tipo sugerido | Clave | Descripcion |
| --- | --- | --- | --- |
| `id_event` | INT AUTO_INCREMENT | PK | Identificador |
| `id_citation` | INT NOT NULL | FK `virtual_citations.id_citation` | Citacion |
| `actor_type` | VARCHAR(20) |  | `parent`, `user`, `system` |
| `actor_id` | INT NOT NULL |  | Actor |
| `event_type` | VARCHAR(40) |  | `created`, `sent`, `read`, `accepted`, `rejected`, `confirmed`, `message_sent`, `cancelled` |
| `payload` | JSON NULL |  | Datos adicionales |
| `created_at` | DATETIME |  | Fecha |

Relacion: permite reconstruir el estado y auditar RF13-RF17.

## `teacher_availability_slots`

Finalidad: guardar disponibilidad docente para programacion de citas.

| Campo | Tipo sugerido | Clave | Descripcion |
| --- | --- | --- | --- |
| `id_slot` | INT AUTO_INCREMENT | PK | Identificador |
| `id_teacher` | INT NOT NULL | FK `users.id_user` | Docente |
| `starts_at` | DATETIME |  | Inicio |
| `ends_at` | DATETIME |  | Fin |
| `status` | VARCHAR(20) |  | `available`, `reserved`, `blocked` |
| `created_at` | TIMESTAMP |  | Creacion |

Relacion: usado por backend/docente para RF14. La app movil de padres no lo renderiza directamente en esta version.

## Resumen de validacion movil

| Punto PMV3 | Estado en movil |
| --- | --- |
| Ver citaciones | Preparado en `CitationsScreen` |
| Aceptar/rechazar | Preparado con `respondToCitation` |
| Confirmar citacion | Preparado con `confirmCitation` |
| Comunicacion bidireccional | Preparado con `loadCitationMessages` y `sendCitationMessage` |
| Trazabilidad | Contrato y tabla propuesta (`citation_events`) |
| Citaciones masivas | Contrato cubierto por `scope` y `citation_recipients` |
| Web-app | Sin cambios requeridos ni aplicados |
