\# SOFTWARE TESTING DOCUMENT


**Project:** Online Examination System


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


| Test ID | Test Name | Module | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC01 | Student Registration | Student Management | Registration successful | Registration ID 3 created for student 7 and exam 4 | PASS |
| TC02 | View Students | Student Management | Student details displayed | 7 student records retrieved; HTTP 200 | PASS |
| TC03 | View Exams | Exam Management | Exam list displayed | 4 exam records retrieved; HTTP 200 | PASS |
| TC04 | Load Questions | Question Management | Exam questions displayed | 5 question records retrieved; HTTP 200 | PASS |
| TC05 | Submit Exam | Examination | Marks calculated | 3 correct; 100/100; Passed | PASS |
| TC06 | View Results | Result Management | Results displayed | 32 result records retrieved; HTTP 200 | PASS |
| TC07 | Performance Analysis | Python | Performance report generated | Python performance analysis executed successfully | PASS |
## 5. Detailed API Testing


The Java backend APIs were tested using PowerShell.


### 5.1 GET API Testing


| API Endpoint | Expected HTTP Status | Actual HTTP Status | Result |

|---|---:|---:|---|

| `/api/health` | 200 | 200 | PASS |

| `/api/students` | 200 | 200 | PASS |

| `/api/exams` | 200 | 200 | PASS |

| `/api/questions?examId=1` | 200 | 200 | PASS |

| `/api/results` | 200 | 200 | PASS |


### 5.2 Student Registration API


**Method:** POST

**Endpoint:** `/api/registrations`


**Input:**

```json

{

&#x20; "studentId": 7,

&#x20; "examId": 4

}

```


**Expected Output:** Student registered successfully.


**Actual Output:** Student registered successfully. Registration ID: 3.


**HTTP Status:** 201 Created (as defined by the backend success response).


**Result:** PASS


### 5.3 Submit Examination API


**Method:** POST

**Endpoint:** `/api/submit-exam`


**Input:**

```json

{

&#x20; "studentId": 7,

&#x20; "examId": 1,

&#x20; "answers": [

&#x20;   {

&#x20;     "questionId": 5,

&#x20;     "selectedAnswer": "B"

&#x20;   },

&#x20;   {

&#x20;     "questionId": 6,

&#x20;     "selectedAnswer": "C"

&#x20;   },

&#x20;   {

&#x20;     "questionId": 7,

&#x20;     "selectedAnswer": "C"

&#x20;   }

&#x20; ]

}

```


**Expected Output:** Answers should be evaluated and marks calculated.


**Actual Output:**

- Total Questions: 3

- Correct Answers: 3

- Marks Obtained: 100

- Total Marks: 100

- Percentage: 100%

- Status: Passed


**HTTP Status:** 201 Created (as defined by the backend success response).


**Result:** PASS


*Note: The POST success status codes above are based on the backend response implementation. The successful response bodies were observed during testing.*


## 6. Testing Tools


- Manual Testing

- Postman / PowerShell API testing

- PostgreSQL queries

- Python execution

- Browser-based frontend testing


## 7. Test Coverage


The test cases cover the following major areas:


- Student registration and student listing

- Exam listing

- Question loading

- Examination submission

- Result retrieval

- Python performance analysis

- API health and HTTP response verification


## 8. Overall Test Result


All seven main test cases listed in this report are recorded with PASS status. Additional API checks also returned successful responses.


| Total Main Test Cases | Passed | Failed |

|---:|---:|---:|

| 7 | 7 | 0 |


**Additional API Verification:** 5 GET endpoints returned HTTP 200 successfully. Student registration and examination submission also returned successful response bodies.


## 9. Conclusion


The major modules of the Online Examination System were tested. Student registration, student listing, exam listing, question loading, examination submission, result retrieval and Python performance analysis were verified. All seven main test cases recorded in this report passed. The five tested GET API endpoints also returned HTTP 200 responses.




