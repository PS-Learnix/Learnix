# SECURITY_OWASP.md

## Alcance

Este documento registra la implementacion inicial de contramedidas QWASP/OWASP A01-A05 en Learnix. Los cambios se aplican principalmente en `web-app` y no modifican pantallas ni flujo funcional de `movil-app`.

## Contramedidas aplicadas

### A01 Broken Access Control

**Aplicado:**

- Se agrego `AccessControlService` para validar:
  - token Bearer movil;
  - coincidencia entre `parentId` de la ruta y `parentId` autenticado;
  - relacion padre-estudiante;
  - relacion padre-comunicado;
  - relacion padre-citacion.
- Se reforzaron endpoints `/api/mobile/**` en `MobileParentController`.

**Archivos modificados/creados:**

- `web-app/src/main/java/com/learnix/web/security/AccessControlService.java`
- `web-app/src/main/java/com/learnix/web/controller/rest/MobileParentController.java`
- `web-app/src/main/java/com/learnix/web/config/MobileAuthInterceptor.java`

**Justificacion tecnica:**

Evita que un usuario manipule IDs desde la URL para acceder a estudiantes, citaciones o comunicados de otro padre.

## A02 Security Misconfiguration

**Aplicado:**

- CORS configurable mediante `LEARNIX_ALLOWED_ORIGINS`.
- Swagger/OpenAPI desactivable por ambiente con `SPRINGDOC_ENABLED`.
- Stack traces y mensajes internos deshabilitados en respuestas de error.
- Salida SQL por consola deshabilitada con `spring.jpa.show-sql: false`.
- Cabeceras de seguridad agregadas:
  - `X-Content-Type-Options`
  - `X-Frame-Options`
  - `Referrer-Policy`
  - `Permissions-Policy`
- Manejo REST centralizado de errores.
- Tokens moviles con formato interno invalido se rechazan como `401`.
- JSON invalido, parametros faltantes, tipos incorrectos y fechas mal formadas se responden como `400`.

**Archivos modificados/creados:**

- `web-app/src/main/java/com/learnix/web/config/WebMvcConfig.java`
- `web-app/src/main/java/com/learnix/web/config/MobileAuthInterceptor.java`
- `web-app/src/main/java/com/learnix/web/security/SecurityRestExceptionHandler.java`
- `web-app/src/main/resources/application.yaml`

**Justificacion tecnica:**

Reduce exposicion accidental de informacion sensible y evita configuraciones abiertas en despliegues productivos.

## A03 Software Supply Chain Failures

**Aplicado:**

- Se agrego una evidencia automatica que comprueba la existencia de archivos de dependencias:
  - `pom.xml`
  - `pubspec.lock`
- Se documento la recomendacion de auditoria periodica.
- El panel ejecuta una comprobacion local para marcar la evidencia como `APROBADO` o `FALLIDO`.

**Archivos modificados/creados:**

- `web-app/src/main/java/com/learnix/web/security/SecurityEvidenceService.java`
- `web-app/src/main/java/com/learnix/web/security/SecurityEvidence.java`
- `web-app/src/main/java/com/learnix/web/controller/SecurityEvidenceController.java`
- `web-app/src/main/resources/templates/security_evidences.html`

**Justificacion tecnica:**

Permite evidenciar que el proyecto mantiene dependencias declaradas y bloqueadas, y deja un punto visible para seguimiento.

## A04 Cryptographic Failures

**Aplicado:**

- Se elimino el log que imprimia tokens moviles completos.
- Se evita imprimir passwords.
- El correo se registra enmascarado.
- Se redujo logging SQL/bind para evitar exposicion de datos sensibles.

**Archivos modificados/creados:**

- `web-app/src/main/java/com/learnix/web/controller/rest/MobileAuthController.java`
- `web-app/src/main/java/com/learnix/web/controller/AuthController.java`
- `web-app/src/main/resources/application.yaml`

**Justificacion tecnica:**

Evita que tokens, passwords u otros datos sensibles queden expuestos en consola o logs.

## A05 Injection

**Aplicado:**

- Se agrego `SecurityInputValidator`.
- Se validan IDs positivos.
- Se validan listas cerradas para estados, prioridades y formatos.
- Se bloquean patrones basicos de inyeccion HTML/SQL en parametros y mensajes.
- Se mantiene el uso de consultas parametrizadas y procedimientos almacenados.

**Archivos modificados/creados:**

- `web-app/src/main/java/com/learnix/web/security/SecurityInputValidator.java`
- `web-app/src/main/java/com/learnix/web/controller/rest/MobileParentController.java`
- `web-app/src/main/java/com/learnix/web/controller/rest/MobileAuthController.java`

**Justificacion tecnica:**

Reduce el riesgo de entradas maliciosas o inesperadas antes de llegar a consultas, procedimientos o vistas.

## Evidencias generadas

Se agrego una seccion web:

- Vista HTML: `/security/evidences`
- Reporte JSON: `/security/evidences.json`

Cada evidencia muestra:

- contramedida aplicada;
- prueba realizada;
- resultado obtenido;
- estado;
- fecha y hora;
- modulo evaluado;
- observaciones.

## Pruebas ejecutadas y verificables

Pruebas funcionales esperadas para validar las contramedidas desde cliente HTTP:

- Acceder a `/api/mobile/**` sin token: debe retornar `401`.
- Usar token de otro padre contra `parentId` diferente: debe retornar `403`.
- Usar `studentId` no relacionado con el padre: debe retornar `403`.
- Enviar filtros o mensajes con patrones maliciosos: debe retornar `400`.
- Provocar error REST: debe responder JSON controlado sin stack trace.
- Revisar `/security/evidences` con sesion web iniciada.
- Revisar `/security/evidences.json` con sesion web iniciada.

Pruebas automaticas ligeras ejecutadas por el panel de evidencias:

- Token ausente e invalido rechazados.
- Token movil valido parseado.
- CORS, cabeceras, ocultamiento de stack trace y salida SQL desactivada revisados desde configuracion.
- Archivos `pom.xml` y `pubspec.lock` verificados.
- Controladores escaneados para evitar `System.out`, `printStackTrace` y exposicion directa de mensajes SQL.
- Payloads HTML/SQL sospechosos y estados no permitidos rechazados.

Validacion local intentada:

- `.\mvnw.cmd -q -DskipTests compile`: no ejecutado por fallo del wrapper antes de iniciar Maven (`Cannot start maven from wrapper`).
- `mvn -q -DskipTests compile`: no ejecutado porque `mvn` no esta instalado en el entorno.

## Evidencias visuales

El panel `/security/evidences` funciona como evidencia navegable dentro del sistema. Tambien puede usarse `/security/evidences.json` para adjuntar resultados en informes.

Las evidencias no son solo descriptivas: `SecurityEvidenceService` ejecuta comprobaciones ligeras al renderizar el panel:

- A01: valida rechazo de token ausente, rechazo de token invalido y parseo de token movil valido.
- A02: revisa configuracion de CORS, cabeceras de seguridad y ocultamiento de stack trace.
- A03: comprueba existencia de archivos de dependencias declaradas/bloqueadas.
- A04: escanea controladores para evitar `System.out`, `printStackTrace` y exposicion directa de mensajes SQL.
- A05: prueba rechazo de payload HTML, payload SQL sospechoso y estados no permitidos.

## Recomendaciones pendientes

- Sustituir tokens moviles provisionales por JWT firmado con expiracion.
- Agregar hashing fuerte para passwords si aun se usan credenciales en texto plano en datos semilla.
- Integrar analisis automatico de dependencias en CI.
- Agregar pruebas automatizadas backend cuando el entorno tenga `JAVA_HOME` configurado.
- Revisar politicas de cookies `Secure`, `HttpOnly` y `SameSite` en despliegue final.
- Crear alertas operativas para multiples fallos de login y accesos denegados.
