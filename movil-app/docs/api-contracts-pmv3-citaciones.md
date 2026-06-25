# Contratos API - PMV3 Modulo de Citaciones Virtuales

Este documento valida y documenta los contratos necesarios para que `movil-app` renderice el modulo PMV3 conectado a `web-app`.

## Alcance PMV3

Requerimientos revisados del documento actualizado:

- `RF13` Comunicacion bidireccional: padre y docente deben poder intercambiar mensajes trazables.
- `RF14` Agendar citas: el docente programa fecha, motivo y modalidad.
- `RF15` Aceptar/rechazar citas: el padre gestiona la invitacion desde la app movil.
- `RF16` Enviar citaciones masivas: el colegio envia citaciones a una seccion o grupo.
- `RF17` Confirmar citaciones: el padre confirma recepcion y/o asistencia.

La app movil quedo preparada con `Citation`, `CitationMessage`, `CitationController`, `CitationsScreen` y metodos de `ParentRepository` para consumir estos endpoints cuando el backend este disponible.

## Correcciones de consistencia revisadas en movil

- `status` en los responses moviles de citaciones representa el estado del receptor padre-estudiante (`pending`, `accepted`, `rejected`, `confirmed`, `cancelled`), no solo el estado global de la citacion.
- Los endpoints sin `parentId` en la ruta deben resolver el padre autenticado desde el token y validar que el receptor pertenezca a la citacion.
- La confirmacion movil envia un body explicito `{ "confirmed": true }`.
- La app conserva `parentId = 1` como fallback provisional para el login mock; en backend real debe reemplazarse por el `parent.id` retornado por autenticacion o validarse desde el token.
- La pantalla `CitationsScreen` ya contempla carga, seleccion, respuesta, confirmacion y mensajeria bidireccional. El controlador evita notificaciones de estado despues de cerrar la vista.
- La vista web docente debe leer `virtual_citations.global_status`; la app movil de padres debe leer `citation_recipients.recipient_status`. No deben reutilizar el mismo significado de `status`.
- En la vista web docente, los mensajes con `sender_type = parent` se consideran mensajes entrantes del padre; en la app movil, `isFromParent` se calcula contra el padre autenticado.
- Las justificaciones de rechazo se almacenan en `citation_recipients.response_reason` y se revisan con `justification_status`: `pending_review`, `justified`, `not_justified`.
- Los mensajes del profesor o automaticos pueden dirigirse a un padre especifico mediante `citation_messages.target_id_parent`, evitando que una citacion masiva filtre conversaciones entre familias.

## Convenciones

- Base path sugerido: `/api/mobile`
- URL base en Flutter:
  - Por defecto en Flutter Web/desktop: `http://localhost:8080`
  - Por defecto en Android emulator: `http://10.0.2.2:8080`
  - Se puede sobrescribir con `--dart-define=LEARNIX_API_BASE_URL=<url>`
- El backend `web-app` debe permitir CORS para `/api/mobile/**` cuando se pruebe desde Flutter Web o desde un cliente móvil en desarrollo.
- Autenticacion: `Authorization: Bearer <token>`
- Fechas: ISO 8601 (`YYYY-MM-DDTHH:mm:ss`)
- Estados moviles de receptor: `pending`, `accepted`, `rejected`, `confirmed`, `cancelled`
- Estados globales de citacion: `scheduled`, `cancelled`, `completed`
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
El campo `status` debe salir de `citation_recipients.recipient_status`.

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
Si tambien se requiere el estado institucional de la citacion, devolver un campo adicional `globalStatus`.

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

Para `status = rejected`, `reason` es obligatorio. La app movil no debe permitir enviar rechazo con el campo vacio.

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
El backend debe validar que el usuario autenticado corresponda al receptor registrado en `citation_recipients`.
Cuando `status = rejected`, guardar `response_reason` y establecer `justification_status = pending_review`.

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
La app movil actual habilita el boton `Confirmar` cuando el receptor esta en `accepted`.

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

**Observaciones tecnicas:** el campo `after` permite sincronizacion incremental. Los mensajes del docente con `target_id_parent` solo deben mostrarse al padre destinatario.
El backend debe calcular `isFromParent` comparando el remitente con el padre autenticado.

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
El sender no debe recibirse desde el cliente; debe derivarse del token.

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

## 9. Revision web de justificacion

**Nombre:** Revisar justificacion de rechazo
**Metodo HTTP:** `POST`
**Ruta web:** `/citations/{citationId}/recipients/{recipientId}/justification`

**Descripcion funcional:** permite que el profesor marque la razon de rechazo enviada por el padre como justificada o no justificada.

**Request esperado:**

```http
POST /citations/101/recipients/33/justification
Content-Type: application/x-www-form-urlencoded

status=not_justified&parentId=1
```

**Valores permitidos:** `justified`, `not_justified`.

**Efecto esperado:**

- Actualiza `citation_recipients.justification_status`.
- Registra `citation_recipients.justification_reviewed_at`.
- Si el estado es `not_justified`, inserta automaticamente un mensaje docente dirigido al padre mediante `citation_messages.target_id_parent`.

**Pantalla/componente web:** detalle de citacion, bloque `Resumen de respuestas`.

## 10. Comunicacion web por contacto

**Nombre:** Conversacion docente-padre por citacion
**Metodo HTTP:** `GET`
**Ruta web:** `/citations/{citationId}?parentId={parentId}`

**Descripcion funcional:** muestra al profesor la lista de padres/contactos y la conversacion filtrada del padre seleccionado.

**Datos renderizados:**

- Nombre del padre.
- Estudiante asociado.
- Estado de la ultima citacion.
- Ultimo mensaje.
- Cantidad de mensajes de padre sin leer.

**Pantalla/componente web:** detalle de citacion, bloque `Comunicacion bidireccional`.

## Tablas requeridas o propuestas

Estas tablas existen o quedan completadas por las migraciones de `web-app` para PMV3.

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
| `global_status` | VARCHAR(20) |  | `scheduled`, `cancelled`, `completed` |
| `created_at` | TIMESTAMP |  | Creacion |
| `updated_at` | TIMESTAMP |  | Actualizacion |

Relacion: una citacion tiene muchos receptores, mensajes y eventos.
El estado que renderiza la app (`status`) debe calcularse desde `citation_recipients.recipient_status`.

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
| `justification_status` | VARCHAR(20) NULL |  | `pending_review`, `justified`, `not_justified` |
| `justification_reviewed_at` | DATETIME NULL |  | Fecha de revision docente |
| `read_at` | DATETIME NULL |  | Lectura |
| `responded_at` | DATETIME NULL |  | Aceptacion/rechazo |
| `confirmed_at` | DATETIME NULL |  | Confirmacion |

Relacion: resuelve citaciones masivas por padre/estudiante y permite trazabilidad por destinatario.
Se recomienda una restriccion unica sobre (`id_citation`, `id_parent`, `id_student`) para evitar invitaciones duplicadas.

## `citation_messages`

Finalidad: soportar comunicacion bidireccional dentro de cada citacion.

| Campo | Tipo sugerido | Clave | Descripcion |
| --- | --- | --- | --- |
| `id_message` | INT AUTO_INCREMENT | PK | Identificador |
| `id_citation` | INT NOT NULL | FK `virtual_citations.id_citation` | Citacion |
| `sender_type` | VARCHAR(20) |  | `parent`, `user`, `system` |
| `sender_id` | INT NOT NULL |  | ID del padre o usuario |
| `target_id_parent` | INT NULL | FK `parents.id_parent` | Padre destinatario cuando el mensaje docente no es masivo |
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
| Resumen web docente | Implementado con conteo de aceptados, rechazados y pendientes |
| Revision de justificaciones | Implementada con `justification_status` y mensaje automatico si no procede |
| Trazabilidad | Conservada como soporte tecnico en `citation_events`, ya no como seccion principal |
| Citaciones masivas | Contrato cubierto por `scope` y `citation_recipients` |
| Web-app | Conectado a procedimientos y tablas reales de PMV3 |
