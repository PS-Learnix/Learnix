package com.learnix.web.security;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Integer studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        if (studentCount != null && studentCount > 0) {
            log.info("Database already seeded. Skipping dynamic seeding.");
            return;
        }

        log.info("Seeding database dynamically with Faker...");
        Faker faker = new Faker(new Locale("es"));
        Random rand = new Random(42);

        // 1. Insert Students
        List<Integer> studentIds = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String dni = String.format("%08d", faker.number().numberBetween(10000000, 99999999));
            LocalDate birthDate = faker.date().birthday(6, 12).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            jdbcTemplate.update(
                    "INSERT INTO students (first_name, last_name, dni, birth_date) VALUES (?, ?, ?, ?)",
                    firstName, lastName, dni, java.sql.Date.valueOf(birthDate)
            );
            
            Integer generatedId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            if (generatedId != null) {
                studentIds.add(generatedId);
            }
        }
        log.info("Seeded {} students.", studentIds.size());

        // 2. Fetch Course Periods
        List<Map<String, Object>> coursePeriods = jdbcTemplate.queryForList(
                "SELECT cp.id_course_period, c.id_teacher FROM course_periods cp JOIN courses c ON cp.id_course = c.id_course"
        );

        if (coursePeriods.isEmpty()) {
            log.warn("No course periods found in database. Seeding aborted.");
            return;
        }

        // 3. Enroll Students in Course Periods
        for (Integer studentId : studentIds) {
            List<Map<String, Object>> shuffled = new ArrayList<>(coursePeriods);
            Collections.shuffle(shuffled, rand);
            int enrollCount = 2 + rand.nextInt(2);
            for (int i = 0; i < enrollCount; i++) {
                Integer cpId = (Integer) shuffled.get(i).get("id_course_period");
                jdbcTemplate.update(
                        "INSERT INTO enrollments (id_student, id_course_period, status) VALUES (?, ?, 'active')",
                        studentId, cpId
                );
            }
        }
        log.info("Enrolled students in course periods.");

        // 4. Link the test parent (padre@learnix.com) to 2 random students
        Integer parentId = jdbcTemplate.queryForObject(
                "SELECT id_parent FROM parents WHERE email = 'padre@learnix.com' LIMIT 1",
                Integer.class
        );

        List<Integer> parentStudentIds = new ArrayList<>();
        if (parentId != null && studentIds.size() >= 2) {
            parentStudentIds.add(studentIds.get(0));
            parentStudentIds.add(studentIds.get(1));

            jdbcTemplate.update(
                    "INSERT INTO parent_students (id_parent, id_student, relationship, is_primary) VALUES (?, ?, 'Padre', TRUE)",
                    parentId, studentIds.get(0)
            );
            jdbcTemplate.update(
                    "INSERT INTO parent_students (id_parent, id_student, relationship, is_primary) VALUES (?, ?, 'Madre', FALSE)",
                    parentId, studentIds.get(1)
            );
            log.info("Linked parent (padre@learnix.com) with students ID: {} and {}", studentIds.get(0), studentIds.get(1));
        }

        // 5. Create Activities and Grades
        List<String> terms = Arrays.asList("B1", "B2");
        List<String> gradeValues = Arrays.asList("AD", "A", "B", "C");

        for (Map<String, Object> cp : coursePeriods) {
            Integer cpId = (Integer) cp.get("id_course_period");
            List<Integer> enrolledStudents = jdbcTemplate.queryForList(
                    "SELECT id_student FROM enrollments WHERE id_course_period = ?",
                    Integer.class,
                    cpId
            );

            if (enrolledStudents.isEmpty()) continue;

            for (int a = 1; a <= 4; a++) {
                String activityName = "Actividad Evaluada " + a;
                double weight = 25.0;
                LocalDate dueDate = LocalDate.now().minusDays(rand.nextInt(30));
                String term = terms.get(rand.nextInt(terms.size()));

                jdbcTemplate.update(
                        "INSERT INTO activities (id_course_period, name, weight, due_date, term) VALUES (?, ?, ?, ?, ?)",
                        cpId, activityName, weight, java.sql.Date.valueOf(dueDate), term
                );

                Integer activityId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

                if (activityId != null) {
                    for (Integer studentId : enrolledStudents) {
                        String gradeVal = gradeValues.get(rand.nextInt(gradeValues.size()));
                        jdbcTemplate.update(
                                "INSERT INTO grades (id_activity, id_student, value) VALUES (?, ?, ?)",
                                activityId, studentId, gradeVal
                        );
                    }
                }
            }
        }
        log.info("Seeded activities and grades.");

        // 6. Create Attendance logs (for all past weekdays strictly before today)
        LocalDate startOfSchool = LocalDate.of(2026, 3, 1);
        LocalDate today = LocalDate.now();
        List<LocalDate> schoolDays = new ArrayList<>();
        LocalDate checkDate = startOfSchool;
        while (checkDate.isBefore(today)) {
            int dayOfWeek = checkDate.getDayOfWeek().getValue();
            if (dayOfWeek < 6) { // Monday to Friday (1 to 5)
                schoolDays.add(checkDate);
            }
            checkDate = checkDate.plusDays(1);
        }

        for (Map<String, Object> cp : coursePeriods) {
            Integer cpId = (Integer) cp.get("id_course_period");
            Integer teacherId = (Integer) cp.get("id_teacher");
            List<Integer> enrolledStudents = jdbcTemplate.queryForList(
                    "SELECT id_student FROM enrollments WHERE id_course_period = ?",
                    Integer.class,
                    cpId
            );

            if (enrolledStudents.isEmpty()) continue;

            for (LocalDate date : schoolDays) {
                for (Integer studentId : enrolledStudents) {
                    boolean didAttend = rand.nextDouble() < 0.92;
                    String observation = didAttend ? null : faker.options().option("Enfermo", "Falta Justificada", "Tardanza");
                    jdbcTemplate.update(
                            "INSERT INTO attendances (id_student, id_course_period, id_teacher, date, did_attend, observation) VALUES (?, ?, ?, ?, ?, ?)",
                            studentId, cpId, teacherId, java.sql.Date.valueOf(date), didAttend, observation
                    );
                }
            }
        }
        log.info("Seeded attendance records for all past weekdays before today.");

        // 7. Seed parent alerts, announcements, reports if parent exists
        if (parentId != null && !parentStudentIds.isEmpty()) {
            Integer primaryStudentId = parentStudentIds.get(0);
            
            jdbcTemplate.update(
                    "INSERT INTO parent_alerts (id_student, id_parent, type, title, detail, severity, status, event_date) VALUES (?, ?, 'citation', 'Citación Académica', 'Conversar sobre el rendimiento general de matemáticas.', 'info', 'new', NOW())",
                    primaryStudentId, parentId
            );
            jdbcTemplate.update(
                    "INSERT INTO parent_alerts (id_student, id_parent, type, title, detail, severity, status, event_date) VALUES (?, ?, 'incident', 'Incidencia Recreo', 'Falta de disciplina menor registrada por tutor.', 'warning', 'new', NOW())",
                    primaryStudentId, parentId
            );

            jdbcTemplate.update(
                    "INSERT INTO announcements (title, body, sender_name, priority) VALUES (?, ?, ?, ?)",
                    "Clases Abiertas de Ciencias", "Estimados padres, los invitamos a asistir a las clases abiertas de proyectos de ciencias este viernes.", "Coordinación Primaria", "Media"
            );
            Integer announcementId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            if (announcementId != null) {
                jdbcTemplate.update(
                        "INSERT INTO announcement_recipients (id_announcement, id_parent, status) VALUES (?, ?, 'new')",
                        announcementId, parentId
                );
            }

            jdbcTemplate.update(
                    "INSERT INTO academic_reports (id_student, report_type, title, format, file_url) VALUES (?, 'Rendimiento académico', 'Libreta de Notas Bimestre 1', 'pdf', 'https://learnix.yoshua-cloud.dedyn.io/reports/mock_report.pdf')",
                    primaryStudentId
            );

            // Seed individual citation
            jdbcTemplate.update(
                    "INSERT INTO virtual_citations (id_student, id_teacher, created_by_user_id, title, detail, scheduled_at, mode, meeting_url, scope) VALUES (?, 1, 1, ?, ?, DATE_ADD(NOW(), INTERVAL 5 DAY), 'virtual', ?, 'individual')",
                    primaryStudentId, "Citación Individual de Tutoría", "Reunión de tutoría para conversar sobre el avance académico y comportamiento en aula.", "https://meet.google.com/abc-defg-hij"
            );
            Integer indivCitationId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            if (indivCitationId != null) {
                jdbcTemplate.update(
                        "INSERT INTO citation_recipients (id_citation, id_parent, id_student, recipient_status) VALUES (?, ?, ?, 'pending')",
                        indivCitationId, parentId, primaryStudentId
                );
                jdbcTemplate.update(
                        "INSERT INTO citation_messages (id_citation, sender_type, sender_id, body, sent_at) VALUES (?, 'user', 1, 'Estimado acudiente, solicito una reunión virtual para revisar el avance.', DATE_SUB(NOW(), INTERVAL 2 HOUR))",
                        indivCitationId
                );
                jdbcTemplate.update(
                        "INSERT INTO citation_events (id_citation, actor_type, actor_id, event_type, payload) VALUES (?, 'user', 1, 'created', JSON_OBJECT('newStatus', 'pending'))",
                        indivCitationId
                );
            }

            // Seed section citation
            List<Integer> enrolledCpIds = jdbcTemplate.queryForList(
                    "SELECT id_course_period FROM enrollments WHERE id_student = ?",
                    Integer.class,
                    primaryStudentId
            );
            if (!enrolledCpIds.isEmpty()) {
                Integer sectionCpId = enrolledCpIds.get(0);
                jdbcTemplate.update(
                        "INSERT INTO virtual_citations (id_course_period, id_teacher, created_by_user_id, title, detail, scheduled_at, mode, meeting_url, scope) VALUES (?, 1, 1, ?, ?, DATE_ADD(NOW(), INTERVAL 3 DAY), 'virtual', ?, 'section')",
                        sectionCpId, "Reunión General de la Sección", "Entrega de libretas e informes de avance del bimestre.", "https://meet.google.com/xyz-pdqr-lmn"
                );
                Integer sectionCitationId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                if (sectionCitationId != null) {
                    List<Map<String, Object>> studentsInCp = jdbcTemplate.queryForList(
                            "SELECT e.id_student, ps.id_parent FROM enrollments e LEFT JOIN parent_students ps ON e.id_student = ps.id_student WHERE e.id_course_period = ?",
                            sectionCpId
                    );
                    for (Map<String, Object> scp : studentsInCp) {
                        Integer sId = (Integer) scp.get("id_student");
                        Integer pId = (Integer) scp.get("id_parent");
                        if (pId != null) {
                            jdbcTemplate.update(
                                    "INSERT INTO citation_recipients (id_citation, id_parent, id_student, recipient_status) VALUES (?, ?, ?, 'pending') ON DUPLICATE KEY UPDATE recipient_status = recipient_status",
                                    sectionCitationId, pId, sId
                            );
                        }
                    }
                }
            }

            log.info("Seeded parent specific alerts, announcements, reports, and citations.");
        }

        log.info("Dynamic database seeding successfully completed.");
    }
}
