
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import dotenv_values
from pathlib import Path
from urllib.parse import urlsplit
import psycopg
from psycopg.rows import dict_row

app = FastAPI(title="Student Performance Analysis API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Read existing backend database settings
project_folder = Path(__file__).resolve().parent.parent
config = dotenv_values(project_folder / "backend" / ".env")

def get_connection():
    db_url = config.get("DB_URL", "")
    parsed = urlsplit(db_url.replace("jdbc:", "", 1))

    return psycopg.connect(
        host=parsed.hostname or "localhost",
        port=parsed.port or 5432,
        dbname=parsed.path.lstrip("/") or "online_exam_db",
        user=config.get("DB_USER", "postgres"),
        password=config.get("DB_PASSWORD", "")
    )

@app.get("/")
def home():
    return {"message": "Python Performance API is running"}

@app.get("/api/performance")
def get_performance():
    try:
        with get_connection() as conn:
            with conn.cursor(row_factory=dict_row) as cur:
                cur.execute("""
                    SELECT
                        COUNT(*) AS total_attempts,
                        COALESCE(ROUND(AVG(percentage)::numeric, 2), 0)
                            AS average_percentage,
                        COALESCE(MAX(percentage), 0) AS highest_percentage,
                        COALESCE(MIN(percentage), 0) AS lowest_percentage,
                        COUNT(*) FILTER (WHERE percentage >= 40) AS pass_count,
                        COUNT(*) FILTER (WHERE percentage < 40) AS fail_count
                    FROM (
                        SELECT
                            r.marks_obtained * 100.0 /
                            NULLIF(e.total_marks, 0) AS percentage
                        FROM results r
                        JOIN exams e ON r.exam_id = e.exam_id
                    ) AS exam_data
                """)

                return {
                    "success": True,
                    "performance": cur.fetchone()
                }

    except Exception as error:
        return {
            "success": False,
            "message": str(error)
        }
