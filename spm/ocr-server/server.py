"""Minimal Tesseract OCR HTTP server. POST /api/upload/file with multipart 'file'."""
import subprocess, tempfile, os
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/api/upload/file", methods=["POST"])
def upload_file():
    if "file" not in request.files:
        return jsonify({"error": "No file provided"}), 400
    f = request.files["file"]
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(f.filename or "img")[1]) as tmp:
        f.save(tmp)
        tmp_path = tmp.name
    try:
        result = subprocess.run(
            ["tesseract", tmp_path, "stdout", "-l", "eng"],
            capture_output=True, text=True, timeout=120
        )
        if result.returncode != 0:
            return jsonify({"result": "", "error": result.stderr.strip()}), 500
        return jsonify({"result": result.stdout})
    finally:
        os.unlink(tmp_path)

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080)
