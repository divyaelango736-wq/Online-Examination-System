# SOFTWARE TESTING DOCUMENT

**Project:** Online Examination System

## 1. Introduction

Software testing is the process of checking whether an application works correctly and meets the expected requirements.

The Online Examination System is developed using Java, PostgreSQL, HTML, CSS and JavaScript. It allows students to register, view available exams, attend examinations and view their results.

This document describes the testing activities performed on the Online Examination System.

## 2. Objectives of Testing

- To verify that all modules work correctly.
- To identify errors and bugs in the application.
- To check communication between the frontend, backend and database.
- To verify student registration and examination processes.
- To ensure that examination results are calculated correctly.

## 3. Types of Testing

### 3.1 Unit Testing

Unit testing checks individual components or functions of the application separately.

### 3.2 Integration Testing

Integration testing checks whether different modules work together correctly, such as the Java backend and PostgreSQL database.

### 3.3 System Testing

System testing checks the complete application to verify that all modules work together as expected.

### 3.4 Validation Testing

Validation testing checks whether the application meets the specified user requirements.

## 4. Test Cases

The following test cases were performed to verify the major modules of the Online Examination System.

| Test ID | Test Name | Module | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC01 | Student Registration | Registration | Student registration should be successful | Registration successful. Registration ID: 3 | PASS |
| TC02 | View Students | Student Management | Student details should be displayed | 7 student records retrieved successfully | PASS |
| TC03 | View Exams | Exam Management | Available exams should be displayed | 4 exam records retrieved successfully | PASS |
| TC04 | Load Questions | Question Management | Questions for the selected exam should be displayed | 5 question records retrieved successfully | PASS |
| TC05 | Submit Exam | Examination | Answers should be evaluated and the result should be generated | 3 correct answers, 100/100 marks, 100%, Passed | PASS |
| TC06 | View Results | Result Management | Student examination results should be displayed | 32 result records retrieved successfully | PASS |
| TC07 | Performance Analysis | Python Analysis | Performance analysis should execute successfully | Python performance analysis executed successfully | PASS |

## 5. Detailed API Testing

API testing was performed to verify communication between the frontend and backend.

### 5.1 GET API Testing

The following GET APIs were tested successfully.

| Test ID | API Endpoint | Expected Result | Actual Result | Status |
|---|---|---|---|---|
| API01 | `/api/health` | HTTP 200 | HTTP 200 | PASS |
| API02 | `/api/students` | HTTP 200 | HTTP 200 | PASS |
| API03 | `/api/exams` | HTTP 200 | HTTP 200 | PASS |
| API04 | `/api/questions?examId=1` | HTTP 200 | HTTP 200 | PASS |
| API05 | `/api/results` | HTTP 200 | HTTP 200 | PASS |

### 5.2 Student Registration API

**Method:** POST

**Endpoint:** `/api/registrations`

**Input:**

```json
{
  "studentId": 7,
  "examId": 4
}
```

**Expected Output:**

The student should be registered successfully for the selected examination.

**Actual Output:**

Registration was successful and the response returned Registration ID 3.

**Status:** PASS

### 5.3 Submit Examination API

**Method:** POST

**Endpoint:** `/api/submit-exam`

**Input:**

```json
{
  "studentId": 7,
  "examId": 1,
  "answers": [
    {
      "questionId": 5,
      "selectedAnswer": "B"
    },
    {
      "questionId": 6,
      "selectedAnswer": "C"
    },
    {
      "questionId": 7,
      "selectedAnswer": "C"
    }
  ]
}
```

**Expected Output:**

The submitted answers should be evaluated and the examination result should be generated.

**Actual Output:**

- Total Questions: 3
- Correct Answers: 3
- Marks: 100/100
- Percentage: 100%
- Result: Passed

**Status:** PASS

**Note:** The successful POST responses were verified. HTTP 201 is based on the backend response implementation and was not independently recorded as an observed status code.

## 6. Testing Tools and Technologies

The following tools and technologies were used during testing.

| Tool / Technology | Purpose |
|---|---|
| Java 21 | Backend application |
| Maven | Build and execution |
| PostgreSQL | Database testing |
| Postman / API testing | API verification |
| Web Browser | Frontend testing |
| Python | Performance analysis |
| Visual Studio Code | Code editing and project development |

## 7. Test Coverage

The following major modules were covered during testing.

- Student Registration
- Student Management
- Exam Management
- Question Management
- Examination Submission
- Result Management
- Performance Analysis

## 8. Overall Test Result

| Total Main Test Cases | Passed | Failed |
|---:|---:|---:|
| 7 | 7 | 0 |

<text></text>

## 9. Conclusion

The Online Examination System was tested successfully. All seven main test cases passed without any failures.

The GET API endpoints returned successful HTTP 200 responses. Student registration and examination submission were also verified successfully. The system generated the expected examination result, and Python performance analysis was executed successfully.

Therefore, the application satisfies the tested functional requirements.
