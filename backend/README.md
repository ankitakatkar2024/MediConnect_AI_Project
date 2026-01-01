MediConnect AI – Backend (Python / Flask)

This folder contains the backend service developed as part of the MediConnect AI academic project.
The backend is designed to support the Android application by providing basic decision-support logic and REST APIs.

The implementation focuses on learning backend development concepts and simple rule-based / ML-assisted processing, rather than production-level healthcare systems.

🎯 Purpose

The backend was developed to:

Receive symptom-related inputs from the Android application

Apply basic rule-based or simple ML logic to categorize health risk levels

Return risk indicators (Low / Medium / High) for follow-up prioritization

Keep backend processing separate from the mobile user interface

✨ Key Functionalities

Symptom-based risk categorization using rule-based / basic ML logic

REST APIs for communication with the Android application

Lightweight design suitable for academic demonstration

Simple and readable code structure for ease of understanding

🛠️ Technology Stack

Python

Flask (REST API development)

Rule-based / basic ML logic (educational scope)

📂 Folder Structure
backend/
├── app.py        # Flask application entry point
├── README.md     # Backend documentation

▶️ Running the Backend (Local)

To run the backend locally for testing:

pip install -r requirements.txt
python app.py

🔄 Android Application Integration

The Android application communicates with this backend through REST APIs to retrieve basic risk indicators based on the symptoms recorded during field visits.
The backend is invoked only when internet connectivity is available, aligning with the offline-first design of the mobile app.

🔒 Security & Code Practices (Academic Scope)

Environment configuration files (.env) are excluded from version control

Virtual environments (.venv) are not committed

No sensitive credentials or personal health data are stored in the repository

Project intended strictly for learning and demonstration purposes


✅ Final Note

This backend is part of an academic prototype and demonstrates how backend services can support mobile applications with basic decision-support logic in low-connectivity environments.
