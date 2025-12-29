# MediConnect AI – Backend

This folder contains the **Python backend** for the MediConnect AI project.

The backend is responsible for **AI-assisted logic and APIs** used by the Android
application to support frontline healthcare workers such as ASHA, ANM, and GNM.

---

## 🎯 Purpose

- Process symptom inputs sent from the Android app
- Perform AI-based analysis and risk categorization
- Support decision-making for healthcare workers
- Keep backend logic separate from frontend UI

---

## ✨ Features

- AI-based symptom analysis
- Risk classification (Low / Medium / High)
- Lightweight REST APIs consumed by the Android app
- Designed to work with low-resource and rural environments

---

## 🛠️ Tech Stack

- Python
- Flask
- AI-based rule-based / ML logic

---

## 📂 Folder Structure

backend/
├── app.py # Main backend entry point
├── README.md # Backend documentation ## ▶️ How to Run (Local)

## ▶️ How to Run (Local)

```bash
pip install -r requirements.txt
python app.py

## 🔄 Android Integration

The Android application communicates with the backend through REST APIs to obtain
AI-driven symptom insights and risk assessments, enabling decision support for
frontline healthcare workers.

## 🔒 Security Notes

- Environment configuration files (`.env`) are excluded from the repository  
- Virtual environments (`.venv`) are not committed to version control  
- No sensitive credentials or secrets are stored in GitHub
