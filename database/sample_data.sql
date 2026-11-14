-- =======================================================
-- Online Examination Management System - Sample Data
-- Scenario 28 | Register Number 28
-- =======================================================

-- 1. Insert Students
INSERT INTO students (name, email, password, department) VALUES
    ('Arun Kumar', 'arun@example.com', 'student123', 'Information Technology'),
    ('Priya S', 'priya@example.com', 'student123', 'Information Technology'),
    ('Kumar R', 'kumar@example.com', 'student123', 'Computer Science and Engineering'),
    ('Divya E', 'divya@example.com', 'student123', 'Information Technology'),
    ('Rahul M', 'rahul@example.com', 'student123', 'Electronics and Communication')
ON CONFLICT (email) DO NOTHING;

-- 2. Insert Exams
INSERT INTO exams (exam_id, exam_name, subject, total_marks, exam_date) VALUES
    (1, 'Java Internal Assessment', 'Java Programming', 100, CURRENT_DATE),
    (2, 'DBMS Model Examination', 'Database Management', 100, CURRENT_DATE),
    (3, 'Python Basics Quiz', 'Python Programming', 100, CURRENT_DATE)
ON CONFLICT (exam_id) DO UPDATE SET
    exam_name = EXCLUDED.exam_name,
    subject = EXCLUDED.subject,
    total_marks = EXCLUDED.total_marks;

SELECT setval('exams_exam_id_seq', (SELECT COALESCE(MAX(exam_id), 1) FROM exams));

-- 3. Insert Questions for Exam 1 (Java Programming)
INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
    (1, 'Which keyword is used to inherit a class in Java?', 'implements', 'extends', 'inherits', 'super', 'B'),
    (1, 'Which method is the entry point of a Java program?', 'start()', 'run()', 'main()', 'init()', 'C'),
    (1, 'Which data type stores true or false values in Java?', 'int', 'boolean', 'char', 'float', 'B'),
    (1, 'Which keyword is used to create an object in Java?', 'class', 'new', 'this', 'static', 'B'),
    (1, 'Which concept allows the same method name with different parameters?', 'Inheritance', 'Encapsulation', 'Overloading', 'Abstraction', 'C')
ON CONFLICT DO NOTHING;

-- 4. Insert Questions for Exam 2 (Database Management)
INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
    (2, 'Which SQL command is used to retrieve data from a database?', 'GET', 'FETCH', 'SELECT', 'EXTRACT', 'C'),
    (2, 'Which key uniquely identifies each record in a database table?', 'Foreign Key', 'Primary Key', 'Candidate Key', 'Super Key', 'B'),
    (2, 'What does ACID stand for in DBMS transactions?', 'Atomicity, Consistency, Isolation, Durability', 'Access, Control, Integrity, Data', 'Application, Core, Interface, Design', 'Authentication, Cipher, Identity, Defense', 'A'),
    (2, 'Which clause is used to filter records in an SQL statement?', 'ORDER BY', 'GROUP BY', 'WHERE', 'HAVING', 'C'),
    (2, 'Which SQL constraint prevents NULL values in a column?', 'UNIQUE', 'CHECK', 'NOT NULL', 'DEFAULT', 'C')
ON CONFLICT DO NOTHING;

-- 5. Insert Questions for Exam 3 (Python Programming)
INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
    (3, 'What is the correct file extension for Python files?', '.pyt', '.py', '.pt', '.python', 'B'),
    (3, 'Which function outputs text to the console in Python?', 'echo()', 'print()', 'display()', 'write()', 'B'),
    (3, 'Which data type is immutable in Python?', 'List', 'Dictionary', 'Tuple', 'Set', 'C'),
    (3, 'How do you create a function in Python?', 'function myFunc():', 'def myFunc():', 'create myFunc():', 'fn myFunc():', 'B'),
    (3, 'Which keyword is used for conditional branching in Python?', 'if', 'when', 'case', 'choose', 'A')
ON CONFLICT DO NOTHING;

SELECT setval('questions_question_id_seq', (SELECT COALESCE(MAX(question_id), 1) FROM questions));

-- 6. Insert Sample Results for Performance Analysis (using emails to match student IDs)
INSERT INTO results (student_id, exam_id, marks_obtained, status, exam_taken_at)
SELECT s.student_id, vals.exam_id, vals.marks, vals.stat, (CURRENT_TIMESTAMP - vals.ago)
FROM (VALUES
    ('arun@example.com', 1, 100, 'Passed', INTERVAL '2 days'),
    ('priya@example.com', 1, 80, 'Passed', INTERVAL '2 days'),
    ('kumar@example.com', 1, 60, 'Passed', INTERVAL '1 day'),
    ('divya@example.com', 1, 100, 'Passed', INTERVAL '1 day'),
    ('rahul@example.com', 1, 40, 'Needs Improvement', INTERVAL '1 day'),
    ('arun@example.com', 2, 80, 'Passed', INTERVAL '1 day'),
    ('priya@example.com', 2, 100, 'Passed', INTERVAL '1 day'),
    ('kumar@example.com', 2, 40, 'Needs Improvement', INTERVAL '1 day'),
    ('divya@example.com', 2, 80, 'Passed', INTERVAL '12 hours'),
    ('rahul@example.com', 2, 60, 'Passed', INTERVAL '10 hours'),
    ('arun@example.com', 3, 100, 'Passed', INTERVAL '5 hours'),
    ('priya@example.com', 3, 80, 'Passed', INTERVAL '4 hours'),
    ('divya@example.com', 3, 100, 'Passed', INTERVAL '2 hours')
) AS vals(email, exam_id, marks, stat, ago)
JOIN students s ON s.email = vals.email
JOIN exams e ON e.exam_id = vals.exam_id;

SELECT setval('results_result_id_seq', (SELECT COALESCE(MAX(result_id), 1) FROM results));
