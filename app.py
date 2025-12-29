import os
from flask import Flask, request, jsonify
from dotenv import load_dotenv
import google.generativeai as genai

# --- Configuration ---
app = Flask(__name__)

# Load environment variables from .env file
load_dotenv()

# Load API key
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

if not GEMINI_API_KEY:
    raise ValueError("❌ GEMINI_API_KEY not found. Please check your .env file.")

# Configure the Gemini API client
genai.configure(api_key=GEMINI_API_KEY)

# --- AI Logic ---
def get_ai_suggestion(symptom_text: str, language: str) -> str:
    """
    Generates a health suggestion in a specified language.
    """
    try:
        # ✅ Use a valid model name (confirmed working)
        model = genai.GenerativeModel('models/gemini-2.5-flash')



        # ✅ Construct the prompt clearly
        prompt = (
            f"Please provide your response in {language}. "
            "You are a helpful AI health assistant for the MediConnect AI app. "
            "Your role is to analyze symptoms for users in rural and urban India. "
            "Provide a possible cause in simple, clear language. "
            "Then, suggest 2–3 simple, actionable home-care tips. "
            "IMPORTANT: Always end your entire response with this disclaimer on a new line: "
            "'Disclaimer: This is an AI-generated suggestion and not a substitute for professional medical advice. Please consult a doctor.'\n\n"
            f"The user's symptom is: '{symptom_text}'."
        )

        # ✅ Call Gemini
        response = model.generate_content(prompt)

        # ✅ Handle response safely
        if hasattr(response, "text") and response.text:
            return response.text.strip()
        else:
            return "Sorry, I couldn’t generate a response at the moment."

    except Exception as e:
        print(f"⚠️ An error occurred with the Gemini API: {e}")
        return "Sorry, the AI assistant is currently unavailable. Please check your connection and try again later."

# --- API Endpoint ---
@app.route("/check_symptom", methods=["POST"])
def check_symptom_endpoint():
    """
    API endpoint that receives a symptom and language.
    """
    data = request.get_json(silent=True)

    if not data or "symptom" not in data:
        return jsonify({"error": "Symptom not provided."}), 400

    symptom = data["symptom"]
    language = data.get("language", "English")

    suggestion = get_ai_suggestion(symptom, language)

    return jsonify({"suggestion": suggestion})

# --- Run Server ---
if __name__ == "__main__":
    # You can print available models at startup (optional debug)
    try:
        print("✅ Available Gemini models:")
        for m in genai.list_models():
            if "generateContent" in m.supported_generation_methods:
                print(" -", m.name)
    except Exception as e:
        print("⚠️ Could not list models:", e)

    # Run the Flask server
    app.run(host="0.0.0.0", port=5000, debug=True)
