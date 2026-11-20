# Online Examination System

## 1. Project Overview

The Online Examination System is a web-based application developed to conduct online examinations, manage students, maintain questions, evaluate answers, calculate marks, and generate examination results.

## 2. Objectives

- Manage student details.
- Create and manage examinations.
- Store and manage questions.
- Conduct online examinations.
- Evaluate student answers.
- Generate examination results.
- Analyze student performance.

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend Development |
| PostgreSQL | Database Management |
| HTML | Webpage Structure |
| CSS | Webpage Design |
| JavaScript | Frontend Functionality |
| Python | Student Performance Analysis |

## 4. Main Features

- Student Registration
- Student Management
- Examination Management
- Question Management
- Online Examination
- Automatic Mark Calculation
- Result Management
- Student Performance Analysis
- Report Generation

## 5. Project Modules

### Java – Backend Development

Handles backend APIs, examination processing, and result management.

### DBMS – Database Management

Stores student details, exams, questions, and examination results in PostgreSQL.

### Foundation of Data Sciences

Python analyzes examination results and calculates:
- Average Marks
- Highest Marks
- Lowest Marks
- Pass Percentage

### Software Engineering

Includes software requirements, project planning, design descriptions, and software testing concepts.

## 6. Software Requirements

- Java 21
- Maven
- PostgreSQL
- Python
- Visual Studio Code
- Web Browser

## 7. Lines of Code (LOC)

The project contains approximately **2,816 lines of code**, including Java, HTML, CSS, JavaScript, and Python files.

## 8. How to Run the Project

### Step 1: Start PostgreSQL

Ensure that the PostgreSQL service is running.

### Step 2: Start Java Backend

Open the backend terminal and execute:

```bash
cd backend
mvn exec:java
```

### Step 3: Open Frontend

Open the frontend terminal and execute:

```bash
cd frontend
python -m http.server 5501
```

Open the following URL in your browser:

`http://127.0.0.1:5501/index.html`

### Step 4: Run Python Analysis

Open a new terminal in the main project folder:

```bash
python fds/performance_analysis.py
```

## 9. Database Details

- **Database Name:** online_exam_db
- **Database:** PostgreSQL

## 10. Project Conclusion

The Online Examination System integrates Java, Database Management Systems, Python, and Software Engineering concepts to provide an efficient examination and student performance management solution.