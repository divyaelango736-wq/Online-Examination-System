# SOFTWARE TESTING DOCUMENT

## Project: Online Examination System

## 1. Introduction
This document describes the testing activities carried out for the Online Examination System. The purpose of testing is to check whether the application modules work as expected and satisfy the project requirements.

## 2. Objectives of Testing
- Verify student registration and student information retrieval.
- Check exam listing and question loading.
- Verify examination submission and marks calculation.
- Check result retrieval.
- Verify Python-based performance analysis.
- Identify errors and improve application reliability.

## 3. Types of Testing

### 3.1 Unit Testing
Individual modules such as student registration, exam creation and result calculation are checked independently.

### 3.2 Integration Testing
The interaction between the Java backend, PostgreSQL database and Python analysis module is checked.

### 3.3 System Testing
The complete application is checked to ensure that its major modules work together.

### 3.4 Validation Testing
The application functionality is reviewed against the expected project requirements.

## 4. Test Cases and Results

| Test ID | Module | Test Scenario | Expected Result | Status |
|---|---|---|---|---|
| TC01 | Student Management | Student registration | Student registered successfully | PASS |
| TC02 | Student Management | View student list | Student details displayed | PASS |
| TC03 | Exam Management | View exams | Available exam list displayed | PASS |
| TC04 | Question Management | Load questions | Exam questions displayed | PASS |
| TC05 | Examination | Submit examination | Answers submitted and marks calculated | PASS |
| TC06 | Result Management | View results | Student examination results displayed | PASS |
| TC07 | Performance Analysis | Run Python analysis | Performance report generated | PASS |

## 5. Testing Tools
- Manual Testing
- Postman / PowerShell API testing
- PostgreSQL queries
- Python execution
- Browser-based frontend testing

## 6. Test Coverage
The test cases listed in this document cover the following major areas:
- Student registration and student listing
- Exam listing
- Question loading
- Examination submission
- Result retrieval
- Python performance analysis

## 7. Overall Test Result
All seven test cases listed in this report are recorded with PASS status.

| Total Test Cases | Passed | Failed |
|---:|---:|---:|
| 7 | 7 | 0 |

## 8. Conclusion
The major modules of the Online Examination System were tested. Student registration, examination APIs, result retrieval and Python performance analysis were verified successfully. The test results recorded in this document indicate that all seven listed test cases passed.
