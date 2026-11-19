const API = "http://localhost:8080/api";
const PYTHON_API = "http://127.0.0.1:8001";
// STATE
let studentsList = [];
let examsList = [];
let currentQuestions = [];
let selectedAnswers = {}; // questionId -> letter
let activeExam = null;

// DOM ELEMENTS
const statusBadge = document.getElementById("statusBadge");
const apiStatusText = document.getElementById("apiStatus");

const studentSelect = document.getElementById("studentSelect");
const examSelect = document.getElementById("examSelect");
const loadQuestionsBtn = document.getElementById("loadQuestionsBtn");

const examContainer = document.getElementById("examContainer");
const activeExamTitle = document.getElementById("activeExamTitle");
const activeExamSubtitle = document.getElementById("activeExamSubtitle");
const examForm = document.getElementById("examForm");
const submitExamBtn = document.getElementById("submitExamBtn");
const progressText = document.getElementById("progressText");
const progressBarFill = document.getElementById("progressBarFill");
const resultBox = document.getElementById("resultBox");

// RESULTS TAB ELEMENTS
const refreshResultsBtn = document.getElementById("refreshResultsBtn");
const resultsTableBody = document.getElementById("resultsTableBody");
const statTotal = document.getElementById("statTotal");
const statAvg = document.getElementById("statAvg");
const statPassRate = document.getElementById("statPassRate");

// ADMIN TAB ELEMENTS
const targetExamSelect = document.getElementById("targetExamSelect");
const createExamForm = document.getElementById("createExamForm");
const addQuestionForm = document.getElementById("addQuestionForm");

// STUDENTS TAB ELEMENTS
const registerStudentForm = document.getElementById("registerStudentForm");
const studentsTableBody = document.getElementById("studentsTableBody");

// TAB NAVIGATION
document.querySelectorAll(".tab-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
        document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));

        btn.classList.add("active");
        const target = document.getElementById(btn.dataset.tab);
        if (target) target.classList.add("active");

        if (btn.dataset.tab === "resultsTab") loadResults();
        if (btn.dataset.tab === "studentsTab") loadStudentsTable();
    });
});

// UTILITIES
function escapeHtml(str) {
    return String(str ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function extractList(data) {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.value)) return data.value;
    return [];
}

// 1. HEALTH CHECK
async function checkHealth() {
    try {
        const res = await fetch(`${API}/health`);
        if (!res.ok) throw new Error("Health status not OK");
        const data = await res.json();
        statusBadge.className = "status-badge online";
        apiStatusText.textContent = `Online (${data.database})`;
    } catch (err) {
        statusBadge.className = "status-badge error";
        apiStatusText.textContent = "Backend Offline";
    }
}

// 2. LOAD INITIAL METADATA (STUDENTS & EXAMS)
async function loadInitialData() {
    await Promise.all([loadStudents(), loadExams()]);
}

async function loadStudents() {
    try {
        const res = await fetch(`${API}/students`);
        if (!res.ok) throw new Error("Failed to load students");
        studentsList = extractList(await res.json());

        studentSelect.innerHTML = `<option value="">-- Choose Student --</option>`;
        studentsList.forEach(s => {
            const opt = document.createElement("option");
            opt.value = s.studentId;
            opt.textContent = `${s.name} (${s.department || "General"})`;
            studentSelect.appendChild(opt);
        });

        loadStudentsTable();
    } catch (err) {
        console.error("loadStudents error:", err);
    }
}

async function loadExams() {
    try {
        const res = await fetch(`${API}/exams`);
        if (!res.ok) throw new Error("Failed to load exams");
        examsList = extractList(await res.json());

        examSelect.innerHTML = `<option value="">-- Choose Examination --</option>`;
        targetExamSelect.innerHTML = `<option value="">-- Choose Exam --</option>`;

        examsList.forEach(e => {
            const opt = document.createElement("option");
            opt.value = e.examId;
            opt.textContent = `${e.examName} - ${e.subject} (${e.totalMarks} Marks)`;
            examSelect.appendChild(opt);

            const optAdmin = opt.cloneNode(true);
            targetExamSelect.appendChild(optAdmin);
        });
    } catch (err) {
        console.error("loadExams error:", err);
    }
}

// 3. LOAD QUESTIONS FOR EXAMINATION
loadQuestionsBtn.addEventListener("click", async () => {
    const studentId = studentSelect.value;
    const examId = examSelect.value;

    if (!studentId) {
        alert("Please select a student first.");
        studentSelect.focus();
        return;
    }

    if (!examId) {
        alert("Please select an examination.");
        examSelect.focus();
        return;
    }

    activeExam = examsList.find(e => String(e.examId) === String(examId));
    resultBox.style.display = "none";
    examContainer.style.display = "none";
    examForm.innerHTML = `<p class="text-center">Loading examination questions...</p>`;
    loadQuestionsBtn.disabled = true;
    loadQuestionsBtn.textContent = "Loading...";

    try {
        const res = await fetch(`${API}/questions?examId=${examId}`);
        if (!res.ok) throw new Error("Could not load questions for this exam.");
        currentQuestions = extractList(await res.json());

        if (currentQuestions.length === 0) {
            alert("No questions found for this examination yet. Add questions via the Admin tab.");
            examForm.innerHTML = `<p class="text-center">No questions found for this exam.</p>`;
            return;
        }

        selectedAnswers = {};

        activeExamTitle.textContent = activeExam ? activeExam.examName : "Examination";
        activeExamSubtitle.textContent = `Subject: ${activeExam ? activeExam.subject : ""} | Total Marks: ${activeExam ? activeExam.totalMarks : 100} | Total Questions: ${currentQuestions.length}`;

        renderQuestions();
        updateProgress();

        examContainer.style.display = "block";
        examContainer.scrollIntoView({ behavior: "smooth" });

    } catch (err) {
        alert("Error: " + err.message);
    } finally {
        loadQuestionsBtn.disabled = false;
        loadQuestionsBtn.textContent = "🚀 Load Questions & Start Exam";
    }
});

function renderQuestions() {
    examForm.innerHTML = currentQuestions.map((q, idx) => `
        <div class="question-item" id="qItem-${q.questionId}">
            <div class="question-title">
                <strong>Question ${idx + 1}:</strong> ${escapeHtml(q.questionText)}
            </div>
            <div class="question-options">
                ${["A", "B", "C", "D"].map(optKey => {
                    const optText = q["option" + optKey];
                    if (!optText) return "";
                    return `
                        <label class="option-label" id="label-${q.questionId}-${optKey}">
                            <input type="radio" 
                                name="q_${q.questionId}" 
                                value="${optKey}" 
                                onchange="onAnswerSelected(${q.questionId}, '${optKey}')">
                            <span><strong>${optKey}.</strong> ${escapeHtml(optText)}</span>
                        </label>
                    `;
                }).join("")}
            </div>
        </div>
    `).join("");
}

window.onAnswerSelected = function(questionId, optionLetter) {
    selectedAnswers[questionId] = optionLetter;

    // Highlight selected label
    ["A", "B", "C", "D"].forEach(l => {
        const el = document.getElementById(`label-${questionId}-${l}`);
        if (el) el.classList.toggle("selected", l === optionLetter);
    });

    const itemEl = document.getElementById(`qItem-${questionId}`);
    if (itemEl) itemEl.classList.add("answered");

    updateProgress();
};

function updateProgress() {
    const answeredCount = Object.keys(selectedAnswers).length;
    const totalCount = currentQuestions.length;
    const pct = totalCount > 0 ? (answeredCount / totalCount) * 100 : 0;

    progressText.textContent = `${answeredCount} of ${totalCount} answered`;
    progressBarFill.style.width = `${pct}%`;

    submitExamBtn.disabled = answeredCount !== totalCount;
}

// 4. SUBMIT EXAM
submitExamBtn.addEventListener("click", async (e) => {
    e.preventDefault();

    const studentId = parseInt(studentSelect.value, 10);
    const examId = parseInt(examSelect.value, 10);

    const answeredCount = Object.keys(selectedAnswers).length;
    if (answeredCount < currentQuestions.length) {
        alert(`Please answer all ${currentQuestions.length} questions before submitting.`);
        return;
    }

    if (!confirm("Are you sure you want to submit your examination? Once submitted, it will be automatically evaluated.")) {
        return;
    }

    const answersPayload = currentQuestions.map(q => ({
        questionId: q.questionId,
        selectedAnswer: selectedAnswers[q.questionId] || ""
    }));

    submitExamBtn.disabled = true;
    submitExamBtn.textContent = "Evaluating...";

    try {
        const res = await fetch(`${API}/submit-exam`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                studentId,
                examId,
                answers: answersPayload
            })
        });

        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.error || "Submission failed");
        }

        // Render result card
        const isPass = Number(data.percentage) >= 40;
        resultBox.innerHTML = `
            <div class="result-banner ${isPass ? 'passed' : 'failed'}">
                <h2>${isPass ? '🎉 Examination Completed - Passed!' : '⚠️ Examination Completed'}</h2>
                <p>Candidate: <strong>${escapeHtml(data.studentName || 'Student')}</strong> | Exam: <strong>${escapeHtml(data.examName || 'Assessment')}</strong></p>
                <div class="result-score">${escapeHtml(data.marksObtained)} / ${escapeHtml(data.totalMarks)}</div>
                <p><strong>${escapeHtml(data.percentage)}%</strong> &bull; Status: <span class="badge ${isPass ? 'badge-success' : 'badge-danger'}">${escapeHtml(data.status)}</span></p>

                <div class="result-details-grid">
                    <div class="result-detail-item">
                        <div class="lbl">Total Questions</div>
                        <div class="val">${escapeHtml(data.totalQuestions)}</div>
                    </div>
                    <div class="result-detail-item">
                        <div class="lbl">Correct Answers</div>
                        <div class="val">${escapeHtml(data.correctAnswers)}</div>
                    </div>
                    <div class="result-detail-item">
                        <div class="lbl">Accuracy</div>
                        <div class="val">${escapeHtml(data.percentage)}%</div>
                    </div>
                    <div class="result-detail-item">
                        <div class="lbl">Result ID</div>
                        <div class="val">#${escapeHtml(data.resultId)}</div>
                    </div>
                </div>
            </div>
        `;

        resultBox.style.display = "block";
        examContainer.style.display = "none";
        resultBox.scrollIntoView({ behavior: "smooth" });

    } catch (err) {
        alert("Submission Error: " + err.message);
        submitExamBtn.disabled = false;
        submitExamBtn.textContent = "Submit Examination";
    }
});

// PERFORMANCE CHARTS
let studentMarksChart = null;
let passFailChart = null;
let averageChart = null;

function updatePerformanceCharts(results) {

    // 1. Student-wise Marks Graph
    const studentLabels = results.map(r =>
        `${r.studentName} - ${r.examName}`
    );

    const marks = results.map(r =>
        Number(r.marksObtained)
    );

    if (studentMarksChart) studentMarksChart.destroy();

    studentMarksChart = new Chart(
        document.getElementById("studentMarksChart"),
        {
            type: "bar",
            data: {
                labels: studentLabels,
                datasets: [{
                    label: "Marks Obtained",
                    data: marks
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: { beginAtZero: true }
                }
            }
        }
    );

    // 2. Pass vs Fail Graph
    const passed = results.filter(r =>
        Number(r.percentage) >= 40
    ).length;

    const failed = results.length - passed;

    if (passFailChart) passFailChart.destroy();

    passFailChart = new Chart(
        document.getElementById("passFailChart"),
        {
            type: "doughnut",
            data: {
                labels: ["Passed", "Failed"],
                datasets: [{
                    data: [passed, failed]
                }]
            },
            options: {
                responsive: true
            }
        }
    );

    // 3. Average Performance Graph
    const average = results.length
        ? results.reduce((sum, r) =>
            sum + Number(r.percentage), 0
          ) / results.length
        : 0;

    if (averageChart) averageChart.destroy();

    averageChart = new Chart(
        document.getElementById("averageChart"),
        {
            type: "bar",
            data: {
                labels: ["Average Percentage"],
                datasets: [{
                    label: "Average Performance",
                    data: [average]
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100
                    }
                }
            }
        }
    );
}
// 5. LOAD RESULTS & PERFORMANCE
async function loadResults() {
    resultsTableBody.innerHTML =
        `<tr><td colspan="9" class="text-center">Loading results...</td></tr>`;

    try {
        // Get examination results from Java
        const javaResponse = await fetch(`${API}/results`);

        if (!javaResponse.ok) {
            throw new Error("Could not fetch examination results");
        }

        const results = extractList(await javaResponse.json());
        updatePerformanceCharts(results);
        // Get performance analysis from Python
        let pythonData = null;

        try {
            const pythonResponse = await fetch(
                `${PYTHON_API}/api/performance`
            );

            if (!pythonResponse.ok) {
                throw new Error("Python API unavailable");
            }

            pythonData = await pythonResponse.json();

            if (!pythonData.success) {
                throw new Error(pythonData.message);
            }

            const performance = pythonData.performance;

            // Display Python analytics
            statTotal.textContent = performance.total_attempts;
            statAvg.textContent =
                `${performance.average_percentage}%`;

            const passRate = performance.total_attempts > 0
                ? (performance.pass_count /
                   performance.total_attempts) * 100
                : 0;

            statPassRate.textContent =
                `${passRate.toFixed(1)}%`;

        } catch (pythonError) {
            console.error("Python analysis error:", pythonError);

            statTotal.textContent = "Unavailable";
            statAvg.textContent = "Unavailable";
            statPassRate.textContent = "Unavailable";
        }

        // No examination results
        if (results.length === 0) {
            resultsTableBody.innerHTML =
                `<tr><td colspan="9" class="text-center">
                No examination results recorded yet.
                </td></tr>`;

            if (!pythonData) {
                statTotal.textContent = "0";
                statAvg.textContent = "0%";
                statPassRate.textContent = "0%";
            }

            return;
        }

        // Display examination results table
        resultsTableBody.innerHTML = results.map(r => {

            const isPass = Number(r.percentage) >= 40;

            const dateStr = r.examTakenAt
                ? r.examTakenAt.split(".")[0]
                : "-";

            return `
                <tr>
                    <td><strong>#${escapeHtml(r.resultId)}</strong></td>
                    <td>${escapeHtml(r.studentName)}</td>
                    <td>${escapeHtml(r.department || "-")}</td>
                    <td>${escapeHtml(r.examName)}</td>
                    <td>${escapeHtml(r.subject)}</td>
                    <td>
                        <strong>${escapeHtml(r.marksObtained)}</strong>
                        / ${escapeHtml(r.totalMarks)}
                    </td>
                    <td>${escapeHtml(r.percentage)}%</td>
                    <td>
                        <span class="badge ${
                            isPass ? 'badge-success' : 'badge-danger'
                        }">
                            ${isPass ? 'Passed' : 'Failed'}
                        </span>
                    </td>
                    <td>${escapeHtml(dateStr)}</td>
                </tr>
            `;
        }).join("");

    } catch (err) {
        console.error("Results error:", err);

        resultsTableBody.innerHTML =
            `<tr><td colspan="9" class="text-center text-danger">
            Failed to load results: ${escapeHtml(err.message)}
            </td></tr>`;
    }
}

// 6. ADMIN: CREATE EXAM
createExamForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const examName = document.getElementById("newExamName").value.trim();
    const subject = document.getElementById("newSubject").value.trim();
    const totalMarks = parseInt(document.getElementById("newTotalMarks").value, 10);
    const examDate = document.getElementById("newExamDate").value;

    try {
        const res = await fetch(`${API}/exams`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ examName, subject, totalMarks, examDate })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || "Failed to create exam");

        alert("Examination created successfully!");
        createExamForm.reset();
        await loadExams();
    } catch (err) {
        alert("Error creating exam: " + err.message);
    }
});

// Set default date for new exam to today
const today = new Date().toISOString().split("T")[0];
const newExamDateEl = document.getElementById("newExamDate");
if (newExamDateEl) newExamDateEl.value = today;

// 7. ADMIN: ADD QUESTION
addQuestionForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const examId = parseInt(targetExamSelect.value, 10);
    const questionText = document.getElementById("newQuestionText").value.trim();
    const optionA = document.getElementById("newOptionA").value.trim();
    const optionB = document.getElementById("newOptionB").value.trim();
    const optionC = document.getElementById("newOptionC").value.trim();
    const optionD = document.getElementById("newOptionD").value.trim();
    const correctAnswer = document.getElementById("newCorrectAnswer").value;

    if (!examId) {
        alert("Please select target exam.");
        return;
    }

    try {
        const res = await fetch(`${API}/questions`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                examId,
                questionText,
                optionA,
                optionB,
                optionC,
                optionD,
                correctAnswer
            })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || "Failed to add question");

        alert("Question added successfully!");
        addQuestionForm.reset();
    } catch (err) {
        alert("Error adding question: " + err.message);
    }
});

// 8. STUDENT REGISTRATION
registerStudentForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("studentName").value.trim();
    const email = document.getElementById("studentEmail").value.trim();
    const department = document.getElementById("studentDept").value;

    try {
        const res = await fetch(`${API}/students`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email, department, password: "student123" })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || "Failed to register student");

        alert("Student registered successfully!");
        registerStudentForm.reset();
        await loadStudents();
    } catch (err) {
        alert("Error registering student: " + err.message);
    }
});

function loadStudentsTable() {
    if (!studentsTableBody) return;
    if (studentsList.length === 0) {
        studentsTableBody.innerHTML = `<tr><td colspan="4" class="text-center">No students registered yet.</td></tr>`;
        return;
    }

    studentsTableBody.innerHTML = studentsList.map(s => `
        <tr>
            <td><strong>#${escapeHtml(s.studentId)}</strong></td>
            <td>${escapeHtml(s.name)}</td>
            <td>${escapeHtml(s.email)}</td>
            <td>${escapeHtml(s.department || "General")}</td>
        </tr>
    `).join("");
}

// INITIALIZE
checkHealth();
loadInitialData();
setInterval(checkHealth, 10000);
// DOWNLOAD STUDENT RESULTS AS CSV
async function downloadReport() {
    const response = await fetch(`${API}/results`);
    const results = extractList(await response.json());

    const headers = [
        "Student",
        "Department",
        "Exam Name",
        "Subject",
        "Marks",
        "Percentage",
        "Status"
    ];

    const rows = results.map(r => [
        r.studentName,
        r.department,
        r.examName,
        r.subject,
        r.marksObtained,
        r.percentage,
        r.status
    ]);

    const csv = [headers, ...rows]
        .map(row => row.join(","))
        .join("\n");

    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = "Student_Performance_Report.csv";
    link.click();

    URL.revokeObjectURL(url);
}

document.getElementById("downloadReportBtn")
    .addEventListener("click", downloadReport);
