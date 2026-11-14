"""Online Examination Management and Student Performance Analysis System
Scenario 28 | Register Number: 28
Python Data Analysis and Visualization Module
"""

import os
import sys
import warnings
import pandas as pd
import matplotlib.pyplot as plt

warnings.filterwarnings("ignore", category=UserWarning)


def get_db_password():
    pwd = os.getenv("PGPASSWORD") or os.getenv("DB_PASSWORD")
    if pwd:
        return pwd
    for p in [".env", "../backend/.env", "backend/.env"]:
        if os.path.exists(p):
            with open(p, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line.startswith("DB_PASSWORD="):
                        return line.split("=", 1)[1].strip()
    return "D"


DB_CONFIG = {
    "host": os.getenv("PGHOST", "localhost"),
    "port": os.getenv("PGPORT", "5432"),
    "dbname": os.getenv("PGDATABASE", "online_exam_db"),
    "user": os.getenv("PGUSER", "postgres"),
    "password": get_db_password(),
}


def main():
    print("=" * 65)
    print(" ONLINE EXAMINATION SYSTEM - STUDENT PERFORMANCE ANALYSIS ")
    print("=" * 65)

    try:
        import psycopg
    except ImportError:
        print("Error: psycopg module not found. Run 'pip install -r requirements.txt'")
        sys.exit(1)

    try:
        with psycopg.connect(**DB_CONFIG) as conn:
            query = """
                SELECT 
                    s.student_id,
                    s.name AS student_name,
                    s.department,
                    e.exam_id,
                    e.exam_name,
                    e.subject,
                    e.total_marks AS max_marks,
                    r.marks_obtained,
                    ROUND((r.marks_obtained * 100.0 / NULLIF(e.total_marks, 0)), 2) AS percentage,
                    r.status,
                    r.exam_taken_at
                FROM results r
                JOIN students s ON s.student_id = r.student_id
                JOIN exams e ON e.exam_id = r.exam_id
                ORDER BY r.result_id;
            """
            df = pd.read_sql(query, conn)

        if df.empty:
            print("\n[!] No evaluated results found in database.")
            print("Please take an examination through the frontend first.")
            return

        os.makedirs("output", exist_ok=True)

        print(f"\n[+] Successfully retrieved {len(df)} examination record(s).")
        print("\n--- Summary by Subject ---")
        subject_stats = df.groupby("subject")["percentage"].agg(
            Attempts="count",
            Average_Score="mean",
            Highest_Score="max",
            Lowest_Score="min"
        ).round(2)
        print(subject_stats)

        print("\n--- Summary by Student ---")
        student_stats = df.groupby(["student_name", "department"])["percentage"].agg(
            Exams_Taken="count",
            Average_Percentage="mean"
        ).round(2).sort_values(by="Average_Percentage", ascending=False)
        print(student_stats)

        # Plot 1: Student Average Performance
        plt.figure(figsize=(9, 5))
        student_avg = df.groupby("student_name")["percentage"].mean().sort_values()
        bars = plt.barh(student_avg.index, student_avg.values, color="#3b82f6", edgecolor="#1d4ed8")
        plt.title("Student Performance Analysis - Average Percentage", fontsize=14, fontweight="bold")
        plt.xlabel("Average Percentage (%)", fontsize=11)
        plt.ylabel("Student", fontsize=11)
        plt.xlim(0, 105)
        for bar in bars:
            plt.text(bar.get_width() + 1.5, bar.get_y() + bar.get_height() / 2,
                     f"{bar.get_width():.1f}%", va="center", fontsize=10, fontweight="bold")
        plt.tight_layout()
        plt.savefig("output/student_performance.png", dpi=150)
        plt.close()
        print("\n[+] Chart saved: output/student_performance.png")

        # Plot 2: Subject-wise Performance Comparison
        plt.figure(figsize=(8, 5))
        subj_avg = df.groupby("subject")["percentage"].mean()
        plt.bar(subj_avg.index, subj_avg.values, color=["#10b981", "#6366f1", "#f59e0b"], edgecolor="#1f2937")
        plt.title("Subject-wise Average Examination Score", fontsize=14, fontweight="bold")
        plt.xlabel("Subject", fontsize=11)
        plt.ylabel("Average Percentage (%)", fontsize=11)
        plt.ylim(0, 110)
        for i, val in enumerate(subj_avg.values):
            plt.text(i, val + 2, f"{val:.1f}%", ha="center", fontsize=10, fontweight="bold")
        plt.tight_layout()
        plt.savefig("output/subject_performance.png", dpi=150)
        plt.close()
        print("[+] Chart saved: output/subject_performance.png")

        # Plot 3: Result Status Distribution (Passed vs Needs Improvement)
        plt.figure(figsize=(6, 5))
        status_counts = df["status"].value_counts()
        colors = ["#10b981" if s == "Passed" else "#ef4444" for s in status_counts.index]
        plt.pie(status_counts.values, labels=status_counts.index, autopct="%1.1f%%",
                colors=colors, startangle=140, explode=[0.05] * len(status_counts),
                textprops={"fontsize": 11, "fontweight": "bold"})
        plt.title("Examination Result Status Distribution", fontsize=14, fontweight="bold")
        plt.tight_layout()
        plt.savefig("output/status_distribution.png", dpi=150)
        plt.close()
        print("[+] Chart saved: output/status_distribution.png")

        print("\n" + "=" * 65)
        print(" ANALYSIS COMPLETE! All charts saved in python-analysis/output/ ")
        print("=" * 65)

    except Exception as exc:
        print("\n[!] Analysis failed. Please check PostgreSQL connection and credentials.")
        print(f"Details: {exc}")


if __name__ == "__main__":
    main()
