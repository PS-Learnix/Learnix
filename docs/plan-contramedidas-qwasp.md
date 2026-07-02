# Plan de contramedidas QWASP/OWASP para Learnix

Este documento define un plan de implementacion para mitigar riesgos de seguridad en Learnix, tomando como guia las categorias indicadas por QWASP/OWASP. El alcance considera:

- `web-app`: aplicacion Spring Boot con Thymeleaf, controladores web, controladores REST moviles, repositorios JDBC y migraciones Flyway.
- `movil-app`: aplicacion Flutter para padres.
- Base de datos: tablas academicas, tablas moviles, citaciones PMV3 y procedimientos almacenados.

El objetivo no es marcar todos los puntos como completados, sino establecer como verificar si existen, que contramedidas aplicar y como probar que funcionan.

## 1. A01 Broken Access Control

### Pregunta guia

¿Se tiene implementado el A01 Broken Access Control?

### Riesgo

Un usuario podria acceder a datos o acciones que no le corresponden, por ejemplo:

- Un padre consultando informacion de un estudiante que no esta asociado a su cuenta.
- Un docente accediendo a citaciones o registros de otro docente.
- Un usuario no autenticado ingresando a rutas internas del panel web.
- Manipulacion de `parentId`, `studentId`, `citationId` o `coursePeriodId` desde URL o request.

### Contramedidas propuestas

1. Centralizar validaciones de autorizacion.
   - En `web-app`, crear servicios de autorizacion como `AccessControlService`.
   - Validar relaciones antes de devolver datos:
     - padre-estudiante;
     - docente-curso;
     - docente-citacion;
     - usuario-rol-ruta.

2. No confiar en IDs enviados por el cliente.
   - En endpoints moviles, derivar `parentId` desde el token o sesion.
   - Si temporalmente se usa `parentId` por URL, validar siempre contra la identidad autenticada.

3. Proteger rutas web.
   - Verificar sesion en todos los controladores.
   - Redirigir a login si no hay usuario.
   - Bloquear rutas por rol cuando aplique.

4. Validar acceso a objetos especificos.
   - Antes de consultar una citacion, validar que el docente sea propietario.
   - Antes de consultar datos moviles, validar que el padre tenga relacion con el estudiante.

### Implementacion sugerida

- Crear metodos como:
  - `canParentAccessStudent(parentId, studentId)`.
  - `canTeacherAccessCitation(teacherId, citationId)`.
  - `canTeacherAccessCoursePeriod(teacherId, coursePeriodId)`.
- Usarlos en `MobileService`, `WebCitationService` y controladores web.
- Agregar respuestas HTTP:
  - `401` si no hay autenticacion.
  - `403` si el usuario no tiene permiso.
  - `404` si el recurso no existe o no debe exponerse.

### Pruebas

- Intentar consultar `/api/mobile/parents/1/students/999/dashboard`.
- Intentar abrir una citacion creada por otro docente.
- Intentar acceder a rutas web sin sesion.
- Confirmar que el sistema retorna `403` o redirecciona a login.

## 2. A02 Security Misconfiguration

### Pregunta guia

¿Se tiene implementado el A02 Security Misconfiguration?

### Riesgo

Configuraciones inseguras pueden exponer informacion sensible o abrir acceso no deseado:

- CORS demasiado permisivo.
- Errores con stack trace visibles.
- Credenciales en archivos versionados.
- Endpoints de desarrollo habilitados en produccion.
- Configuracion insegura de cookies o sesiones.

### Contramedidas propuestas

1. Configurar perfiles por ambiente.
   - `application-dev.yaml`
   - `application-prod.yaml`
   - variables de entorno para secretos.

2. Endurecer CORS.
   - Permitir solo dominios necesarios.
   - Separar origenes de desarrollo y produccion.

3. Manejar errores sin exponer detalles internos.
   - Crear un `@ControllerAdvice` para REST.
   - Mostrar mensajes controlados al usuario.
   - Registrar detalles tecnicos solo en logs.

4. Proteger cookies y sesiones.
   - `HttpOnly`.
   - `Secure` en produccion.
   - `SameSite=Lax` o `Strict`.
   - Tiempo de expiracion razonable.

5. Revisar archivos de configuracion.
   - No versionar claves, tokens, usuarios ni passwords reales.
   - Usar `.env.example` solo con valores ficticios.

### Implementacion sugerida

- Revisar `application.yaml`.
- Crear perfiles y documentar variables requeridas:
  - `DB_URL`
  - `DB_USER`
  - `DB_PASSWORD`
  - `JWT_SECRET` si se usa token.
- Ajustar `WebMvcConfig` para CORS limitado.
- Agregar `server.error.include-stacktrace=never`.

### Pruebas

- Probar una excepcion controlada y verificar que no muestre stack trace.
- Probar peticiones desde un origen no permitido.
- Revisar que no existan secretos reales en Git.

## 3. A03 Software Supply Chain Failures

### Pregunta guia

¿Se tiene implementado el A03 Software Supply Chain Failures?

### Riesgo

Dependencias vulnerables o no verificadas pueden comprometer el sistema:

- Paquetes Flutter obsoletos.
- Dependencias Maven vulnerables.
- Scripts o librerias estaticas sin control de version.
- Actualizaciones sin revision.

### Contramedidas propuestas

1. Auditar dependencias.
   - Para Flutter: `flutter pub outdated`.
   - Para Maven: `mvn dependency:tree` y herramientas de analisis.

2. Fijar versiones.
   - Mantener `pubspec.lock`.
   - Mantener versiones Maven controladas.

3. Revisar librerias estaticas.
   - `bootstrap`, `htmx` y otros assets deben tener version conocida.

4. Integrar revision periodica.
   - Revision mensual de dependencias.
   - Actualizacion controlada con pruebas.

### Implementacion sugerida

- Agregar una seccion en `README.md` con comandos:
  - `flutter pub outdated`
  - `flutter test`
  - `mvn test`
  - `mvn dependency:tree`
- Activar Dependabot o una herramienta equivalente si el repositorio lo permite.
- Registrar dependencias criticas y version usada.

### Pruebas

- Ejecutar analisis de dependencias.
- Actualizar una dependencia en rama separada.
- Ejecutar pruebas antes de integrar.

## 4. A04 Cryptographic Failures

### Pregunta guia

¿Se tiene implementado el A04 Cryptographic Failures?

### Riesgo

Informacion sensible podria quedar expuesta:

- Passwords en texto plano.
- Tokens debiles.
- Comunicacion sin HTTPS.
- Datos sensibles sin proteccion.
- Claves criptograficas en codigo.

### Contramedidas propuestas

1. Hashear passwords.
   - Usar BCrypt, Argon2 o PBKDF2.
   - Nunca guardar passwords en texto plano.

2. Proteger tokens.
   - Firmar tokens con secreto fuerte.
   - Definir expiracion.
   - Rotar secretos si se filtran.

3. Usar HTTPS en produccion.
   - Configurar TLS en proxy o servidor.
   - Redirigir HTTP a HTTPS.

4. Minimizar datos sensibles.
   - No exponer DNI, telefono o datos personales si no son necesarios.
   - Enmascarar datos en logs.

5. Manejar secretos por entorno.
   - Nunca dejar claves en el repositorio.

### Implementacion sugerida

- Revisar flujo de login web y movil.
- Confirmar que el repositorio de usuarios no compara passwords en texto plano.
- Agregar `PasswordEncoder` en Spring Security o servicio equivalente.
- En logs, evitar imprimir tokens o datos personales completos.

### Pruebas

- Crear usuario y confirmar que password no se guarda legible.
- Revisar logs de login y llamadas moviles.
- Verificar expiracion de sesion/token.

## 5. A05 Injection

### Pregunta guia

¿Se tiene implementado el A05 Injection?

### Riesgo

Entradas no validadas podrian provocar SQL Injection, XSS o manipulacion de comandos:

- Busquedas por nombre o DNI.
- IDs enviados desde URL.
- Formularios de citaciones, actividades, asistencia o reportes.
- Mensajes enviados por padres/docentes.

### Contramedidas propuestas

1. Usar consultas parametrizadas.
   - Mantener `JdbcTemplate` con parametros `?`.
   - Evitar concatenar strings con entradas del usuario.

2. Validar entradas.
   - Longitud maxima.
   - Campos obligatorios.
   - Formatos: fecha, DNI, email, telefono.

3. Escapar salida HTML.
   - Thymeleaf con `th:text` en lugar de texto sin escapar.
   - Evitar `th:utext` salvo necesidad justificada.

4. Sanitizar mensajes.
   - Limitar longitud de mensajes.
   - No permitir HTML activo.

### Implementacion sugerida

- Revisar repositorios que construyen SQL.
- Crear validadores DTO para requests REST.
- Agregar validaciones en controladores web.
- En mensajes de citacion, validar `body` no vacio y longitud maxima.

### Pruebas

- Enviar `' OR '1'='1` en busquedas.
- Enviar `<script>alert(1)</script>` en campos de texto.
- Verificar que se almacene o muestre como texto, no como codigo ejecutable.

## 6. A06 Insecure Design

### Pregunta guia

¿Se tiene implementado el A06 Insecure Design?

### Riesgo

El sistema puede ser vulnerable por decisiones de diseno, aunque el codigo no tenga errores tecnicos evidentes:

- Flujo de citaciones que permita estados contradictorios.
- Acciones repetidas que dupliquen eventos.
- Falta de reglas para cancelar, aceptar o rechazar.
- Falta de separacion entre responsabilidades web y movil.

### Contramedidas propuestas

1. Definir maquina de estados.
   - Citaciones:
     - `pending`
     - `accepted`
     - `rejected`
     - `cancelled`
   - No permitir pasar de `accepted` a `rejected` sin regla explicita.

2. Hacer operaciones idempotentes.
   - Si el usuario envia dos veces la misma respuesta, no duplicar eventos.

3. Documentar flujos.
   - Dashboard de padres.
   - Citaciones virtuales.
   - Asistencia.
   - Reportes.

4. Separar supervision web de acciones moviles.
   - La web docente/admin visualiza y gestiona segun rol.
   - La app movil mantiene su flujo propio.

### Implementacion sugerida

- Mantener validaciones de estado en procedimientos almacenados.
- Documentar contratos en `movil-app/docs`.
- Crear pruebas de transicion de estado.

### Pruebas

- Intentar rechazar una citacion ya aceptada.
- Intentar aceptar una citacion cancelada.
- Enviar dos veces la misma respuesta.
- Verificar que no haya duplicados.

## 7. A07 Authentication Failures

### Pregunta guia

¿Se tiene implementado el A07 Authentication Failures?

### Riesgo

Fallos de autenticacion permiten suplantacion o acceso indebido:

- Sesiones sin expiracion.
- Passwords debiles.
- Tokens sin validacion.
- Login sin control de intentos.
- Autenticacion movil provisional no reemplazada por backend real.

### Contramedidas propuestas

1. Fortalecer login.
   - Validar credenciales en backend.
   - Hashear passwords.
   - Limitar intentos fallidos.

2. Controlar sesiones.
   - Expiracion por inactividad.
   - Logout real.
   - Regeneracion de sesion despues del login.

3. Autenticacion movil.
   - Reemplazar fallbacks provisionales por usuario autenticado.
   - Derivar `parentId` desde token.

4. Politicas de password.
   - Longitud minima.
   - Bloqueo o demora ante multiples fallos.

### Implementacion sugerida

- Revisar `AuthController`, `MobileAuthController` y `MobileAuthInterceptor`.
- Agregar validacion de token en todos los endpoints `/api/mobile/**`.
- Evitar imprimir tokens en consola.

### Pruebas

- Intentar login con credenciales incorrectas repetidas.
- Usar token invalido o expirado.
- Acceder a endpoint movil sin token.

## 8. A08 Software or Data Integrity Failures

### Pregunta guia

¿Se tiene implementado el A08 Software or Data Integrity Failures?

### Riesgo

Datos o actualizaciones pueden alterarse sin control:

- Migraciones no revisadas.
- Datos mock mezclados con datos reales.
- Falta de auditoria en cambios de estado.
- Integridad referencial incompleta.

### Contramedidas propuestas

1. Controlar migraciones.
   - Usar Flyway.
   - No editar migraciones ya aplicadas en produccion.
   - Crear nuevas versiones para cambios posteriores.

2. Aplicar llaves foraneas.
   - Citaciones con padres, estudiantes y docentes.
   - Mensajes con citaciones.

3. Registrar eventos relevantes.
   - Respuesta a citacion.
   - Cancelacion.
   - Mensajes enviados.
   - Cambios de asistencia.

4. Separar mocks de produccion.
   - Mantener mocks solo para pruebas o modo demo.
   - Priorizar repositorios HTTP cuando hay backend.

### Implementacion sugerida

- Revisar migraciones Flyway.
- Verificar constraints en tablas nuevas.
- Registrar eventos en `citation_events`.
- Documentar cuando un dato es mock y cuando es real.

### Pruebas

- Intentar insertar mensaje con citacion inexistente.
- Intentar duplicar receptor de una misma citacion.
- Verificar que Flyway aplique migraciones en orden.

## 9. A09 Security Logging and Alerting Failures

### Pregunta guia

¿Se tiene implementado el A09 Security Logging and Alerting Failures?

### Riesgo

Sin logs y alertas suficientes no se detectan abusos o fallos:

- Intentos de acceso no autorizado.
- Errores repetidos de login.
- Cambios sospechosos de estado.
- Fallos silenciosos en endpoints moviles.

### Contramedidas propuestas

1. Registrar eventos de seguridad.
   - Login exitoso/fallido.
   - Acceso denegado.
   - Cambio de estado importante.
   - Errores de validacion repetidos.

2. No registrar datos sensibles.
   - No imprimir tokens.
   - No imprimir passwords.
   - Evitar DNI completo si no es necesario.

3. Crear alertas operativas.
   - Multiples fallos de login.
   - Muchas respuestas fallidas desde un mismo usuario.
   - Errores 500 repetidos.

4. Centralizar manejo de excepciones.
   - `@ControllerAdvice` para REST.
   - Paginas de error controladas para web.

### Implementacion sugerida

- Configurar `logback-spring.xml`.
- Revisar `System.out.println` en controladores y reemplazar por logger.
- Agregar correlacion simple por request si es posible.

### Pruebas

- Provocar login fallido.
- Provocar acceso no autorizado.
- Revisar que exista log sin exponer datos sensibles.

## 10. A10 Mishandling of Exceptional Conditions

### Pregunta guia

¿Se tiene implementado el A10 Mishandling of Exceptional Conditions?

### Riesgo

Errores mal manejados pueden bloquear la app, exponer detalles internos o dejar estados inconsistentes:

- La app movil queda esperando indefinidamente.
- El backend devuelve errores tecnicos sin formato.
- Excepciones SQL se muestran al usuario.
- Botones quedan activos durante operaciones largas.

### Contramedidas propuestas

1. Manejar timeouts en movil.
   - Toda llamada HTTP critica debe tener `timeout`.
   - Mostrar error amigable y liberar botones.

2. Manejar errores REST con formato uniforme.
   - `code`
   - `message`
   - `details`

3. Evitar bloqueos de UI.
   - Estados `isLoading`, `isResponding`, `isSending`.
   - Deshabilitar botones durante peticiones.

4. Transacciones en operaciones criticas.
   - Responder citacion.
   - Enviar mensaje.
   - Registrar asistencia.

5. No mostrar stack traces.
   - Logs tecnicos para administradores.
   - Mensajes simples para usuarios.

### Implementacion sugerida

- En Flutter, usar `.timeout(Duration(seconds: 15))` en llamadas HTTP.
- En Spring, capturar `SQLException`, `IllegalArgumentException` y errores de validacion.
- Usar `@Transactional` en servicios que cambian estado.

### Pruebas

- Apagar backend y probar app movil.
- Enviar request invalido.
- Forzar error SQL controlado.
- Verificar que la UI no se cuelgue y muestre mensaje.

## Plan de ejecucion recomendado

| Fase | Actividad | Prioridad |
| --- | --- | --- |
| 1 | Revisar autenticacion, autorizacion y acceso por rol | Alta |
| 2 | Validar endpoints moviles y web contra acceso indebido | Alta |
| 3 | Agregar manejo global de errores REST y web | Alta |
| 4 | Revisar consultas SQL y validaciones de entrada | Alta |
| 5 | Configurar logs seguros y remover impresiones sensibles | Media |
| 6 | Auditar dependencias Flutter y Maven | Media |
| 7 | Documentar maquina de estados y contratos | Media |
| 8 | Revisar configuracion productiva: CORS, cookies, HTTPS | Alta |

## Evidencias sugeridas

Para sustentar que las contramedidas existen, se recomienda guardar:

- Capturas de pruebas con acceso denegado.
- Logs de errores controlados.
- Resultado de `flutter analyze`.
- Resultado de `flutter test`.
- Resultado de pruebas backend.
- Capturas de validaciones web.
- Lista de dependencias revisadas.
- Extracto de migraciones y procedimientos con validaciones de estado.

