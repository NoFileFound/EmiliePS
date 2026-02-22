from flask import Flask, send_file, abort
from pathlib import Path

app = Flask(__name__)
FILE_TO_SERVE = Path("nametranslations.txt")

@app.route("/Translate", methods=["GET"])
def serve_translate():
    return send_file(FILE_TO_SERVE, as_attachment=False)

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8100)