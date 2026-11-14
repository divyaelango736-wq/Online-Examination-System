package in.vcet.exam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {

        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // ==========================================
        // 1. HEALTH API
        // ==========================================
        server.createContext("/api/health", exchange -> {
            if (handleCors(exchange)) return;

            try (Connection connection = Database.getConnection()) {
                send(exchange, 200, gson.toJson(Map.of(
                        "status", "ok",
                        "database", "connected"
                )));
            } catch (SQLException e) {
                send(exchange, 500, gson.toJson(Map.of(
                        "status", "error",
                        "error", "Database connection failed: " + e.getMessage()
                )));
            }
        });

        // ==========================================
        // 2. STUDENTS API (GET ALL, POST REGISTER)
        // ==========================================
        server.createContext("/api/students", exchange -> {
            if (handleCors(exchange)) return;

            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                String sql = "SELECT student_id, name, email, department FROM students ORDER BY student_id";

                try (Connection conn = Database.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    List<Map<String, Object>> list = new ArrayList<>();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("studentId", rs.getInt("student_id"));
                        row.put("name", rs.getString("name"));
                        row.put("email", rs.getString("email"));
                        row.put("department", rs.getString("department"));
                        list.add(row);
                    }

                    send(exchange, 200, gson.toJson(list));

                } catch (Exception e) {
                    send(exchange, 500,
                            gson.toJson(Map.of("error", "Unable to load students")));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    JsonObject data = JsonParser.parseString(body).getAsJsonObject();

                    String name = data.get("name").getAsString().trim();
                    String email = data.get("email").getAsString().trim();
                    String department = data.get("department").getAsString().trim();
                    String password = data.has("password")
                            ? data.get("password").getAsString() : "";

                    if (name.isEmpty() || email.isEmpty() ||
                            department.isEmpty() || password.length() < 8) {
                        send(exchange, 400, gson.toJson(Map.of(
                                "error", "Enter all details. Password must contain at least 8 characters."
                        )));
                        return;
                    }

                    String hashedPassword = PasswordUtil.hashPassword(password);

                    String sql = "INSERT INTO students (name, email, password, department) " +
                            "VALUES (?, ?, ?, ?) RETURNING student_id";

                    try (Connection conn = Database.getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setString(1, name);
                        ps.setString(2, email);
                        ps.setString(3, hashedPassword);
                        ps.setString(4, department);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();

                            send(exchange, 201, gson.toJson(Map.of(
                                    "message", "Student registered successfully",
                                    "studentId", rs.getInt("student_id")
                            )));
                        }
                    }

                } catch (java.sql.SQLException e) {
                    send(exchange, 400, gson.toJson(Map.of(
                            "error", "Registration failed. Email may already exist."
                    )));
                } catch (Exception e) {
                    send(exchange, 400, gson.toJson(Map.of(
                            "error", "Invalid student details."
                    )));
                }
                return;
            }

            send(exchange, 405,
                    gson.toJson(Map.of("error", "Method not allowed")));
        });

        // ==========================================
        // 3. EXAMS API (GET ALL, POST CREATE)
        // ==========================================
        server.createContext("/api/exams", exchange -> {
            if (handleCors(exchange)) return;

            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                String sql = "SELECT exam_id, exam_name, subject, total_marks, exam_date " +
                        "FROM exams ORDER BY exam_id DESC";

                try (Connection conn = Database.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    List<Map<String, Object>> list = new ArrayList<>();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("examId", rs.getInt("exam_id"));
                        row.put("examName", rs.getString("exam_name"));
                        row.put("subject", rs.getString("subject"));
                        row.put("totalMarks", rs.getInt("total_marks"));
                        row.put("examDate", rs.getDate("exam_date").toString());
                        list.add(row);
                    }

                    send(exchange, 200, gson.toJson(list));

                } catch (Exception e) {
                    send(exchange, 500,
                            gson.toJson(Map.of("error", "Unable to load examinations")));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    JsonObject data = JsonParser.parseString(body).getAsJsonObject();

                    String examName = data.get("examName").getAsString().trim();
                    String subject = data.get("subject").getAsString().trim();
                    int totalMarks = data.get("totalMarks").getAsInt();
                    Date examDate = Date.valueOf(
                            data.get("examDate").getAsString()
                    );

                    if (examName.isEmpty() || subject.isEmpty() || totalMarks <= 0) {
                        send(exchange, 400,
                                gson.toJson(Map.of("error", "Invalid examination details")));
                        return;
                    }

                    String sql = "INSERT INTO exams (exam_name, subject, total_marks, exam_date) " +
                            "VALUES (?, ?, ?, ?) RETURNING exam_id";

                    try (Connection conn = Database.getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setString(1, examName);
                        ps.setString(2, subject);
                        ps.setInt(3, totalMarks);
                        ps.setDate(4, examDate);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();

                            send(exchange, 201, gson.toJson(Map.of(
                                    "message", "Examination created successfully",
                                    "examId", rs.getInt("exam_id")
                            )));
                        }
                    }

                } catch (Exception e) {
                    send(exchange, 400,
                            gson.toJson(Map.of("error", "Unable to create examination")));
                }
                return;
            }

            send(exchange, 405,
                    gson.toJson(Map.of("error", "Method not allowed")));
        });

        // ==========================================
        // 4. QUESTIONS API (GET by examId, POST add)
        // ==========================================
        server.createContext("/api/questions", exchange -> {
            if (handleCors(exchange)) return;

            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                int examId = -1;
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2 && "examId".equalsIgnoreCase(pair[0])) {
                            try {
                                examId = Integer.parseInt(pair[1]);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }

                if (examId <= 0) {
                    send(exchange, 400, gson.toJson(Map.of("error", "Please provide a valid examId parameter.")));
                    return;
                }

                String sql = "SELECT question_id, exam_id, question_text, option_a, option_b, option_c, option_d " +
                             "FROM questions WHERE exam_id = ? ORDER BY RANDOM() LIMIT 5";
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, examId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        List<Map<String, Object>> questions = new ArrayList<>();
                        while (rs.next()) {
                            Map<String, Object> q = new LinkedHashMap<>();
                            q.put("questionId", rs.getInt("question_id"));
                            q.put("examId", rs.getInt("exam_id"));
                            q.put("questionText", rs.getString("question_text"));
                            q.put("optionA", rs.getString("option_a"));
                            q.put("optionB", rs.getString("option_b"));
                            q.put("optionC", rs.getString("option_c"));
                            q.put("optionD", rs.getString("option_d"));
                            questions.add(q);
                        }
                        send(exchange, 200, gson.toJson(questions));
                    }
                } catch (Exception e) {
                    send(exchange, 500, gson.toJson(Map.of("error", e.getMessage())));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject data = JsonParser.parseString(body).getAsJsonObject();

                    int examId = data.get("examId").getAsInt();
                    String questionText = data.get("questionText").getAsString().trim();
                    String optionA = data.get("optionA").getAsString().trim();
                    String optionB = data.get("optionB").getAsString().trim();
                    String optionC = data.get("optionC").getAsString().trim();
                    String optionD = data.get("optionD").getAsString().trim();
                    String correctAnswer = data.get("correctAnswer").getAsString().trim().toUpperCase();

                    if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() ||
                            optionC.isEmpty() || optionD.isEmpty() ||
                            !List.of("A", "B", "C", "D").contains(correctAnswer)) {
                        send(exchange, 400, gson.toJson(Map.of("error", "Invalid question details. Options A-D and valid correct answer required.")));
                        return;
                    }

                    String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING question_id";
                    try (Connection conn = Database.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, examId);
                        stmt.setString(2, questionText);
                        stmt.setString(3, optionA);
                        stmt.setString(4, optionB);
                        stmt.setString(5, optionC);
                        stmt.setString(6, optionD);
                        stmt.setString(7, correctAnswer);

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                send(exchange, 201, gson.toJson(Map.of(
                                        "message", "Question added successfully",
                                        "questionId", rs.getInt("question_id")
                                )));
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    send(exchange, 400, gson.toJson(Map.of("error", e.getMessage())));
                    return;
                }
            }

            send(exchange, 405, gson.toJson(Map.of("error", "Method not allowed.")));
        });

        // ==========================================
        // 5. SUBMIT EXAM API
        // ==========================================
        server.createContext("/api/submit-exam", exchange -> {
            if (handleCors(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, gson.toJson(Map.of("error", "Method not allowed")));
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject data = JsonParser.parseString(body).getAsJsonObject();

                int studentId = data.get("studentId").getAsInt();
                int examId = data.get("examId").getAsInt();
                JsonArray answers = data.getAsJsonArray("answers");

                if (answers == null || answers.isEmpty()) {
                    send(exchange, 400, gson.toJson(Map.of("error", "Please submit answers.")));
                    return;
                }

                try (Connection connection = Database.getConnection()) {
                    connection.setAutoCommit(false);

                    try {
                        // 1. Verify student exists
                        String studentSql = "SELECT student_id, name FROM students WHERE student_id = ?";
                        String studentName = "";
                        try (PreparedStatement ps = connection.prepareStatement(studentSql)) {
                            ps.setInt(1, studentId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next()) {
                                    throw new Exception("Student not found with ID: " + studentId);
                                }
                                studentName = rs.getString("name");
                            }
                        }

                        // 2. Verify exam exists & get total marks
                        int totalMarks;
                        String examName = "";
                        String examSql = "SELECT exam_name, total_marks FROM exams WHERE exam_id = ?";
                        try (PreparedStatement ps = connection.prepareStatement(examSql)) {
                            ps.setInt(1, examId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next()) {
                                    throw new Exception("Exam not found with ID: " + examId);
                                }
                                examName = rs.getString("exam_name");
                                totalMarks = rs.getInt("total_marks");
                            }
                        }

                        // 3. Fetch correct answers for questions of this exam
                        Map<Integer, String> correctAnswers = new LinkedHashMap<>();
                        String qSql = "SELECT question_id, correct_answer FROM questions WHERE exam_id = ?";
                        try (PreparedStatement ps = connection.prepareStatement(qSql)) {
                            ps.setInt(1, examId);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    correctAnswers.put(
                                            rs.getInt("question_id"),
                                            rs.getString("correct_answer").trim().toUpperCase()
                                    );
                                }
                            }
                        }

                        if (correctAnswers.isEmpty()) {
                            throw new Exception("No questions found for exam ID: " + examId);
                        }

                        // 4. Validate and record answers
                        int correctCount = 0;
                        int totalSubmitted = answers.size();
                        List<Integer> processedQuestions = new ArrayList<>();

                        String answerSql = "INSERT INTO student_answers (student_id, question_id, selected_answer, is_correct) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ps = connection.prepareStatement(answerSql)) {
                            for (JsonElement element : answers) {
                                JsonObject ans = element.getAsJsonObject();
                                int questionId = ans.get("questionId").getAsInt();
                                String selected = ans.has("selectedAnswer") && !ans.get("selectedAnswer").isJsonNull()
                                        ? ans.get("selectedAnswer").getAsString().trim().toUpperCase()
                                        : "";

                                if (selected.isEmpty() || !List.of("A", "B", "C", "D").contains(selected)) {
                                    throw new Exception("Invalid answer selected for question " + questionId);
                                }

                                if (!correctAnswers.containsKey(questionId)) {
                                    throw new Exception("Question " + questionId + " does not belong to this exam.");
                                }

                                if (processedQuestions.contains(questionId)) {
                                    throw new Exception("Duplicate answer submitted for question " + questionId);
                                }
                                processedQuestions.add(questionId);

                                boolean isCorrect = selected.equals(correctAnswers.get(questionId));
                                if (isCorrect) {
                                    correctCount++;
                                }

                                ps.setInt(1, studentId);
                                ps.setInt(2, questionId);
                                ps.setString(3, selected);
                                ps.setBoolean(4, isCorrect);
                                ps.addBatch();
                            }
                            ps.executeBatch();
                        }

                        // 5. Calculate marks proportionally
                        int marksObtained = (int) Math.round(((double) correctCount / totalSubmitted) * totalMarks);
                        double percentage = Math.round(((double) marksObtained / totalMarks) * 1000.0) / 10.0;
                        String status = percentage >= 50.0 ? "Passed" : "Needs Improvement";

                        // 6. Record result
                        String resultSql = "INSERT INTO results (student_id, exam_id, marks_obtained, status, exam_taken_at) " +
                                           "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING result_id";
                        int resultId;
                        try (PreparedStatement ps = connection.prepareStatement(resultSql)) {
                            ps.setInt(1, studentId);
                            ps.setInt(2, examId);
                            ps.setInt(3, marksObtained);
                            ps.setString(4, status);
                            try (ResultSet rs = ps.executeQuery()) {
                                rs.next();
                                resultId = rs.getInt("result_id");
                            }
                        }

                        connection.commit();

                        Map<String, Object> resp = new LinkedHashMap<>();
                        resp.put("message", "Exam submitted and evaluated successfully");
                        resp.put("resultId", resultId);
                        resp.put("studentId", studentId);
                        resp.put("studentName", studentName);
                        resp.put("examId", examId);
                        resp.put("examName", examName);
                        resp.put("totalQuestions", totalSubmitted);
                        resp.put("correctAnswers", correctCount);
                        resp.put("marksObtained", marksObtained);
                        resp.put("totalMarks", totalMarks);
                        resp.put("percentage", percentage);
                        resp.put("status", status);

                        send(exchange, 201, gson.toJson(resp));

                    } catch (Exception ex) {
                        connection.rollback();
                        send(exchange, 400, gson.toJson(Map.of("error", ex.getMessage())));
                    }
                }
            } catch (Exception e) {
                send(exchange, 400, gson.toJson(Map.of("error", e.getMessage())));
            }
        });

        // ==========================================
        // 6. RESULTS API (GET all or by studentId)
        // ==========================================
        server.createContext("/api/results", exchange -> {
            if (handleCors(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, gson.toJson(Map.of("error", "Method not allowed")));
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Integer filterStudentId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "studentId".equalsIgnoreCase(pair[0])) {
                        try {
                            filterStudentId = Integer.parseInt(pair[1]);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            StringBuilder sql = new StringBuilder(
                    "SELECT r.result_id, r.student_id, s.name AS student_name, s.department, " +
                    "r.exam_id, e.exam_name, e.subject, r.marks_obtained, e.total_marks, " +
                    "ROUND((r.marks_obtained * 100.0 / NULLIF(e.total_marks, 0)), 1) AS percentage, " +
                    "r.status, r.exam_taken_at " +
                    "FROM results r " +
                    "JOIN students s ON s.student_id = r.student_id " +
                    "JOIN exams e ON e.exam_id = r.exam_id "
            );

            if (filterStudentId != null) {
                sql.append("WHERE r.student_id = ? ");
            }
            sql.append("ORDER BY r.result_id DESC");

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

                if (filterStudentId != null) {
                    stmt.setInt(1, filterStudentId);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("resultId", rs.getInt("result_id"));
                        row.put("studentId", rs.getInt("student_id"));
                        row.put("studentName", rs.getString("student_name"));
                        row.put("department", rs.getString("department"));
                        row.put("examId", rs.getInt("exam_id"));
                        row.put("examName", rs.getString("exam_name"));
                        row.put("subject", rs.getString("subject"));
                        row.put("marksObtained", rs.getInt("marks_obtained"));
                        row.put("totalMarks", rs.getInt("total_marks"));
                        row.put("percentage", rs.getDouble("percentage"));
                        row.put("status", rs.getString("status"));
                        row.put("examTakenAt", rs.getString("exam_taken_at"));
                        list.add(row);
                    }
                    send(exchange, 200, gson.toJson(list));
                }
            } catch (SQLException e) {
                send(exchange, 500, gson.toJson(Map.of("error", e.getMessage())));
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Online Examination System backend running on http://localhost:" + port);
    }

    private static boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static void send(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
            return;
        }

        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
