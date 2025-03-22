from flask import Flask, request, jsonify, redirect
import jwt
import time
import sqlite3
import secrets

app = Flask(__name__)
SECRET_KEY = "your_secret_key"
DB_FILE = "oauth.db"
ALGORITHM = "HS256"

def init_db():
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("""
        CREATE TABLE IF NOT EXISTS clients (
            client_id TEXT PRIMARY KEY,
            client_secret TEXT,
            redirect_uri TEXT
        )
    """)
    conn.commit()
    conn.close()

def get_client(client_id):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("SELECT * FROM clients WHERE client_id = ?", (client_id,))
    client = c.fetchone()
    conn.close()
    return client

@app.route("/authorize")
def authorize():
    client_id = request.args.get("client_id")
    redirect_uri = request.args.get("redirect_uri")
    client = get_client(client_id)
    if not client or client[2] != redirect_uri:
        return jsonify({"error": "invalid_client"}), 400
    code = secrets.token_urlsafe(16)
    return redirect(f"{redirect_uri}?code={code}")

@app.route("/token", methods=["POST"])
def token():
    client_id = request.form.get("client_id")
    client_secret = request.form.get("client_secret")
    code = request.form.get("code")
    client = get_client(client_id)
    if not client or client[1] != client_secret:
        return jsonify({"error": "invalid_client"}), 400
    access_token = jwt.encode({"sub": client_id, "exp": time.time() + 3600}, SECRET_KEY, algorithm=ALGORITHM)
    return jsonify({"access_token": access_token, "token_type": "Bearer"})

@app.route("/protected")
def protected():
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "missing_token"}), 401
    token = auth_header.split(" ")[1]
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return jsonify({"message": "Access granted", "user": payload["sub"]})
    except jwt.ExpiredSignatureError:
        return jsonify({"error": "token_expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"error": "invalid_token"}), 401

if __name__ == "__main__":
    init_db()
    app.run(debug=True, host="0.0.0.0", port=5001)
