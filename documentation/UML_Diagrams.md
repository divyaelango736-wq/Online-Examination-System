
# UML DIAGRAMS
## Online Examination System

## 1. Use Case Diagram

Actors:
- Admin
- Student

Admin Use Cases:
- Manage Students
- Create Examination
- Add Questions
- View Results

Student Use Cases:
- Register
- View Examinations
- Attend Examination
- Submit Answers
- View Results

## 2. Class Diagram

Main Classes:
- Student
- Exam
- Question
- Result
- Database

Relationships:
- One Student can attend many Exams.
- One Exam contains many Questions.
- One Student can have many Results.
- Each Result belongs to one Exam.

## 3. Activity Diagram

Start
  |
Student Registration
  |
Select Examination
  |
Load Questions
  |
Answer Questions
  |
Submit Examination
  |
Calculate Marks
  |
Store Results
  |
Display Results
  |
End

## 4. Sequence Diagram

Student -> Frontend: Select Exam
Frontend -> Java Backend: Request Questions
Java Backend -> PostgreSQL: Fetch Questions
PostgreSQL -> Java Backend: Return Questions
Java Backend -> Frontend: Display Questions
Student -> Frontend: Submit Answers
Frontend -> Java Backend: Submit Answers
Java Backend -> PostgreSQL: Save Result
Java Backend -> Frontend: Display Result
