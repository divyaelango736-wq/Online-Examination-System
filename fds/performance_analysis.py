
import os
from pathlib import Path
from urllib.parse import urlparse
import psycopg
from psycopg.rows import dict_row
from statistics import mean

# Find project folder and read backend/.env
PROJECT_DIR = Path(__file__).resolve().parent.parent
ENV_FILE = PROJECT_DIR / "backend" / ".env"

if ENV_FILE.exists():
    for line in ENV_FILE.read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, value = line.split("=", 1)
            os.environ.setdefault(key.strip(), value.strip().strip('"').strip("'"))

# Database configuration
db_url = os.getenv(
    "DB_URL",
    "jdbc:postgresql://localhost:5432/online_exam_db"
)
parsed = urlparse(db_url.replace("jdbc:", ""))

DB_CONFIG = {
    "host": parsed.hostname or "localhost",
    "port": parsed.port or 5432,
    "dbname": parsed.path.lstrip("/") or "online_exam_db",
    "user": os.getenv("DB_USER", "postgres"),
    "password": os.getenv("DB_PASSWORD", "")
}

def analyze_performance():
    query = """
        SELECT
            s.name AS student_name,
            e.exam_name,
            e.subject,
            r.marks_obtained,
            e.total_marks,
            ROUND(
                r.marks_obtained * 100.0 /
                NULLIF(e.total_marks, 0), 1
            ) AS percentage,
            r.status
        FROM results r
        JOIN students s ON s.student_id = r.student_id
        JOIN exams e ON e.exam_id = r.exam_id
        ORDER BY r.result_id DESC
    """

    try:
        with psycopg.connect(**DB_CONFIG, row_factory=dict_row) as conn:
            records = conn.execute(query).fetchall()

        print("\n===== STUDENT PERFORMANCE ANALYSIS =====")

        if not records:
            print("No examination results found.")
            return

        percentages = [float(r["percentage"]) for r in records
                       if r["percentage"] is not None]

        if not percentages:
            print("No valid marks available for analysis.")
            return

        print("\nTotal Exam Attempts:", len(records))
        print("Average Percentage:", round(mean(percentages), 2), "%")
        print("Highest Percentage:", max(percentages), "%")
        print("Lowest Percentage:", min(percentages), "%")

        passed = sum(1 for p in percentages if p >= 40)
        pass_percentage = (passed / len(percentages)) * 100

        print("Pass Count:", passed)
        print("Fail Count:", len(percentages) - passed)
        print("Pass Percentage:", round(pass_percentage, 2), "%")

        print("\n===== INDIVIDUAL RESULTS =====")
        for r in records:
            print(
                f"Student: {r['student_name']} | "
                f"Subject: {r['subject']} | "
                f"Marks: {r['marks_obtained']}/{r['total_marks']} | "
                f"Percentage: {r['percentage']}%"
            )

    except Exception as error:
        print("Database connection or analysis error:", error)


if __name__ == "__main__":
    analyze_performance()
