# Contratos API - App Movil Padres

Este documento define los endpoints necesarios para obtener los datos que renderiza `movil-app`.
Se omite la funcionalidad de chat bidireccional por ahora.

## Convenciones

- Base path sugerido: `/api/mobile`
- Autenticacion sugerida: `Authorization: Bearer <token>`
- Fechas: ISO 8601 (`YYYY-MM-DD` o `YYYY-MM-DDTHH:mm:ss`)
- Estados normalizados:
  - `academicStatus`: `excellent`, `good`, `atRisk`
  - `attendanceStatus`: `attended`, `absent`, `late`
  - `alertStatus`: `new`, `read`, `archived`
  - `alertSeverity`: `info`, `warning`, `critical`
  - `alertCategory`: `activityDue`, `citation`, `incident`
  - `reportFormat`: `pdf`, `excel`

## Tablas Base Existentes

La app usa datos que ya existen en la base de `web-app`:

- `students`
- `users`
- `periods`
- `courses`
- `course_periods`
- `enrollments`
- `attendances`
- `activities`
- `grades`

## Tablas Requeridas Adicionales

Estas tablas ya estan propuestas en `web-app/src/main/resources/db/migration/V14__create_parent_mobile_tables.sql`.
Para este alcance, las tablas de chat (`conversations`, `messages`, `message_attachments`) no son necesarias.

### `parents`

```sql
CREATE TABLE parents (
    id_parent INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(30),
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### `parent_students`

```sql
CREATE TABLE parent_students (
    id_parent INT NOT NULL,
    id_student INT NOT NULL,
    relationship VARCHAR(40) NOT NULL DEFAULT 'Apoderado',
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_parent, id_student),
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE
);
```

### `parent_alerts`

Usada para citaciones e incidencias. Las actividades proximas tambien pueden calcularse desde `activities.due_date`, pero si se quiere persistirlas como alertas, usar `category = activityDue`.

```sql
CREATE TABLE parent_alerts (
    id_alert INT AUTO_INCREMENT PRIMARY KEY,
    id_student INT NOT NULL,
    id_parent INT,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(150) NOT NULL,
    detail TEXT,
    severity VARCHAR(20) NOT NULL DEFAULT 'info',
    status VARCHAR(20) NOT NULL DEFAULT 'new',
    source_table VARCHAR(80),
    source_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    archived_at TIMESTAMP NULL,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE SET NULL
);
```

Campos esperados por la app:

| Campo app | Campo tabla sugerido |
| --- | --- |
| `category` | `type` (`activityDue`, `citation`, `incident`) |
| `title` | `title` |
| `detail` | `detail` |
| `date` | `created_at` para incidencias, o fecha objetivo desde `source_table/source_id` para citaciones |
| `status` | `status` |
| `severity` | `severity` |

### `announcements`

```sql
CREATE TABLE announcements (
    id_announcement INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    sender_name VARCHAR(150) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'normal',
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### `announcement_recipients`

```sql
CREATE TABLE announcement_recipients (
    id_announcement INT NOT NULL,
    id_parent INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'new',
    read_at TIMESTAMP NULL,
    PRIMARY KEY (id_announcement, id_parent),
    FOREIGN KEY (id_announcement) REFERENCES announcements(id_announcement) ON DELETE CASCADE,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE
);
```

### `academic_reports`

```sql
CREATE TABLE academic_reports (
    id_report INT AUTO_INCREMENT PRIMARY KEY,
    id_student INT NOT NULL,
    report_type VARCHAR(60) NOT NULL,
    title VARCHAR(150) NOT NULL,
    format VARCHAR(20) NOT NULL,
    file_url VARCHAR(500),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE
);
```

Recomendacion: si un reporte tiene PDF y Excel, puede haber una fila por formato o una tabla hija `academic_report_files`.

## Endpoints

## 1. Login Provisional

Autentica al padre y devuelve el estudiante principal.

**Metodo:** `POST`  
**Endpoint:** `/api/mobile/auth/login`

### Request

```json
{
  "email": "padre@learnix.com",
  "password": "123456"
}
```

### Response 200

```json
{
  "token": "jwt-token",
  "parent": {
    "id": 1,
    "firstName": "Carlos",
    "lastName": "Perez",
    "email": "padre@learnix.com"
  },
  "students": [
    {
      "id": 1,
      "fullName": "Juan Perez",
      "gradeSection": "A",
      "isPrimary": true
    }
  ]
}
```

### Tablas

- Requiere `parents`.
- Requiere `parent_students`.
- Usa `students`, `enrollments` y `course_periods` para resolver la seccion matriculada.

## 2. Dashboard Inicial

Devuelve los datos agregados que renderiza Inicio: resumen, promedios por curso, actividades, asistencia y reportes.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/parents/{parentId}/students/{studentId}/dashboard`

### Request

Path params:

```json
{
  "parentId": 1,
  "studentId": 1
}
```

### Response 200

```json
{
  "student": {
    "id": 1,
    "fullName": "Juan Perez",
    "gradeSection": "A",
    "generalAverage": 17.0,
    "attendancePercentage": 92.0,
    "academicStatus": "good"
  },
  "courseAverages": [
    {
      "courseId": 1,
      "coursePeriodId": 1,
      "courseName": "Matematica",
      "average": 18.0
    }
  ],
  "activities": [
    {
      "id": 10,
      "name": "Informe de lectura",
      "courseName": "Comunicacion",
      "term": "B4",
      "date": "2026-06-02",
      "grade": "15",
      "status": "Pendiente"
    }
  ],
  "attendanceStats": {
    "attendedDays": 22,
    "absentDays": 2,
    "lateDays": 1,
    "attendancePercentage": 92.0
  },
  "reports": [
    {
      "id": 1,
      "title": "Rendimiento academico",
      "description": "Promedios y progreso por curso.",
      "formats": ["pdf", "excel"]
    }
  ]
}
```

### Tablas

- Usa `students`, `parent_students`.
- Usa `courses`, `course_periods`, `enrollments`.
- Usa `activities`, `grades`.
- Usa `attendances`.
- Usa `academic_reports`.

Nota: `student.gradeSection` debe calcularse desde las matriculas activas del estudiante (`enrollments.status = active`) y las secciones de `course_periods.section`. Formato esperado por la app: `A` o `A, B` si el estudiante aparece en mas de una seccion activa.

Nota: el campo `term` no existe explicitamente en `activities`. Si se requiere persistencia real por bimestre, agregar:

```sql
ALTER TABLE activities ADD COLUMN term VARCHAR(10);
```

Alternativa: calcular el bimestre por `due_date` segun el calendario escolar.

## 3. Progreso Academico con Filtros

Devuelve progreso temporal y actividades filtrables por bimestre y curso.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/students/{studentId}/progress`

### Query Params

| Parametro | Tipo | Requerido | Ejemplo |
| --- | --- | --- | --- |
| `term` | string | No | `B2` |
| `courseId` | int | No | `1` |

### Request

```json
{
  "studentId": 1,
  "term": "B2",
  "courseId": 1
}
```

### Response 200

```json
{
  "termProgress": [
    {
      "term": "B1",
      "average": 15.0
    },
    {
      "term": "B2",
      "average": 16.0
    }
  ],
  "activities": [
    {
      "id": 11,
      "name": "Practica de fracciones",
      "courseId": 1,
      "courseName": "Matematica",
      "term": "B2",
      "date": "2026-05-22",
      "grade": "19",
      "status": "Calificada"
    }
  ],
  "availableFilters": {
    "terms": ["B1", "B2", "B3", "B4"],
    "courses": [
      {
        "id": 1,
        "name": "Matematica"
      }
    ]
  }
}
```

### Tablas

- Usa `activities`, `grades`.
- Usa `courses`, `course_periods`, `enrollments`.
- Requiere resolver `term` por columna nueva o por calendario.

## 4. Detalle de Asistencia

Devuelve el detalle visual de asistencia para Inicio.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/students/{studentId}/attendance`

### Query Params

| Parametro | Tipo | Requerido | Ejemplo |
| --- | --- | --- | --- |
| `from` | date | No | `2026-05-01` |
| `to` | date | No | `2026-05-31` |

### Request

Path params:

```json
{
  "studentId": 1
}
```

Query params:

```json
{
  "from": "2026-05-01",
  "to": "2026-05-31"
}
```

### Response 200

```json
{
  "stats": {
    "attendedDays": 22,
    "absentDays": 2,
    "lateDays": 1,
    "attendancePercentage": 92.0
  },
  "days": [
    {
      "date": "2026-05-01",
      "status": "attended"
    },
    {
      "date": "2026-05-09",
      "status": "absent"
    },
    {
      "date": "2026-05-14",
      "status": "late"
    }
  ]
}
```

### Tablas

- Usa `attendances`.

Nota: `attendances.did_attend` solo distingue asistencia/falta. Para tardanzas se requiere un campo adicional:

```sql
ALTER TABLE attendances ADD COLUMN status VARCHAR(20) DEFAULT 'attended';
```

Valores sugeridos: `attended`, `absent`, `late`.

## 5. Recordatorios del Icono de Notificaciones

Devuelve recordatorios para el recuadro del icono superior derecho.

Reglas:

- Actividades pendientes que vencen entre 1 y 4 dias.
- Citaciones proximas entre 7 y 11 dias.
- No incluir incidencias aqui.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/parents/{parentId}/students/{studentId}/reminders`

### Query Params

| Parametro | Tipo | Requerido | Ejemplo |
| --- | --- | --- | --- |
| `today` | date | No | `2026-05-30` |

### Request

Path params:

```json
{
  "parentId": 1,
  "studentId": 1
}
```

Query params:

```json
{
  "today": "2026-05-30"
}
```

### Response 200

```json
{
  "count": 2,
  "items": [
    {
      "id": 10,
      "type": "activityDue",
      "title": "Informe de lectura",
      "detail": "Comunicacion vence el 2026-06-02 (B4).",
      "date": "2026-06-02",
      "severity": "warning"
    },
    {
      "id": 2,
      "type": "citation",
      "title": "Citacion proxima",
      "detail": "Reunion con tutor el 08/06/2026.",
      "date": "2026-06-08",
      "severity": "info"
    }
  ]
}
```

### Tablas

- Actividades: usa `activities`, `grades`, `enrollments`.
- Citaciones: usa `parent_alerts` con `type = citation`.

Nota: para citaciones con fecha objetiva, se recomienda una tabla especifica:

```sql
CREATE TABLE citations (
    id_citation INT AUTO_INCREMENT PRIMARY KEY,
    id_student INT NOT NULL,
    id_parent INT,
    title VARCHAR(150) NOT NULL,
    detail TEXT,
    scheduled_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE SET NULL
);
```

Si no se crea `citations`, usar `parent_alerts.source_table/source_id` o agregar `event_date`:

```sql
ALTER TABLE parent_alerts ADD COLUMN event_date DATETIME NULL;
```

## 6. Incidencias

Perfil muestra solo resumen de incidencias; al presionar una incidencia se abre una pantalla donde se ven todas con estado.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/parents/{parentId}/students/{studentId}/incidents`

### Query Params

| Parametro | Tipo | Requerido | Ejemplo |
| --- | --- | --- | --- |
| `status` | string | No | `new` |

### Request

Path params:

```json
{
  "parentId": 1,
  "studentId": 1
}
```

Query params:

```json
{
  "status": "new"
}
```

### Response 200

```json
{
  "items": [
    {
      "id": 3,
      "title": "Incidencia disciplinaria",
      "detail": "Observacion registrada en recreo.",
      "date": "2026-05-24",
      "status": "archived",
      "severity": "critical"
    }
  ]
}
```

### Tablas

- Usa `parent_alerts` con `type = incident`.

## 7. Resumen de Perfil

Devuelve los datos necesarios para la pestana lateral de Perfil.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/parents/{parentId}/students/{studentId}/profile-summary`

### Request

Path params:

```json
{
  "parentId": 1,
  "studentId": 1
}
```

### Response 200

```json
{
  "parent": {
    "id": 1,
    "fullName": "Carlos Perez",
    "email": "padre@learnix.com"
  },
  "student": {
    "id": 1,
    "fullName": "Juan Perez",
    "gradeSection": "A"
  },
  "announcementPreview": [
    {
      "id": 1,
      "title": "Reunion de padres",
      "sender": "Direccion",
      "date": "2026-05-30"
    }
  ],
  "incidentPreview": [
    {
      "id": 3,
      "title": "Incidencia disciplinaria",
      "detail": "Observacion registrada en recreo."
    }
  ]
}
```

### Tablas

- Usa `parents`, `parent_students`, `students`.
- Usa `announcements`, `announcement_recipients`.
- Usa `parent_alerts` con `type = incident`.

## 8. Comunicados con Filtros

Pantalla abierta desde `Ver comunicados`.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/parents/{parentId}/announcements`

### Query Params

| Parametro | Tipo | Requerido | Ejemplo |
| --- | --- | --- | --- |
| `priority` | string | No | `Alta` |
| `sender` | string | No | `Direccion` |
| `status` | string | No | `new` |

### Request

Path params:

```json
{
  "parentId": 1
}
```

Query params:

```json
{
  "priority": "Alta",
  "sender": "Direccion",
  "status": "new"
}
```

### Response 200

```json
{
  "filters": {
    "priorities": ["Alta", "Media", "Normal"],
    "senders": ["Direccion", "Coordinacion", "Secretaria Academica"],
    "statuses": ["new", "read"]
  },
  "items": [
    {
      "id": 1,
      "title": "Reunion de padres",
      "body": "Reunion general de padres de familia.",
      "sender": "Direccion",
      "priority": "Alta",
      "date": "2026-05-30",
      "status": "new"
    }
  ]
}
```

### Tablas

- Usa `announcements`.
- Usa `announcement_recipients`.

## 9. Marcar Comunicado como Leido

**Metodo:** `PATCH`  
**Endpoint:** `/api/mobile/parents/{parentId}/announcements/{announcementId}/read`

### Request

Path params:

```json
{
  "parentId": 1,
  "announcementId": 1
}
```

Body:

```json
{
  "read": true
}
```

### Response 200

```json
{
  "id": 1,
  "status": "read",
  "readAt": "2026-05-30T19:30:00"
}
```

### Tablas

- Actualiza `announcement_recipients.status`.
- Actualiza `announcement_recipients.read_at`.

## 10. Reportes Academicos

Se renderizan en Inicio.

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/students/{studentId}/reports`

### Query Params

| Parametro | Tipo | Requerido | Ejemplo |
| --- | --- | --- | --- |
| `type` | string | No | `attendance` |
| `format` | string | No | `pdf` |

### Request

Path params:

```json
{
  "studentId": 1
}
```

Query params:

```json
{
  "type": "attendance",
  "format": "pdf"
}
```

### Response 200

```json
{
  "items": [
    {
      "id": 1,
      "title": "Rendimiento academico",
      "description": "Promedios y progreso por curso.",
      "formats": ["pdf", "excel"],
      "files": [
        {
          "format": "pdf",
          "url": "https://learnix.local/reports/1.pdf"
        },
        {
          "format": "excel",
          "url": "https://learnix.local/reports/1.xlsx"
        }
      ],
      "generatedAt": "2026-05-30T19:30:00"
    }
  ]
}
```

### Tablas

- Usa `academic_reports`.

Si `academic_reports` mantiene una fila por formato, el backend debe agrupar por `report_type/title`.

## 11. Descargar Reporte

**Metodo:** `GET`  
**Endpoint:** `/api/mobile/students/{studentId}/reports/{reportId}/download`

### Query Params

| Parametro | Tipo | Requerido | Ejemplo |
| --- | --- | --- | --- |
| `format` | string | Si | `pdf` |

### Request

Path params:

```json
{
  "studentId": 1,
  "reportId": 1
}
```

Query params:

```json
{
  "format": "pdf"
}
```

### Response 200

Binario del archivo con headers:

```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="rendimiento-academico.pdf"
```

Para Excel:

```http
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="rendimiento-academico.xlsx"
```

### Tablas

- Usa `academic_reports.file_url` o genera el archivo al vuelo desde `grades`, `activities`, `attendances`.

## 12. Preferencias de Configuracion

El modo oscuro actualmente puede ser local en la app. Si se desea persistirlo en backend:

**Metodo:** `PATCH`  
**Endpoint:** `/api/mobile/parents/{parentId}/preferences`

### Request

Path params:

```json
{
  "parentId": 1
}
```

Body:

```json
{
  "darkMode": true
}
```

### Response 200

```json
{
  "parentId": 1,
  "darkMode": true,
  "updatedAt": "2026-05-30T19:30:00"
}
```

### Tabla Requerida si se persiste

```sql
CREATE TABLE parent_preferences (
    id_parent INT PRIMARY KEY,
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_parent) REFERENCES parents(id_parent) ON DELETE CASCADE
);
```

## Resumen de Endpoints

| Metodo | Endpoint | Uso en app |
| --- | --- | --- |
| `POST` | `/api/mobile/auth/login` | Login provisional |
| `GET` | `/api/mobile/parents/{parentId}/students/{studentId}/dashboard` | Inicio |
| `GET` | `/api/mobile/students/{studentId}/progress` | Progreso con filtros |
| `GET` | `/api/mobile/students/{studentId}/attendance` | Detalle de asistencia |
| `GET` | `/api/mobile/parents/{parentId}/students/{studentId}/reminders` | Icono de notificaciones |
| `GET` | `/api/mobile/parents/{parentId}/students/{studentId}/incidents` | Pantalla de incidencias |
| `GET` | `/api/mobile/parents/{parentId}/students/{studentId}/profile-summary` | Pestaña lateral Perfil |
| `GET` | `/api/mobile/parents/{parentId}/announcements` | Comunicados con filtros |
| `PATCH` | `/api/mobile/parents/{parentId}/announcements/{announcementId}/read` | Marcar comunicado leido |
| `GET` | `/api/mobile/students/{studentId}/reports` | Reportes en Inicio |
| `GET` | `/api/mobile/students/{studentId}/reports/{reportId}/download` | Descargar reporte |
| `PATCH` | `/api/mobile/parents/{parentId}/preferences` | Persistir modo oscuro opcional |
