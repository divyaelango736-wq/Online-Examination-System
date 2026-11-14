-- =======================================================
-- Online Examination Management and Student Performance Analysis System
-- Database Schema for PostgreSQL (online_exam_db)
-- Scenario 28 | Register Number 28
-- =======================================================

-- 1. Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL DEFAULT 'student123',
    department VARCHAR(50) DEFAULT 'Information Technology'
);

-- 2. Exams Table
CREATE TABLE IF NOT EXISTS exams (
    exam_id SERIAL PRIMARY KEY,
    exam_name VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    total_marks INT NOT NULL DEFAULT 100,
    exam_date DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 3. Questions Table
CREATE TABLE IF NOT EXISTS questions (
    question_id SERIAL PRIMARY KEY,
    exam_id INT NOT NULL REFERENCES exams(exam_id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_answer CHAR(1) NOT NULL CHECK (correct_answer IN ('A', 'B', 'C', 'D'))
);

-- 4. Student Answers Table
CREATE TABLE IF NOT EXISTS student_answers (
    answer_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    question_id INT NOT NULL REFERENCES questions(question_id) ON DELETE CASCADE,
    selected_answer CHAR(1) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. Results Table
CREATE TABLE IF NOT EXISTS results (
    result_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    exam_id INT NOT NULL REFERENCES exams(exam_id) ON DELETE CASCADE,
    marks_obtained INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'Completed',
    exam_taken_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Admins Table
CREATE TABLE IF NOT EXISTS admins (
    admin_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_questions_exam_id ON questions(exam_id);
CREATE INDEX IF NOT EXISTS idx_results_student_id ON results(student_id);
CREATE INDEX IF NOT EXISTS idx_results_exam_id ON results(exam_id);
CREATE INDEX IF NOT EXISTS idx_student_answers_student ON student_answers(student_id);
