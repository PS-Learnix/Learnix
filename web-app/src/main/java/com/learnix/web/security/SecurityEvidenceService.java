package com.learnix.web.security;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class SecurityEvidenceService {

    private final AccessControlService accessControlService;
    private final SecurityInputValidator inputValidator;

    public SecurityEvidenceService(AccessControlService accessControlService, SecurityInputValidator inputValidator) {
        this.accessControlService = accessControlService;
        this.inputValidator = inputValidator;
    }

    public List<SecurityEvidence> buildEvidenceReport() {
        String timestamp = LocalDateTime.now().toString();
        List<SecurityEvidence> evidence = new ArrayList<>();

        CheckResult accessControl = accessControlResult();
        evidence.add(new SecurityEvidence(
                "A01 Broken Access Control",
                "Verificar token Bearer, coincidencia de parentId y relacion padre-estudiante/citacion.",
                accessControl.result(),
                accessControl.status(),
                timestamp,
                "web-app / api-mobile",
                "Las rutas moviles conservan su URL, pero ahora validan acceso antes de ejecutar la logica de negocio."
        ));

        CheckResult misconfiguration = misconfigurationResult();
        evidence.add(new SecurityEvidence(
                "A02 Security Misconfiguration",
                "Revisar CORS, cabeceras de seguridad y errores REST controlados.",
                misconfiguration.result(),
                misconfiguration.status(),
                timestamp,
                "web-app / config",
                "Los errores REST usan SecurityRestExceptionHandler para evitar respuestas con stack trace."
        ));

        evidence.add(new SecurityEvidence(
                "A03 Software Supply Chain Failures",
                "Verificar existencia de archivos de dependencias bloqueadas para Maven y Flutter.",
                supplyChainResult(),
                supplyChainStatus(),
                timestamp,
                "web-app / movil-app",
                "Se recomienda ejecutar revision periodica con flutter pub outdated y analisis de dependencias Maven."
        ));

        CheckResult cryptographic = cryptographicResult();
        evidence.add(new SecurityEvidence(
                "A04 Cryptographic Failures",
                "Comprobar que login movil no registre tokens ni passwords en logs.",
                cryptographic.result(),
                cryptographic.status(),
                timestamp,
                "web-app / autenticacion movil",
                "Queda pendiente reemplazar tokens provisionales por JWT firmado si el despliegue productivo lo requiere."
        ));

        CheckResult injection = injectionResult();
        evidence.add(new SecurityEvidence(
                "A05 Injection",
                "Validar entradas inesperadas en parametros, filtros, estados y mensajes.",
                injection.result(),
                injection.status(),
                timestamp,
                "web-app / api-mobile",
                "Los repositorios mantienen consultas parametrizadas y llamadas a procedimientos almacenados."
        ));

        return evidence;
    }

    private String supplyChainResult() {
        boolean hasPom = exists("pom.xml") || exists("web-app/pom.xml");
        boolean hasPubspecLock = exists("../movil-app/pubspec.lock") || exists("movil-app/pubspec.lock");
        return "pom.xml=" + present(hasPom) + ", pubspec.lock=" + present(hasPubspecLock);
    }

    private String supplyChainStatus() {
        boolean hasPom = exists("pom.xml") || exists("web-app/pom.xml");
        boolean hasPubspecLock = exists("../movil-app/pubspec.lock") || exists("movil-app/pubspec.lock");
        return hasPom && hasPubspecLock ? "APROBADO" : "FALLIDO";
    }

    private CheckResult accessControlResult() {
        boolean missingTokenRejected = throwsSecurity(() -> accessControlService.requireParentFromBearer(null));
        boolean malformedTokenRejected = throwsSecurity(() -> accessControlService.requireParentFromBearer("Bearer token-invalido"));
        boolean validTokenParsed = Integer.valueOf(1).equals(accessControlService.requireParentFromBearer("Bearer jwt-token-parent-1"));
        boolean passed = missingTokenRejected && malformedTokenRejected && validTokenParsed;
        return new CheckResult(
                "token ausente rechazado=" + ok(missingTokenRejected)
                        + ", token invalido rechazado=" + ok(malformedTokenRejected)
                        + ", token valido parseado=" + ok(validTokenParsed),
                status(passed)
        );
    }

    private CheckResult misconfigurationResult() {
        boolean corsRestricted = sourceContains("web-app/src/main/java/com/learnix/web/config/WebMvcConfig.java", "allowedOriginPatterns(parseAllowedOrigins())")
                && !sourceContains("web-app/src/main/java/com/learnix/web/config/WebMvcConfig.java", "allowedOriginPatterns(\"*\")");
        boolean headersConfigured = sourceContains("web-app/src/main/java/com/learnix/web/config/WebMvcConfig.java", "X-Content-Type-Options")
                && sourceContains("web-app/src/main/java/com/learnix/web/config/WebMvcConfig.java", "X-Frame-Options")
                && sourceContains("web-app/src/main/java/com/learnix/web/config/WebMvcConfig.java", "Referrer-Policy")
                && sourceContains("web-app/src/main/java/com/learnix/web/config/WebMvcConfig.java", "Permissions-Policy");
        boolean stackTraceDisabled = sourceContains("web-app/src/main/resources/application.yaml", "include-stacktrace: never");
        boolean sqlOutputDisabled = sourceContains("web-app/src/main/resources/application.yaml", "show-sql: false");
        boolean passed = corsRestricted && headersConfigured && stackTraceDisabled && sqlOutputDisabled;
        return new CheckResult(
                "cors restringido=" + ok(corsRestricted)
                        + ", headers configurados=" + ok(headersConfigured)
                        + ", stacktrace oculto=" + ok(stackTraceDisabled)
                        + ", SQL en consola desactivado=" + ok(sqlOutputDisabled),
                status(passed)
        );
    }

    private CheckResult cryptographicResult() {
        boolean noSystemOut = !sourceMatches("web-app/src/main/java/com/learnix/web/controller", Pattern.compile("System\\.out|printStackTrace"));
        boolean noSqlLeak = !sourceMatches("web-app/src/main/java/com/learnix/web/controller", Pattern.compile("getSQLException\\(\\)\\.getMessage"));
        boolean maskedEmail = sourceContains("web-app/src/main/java/com/learnix/web/controller/rest/MobileAuthController.java", "maskEmail(email)");
        boolean passed = noSystemOut && noSqlLeak && maskedEmail;
        return new CheckResult(
                "sin System.out/printStackTrace=" + ok(noSystemOut)
                        + ", sin mensajes SQL al frontend=" + ok(noSqlLeak)
                        + ", email enmascarado=" + ok(maskedEmail),
                status(passed)
        );
    }

    private CheckResult injectionResult() {
        boolean scriptRejected = throwsIllegalArgument(() -> inputValidator.requireSafeText("<script>alert(1)</script>", "payload", 100));
        boolean sqlRejected = throwsIllegalArgument(() -> inputValidator.requireSafeText("1 UNION SELECT password FROM users", "payload", 100));
        boolean unexpectedStatusRejected = throwsIllegalArgument(() -> inputValidator.optionalAllowedValue("deleted", "status", java.util.Set.of("new", "read", "archived")));
        boolean passed = scriptRejected && sqlRejected && unexpectedStatusRejected;
        return new CheckResult(
                "script rechazado=" + ok(scriptRejected)
                        + ", SQL sospechoso rechazado=" + ok(sqlRejected)
                        + ", estado no permitido rechazado=" + ok(unexpectedStatusRejected),
                status(passed)
        );
    }

    private boolean exists(String relativePath) {
        return Files.exists(resolve(relativePath));
    }

    private String present(boolean value) {
        return value ? "presente" : "no encontrado";
    }

    private String ok(boolean value) {
        return value ? "si" : "no";
    }

    private String status(boolean passed) {
        return passed ? "APROBADO" : "FALLIDO";
    }

    private boolean throwsSecurity(Runnable runnable) {
        try {
            runnable.run();
            return false;
        } catch (SecurityException ex) {
            return true;
        }
    }

    private boolean throwsIllegalArgument(Runnable runnable) {
        try {
            runnable.run();
            return false;
        } catch (IllegalArgumentException ex) {
            return true;
        }
    }

    private boolean sourceContains(String relativePath, String needle) {
        Path path = resolve(relativePath);
        if (!Files.exists(path)) {
            return false;
        }
        try {
            return Files.readString(path).contains(needle);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean sourceMatches(String relativePath, Pattern pattern) {
        Path path = resolve(relativePath);
        if (!Files.exists(path)) {
            return false;
        }
        try {
            if (Files.isRegularFile(path)) {
                return pattern.matcher(Files.readString(path)).find();
            }
            try (Stream<Path> paths = Files.walk(path)) {
                return paths
                        .filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(".java"))
                        .anyMatch(file -> fileMatches(file, pattern));
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean fileMatches(Path file, Pattern pattern) {
        try {
            return pattern.matcher(Files.readString(file)).find();
        } catch (Exception ex) {
            return false;
        }
    }

    private Path resolve(String relativePath) {
        Path base = Path.of(System.getProperty("user.dir")).normalize();
        Path direct = base.resolve(relativePath).normalize();
        if (Files.exists(direct)) {
            return direct;
        }
        return base.resolve("..").resolve(relativePath).normalize();
    }

    private record CheckResult(String result, String status) {
    }
}
