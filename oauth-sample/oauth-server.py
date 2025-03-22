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
    c.execute("""
        CREATE TABLE IF NOT EXISTS auth_codes (
            code TEXT PRIMARY KEY,
            client_id TEXT,
            expires_at INTEGER,
            used INTEGER DEFAULT 0
        )
    """)
    c.execute("""
        CREATE TABLE IF NOT EXISTS refresh_tokens (
            token TEXT PRIMARY KEY,
            client_id TEXT,
            expires_at INTEGER
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

def save_auth_code(code, client_id):
    expires_at = int(time.time()) + 300 # Code valid for 5 minutes
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("INSERT INTO auth_codes (code, client_id, expires_at) VALUES (?, ?, ?)", (code, client_id, expires_at))
    conn.commit()
    conn.close()

def get_auth_code(code):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("SELECT * FROM auth_codes WHERE code = ?", (code,))
    auth_code = c.fetchone()
    conn.close()
    return auth_code

def mark_auth_code_used(code):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("UPDATE auth_codes SET used = 1 WHERE code = ?", (code,))
    conn.commit()
    conn.close()

def save_refresh_token(token, client_id):
    expires_at = int(time.time()) + (7 * 24 * 60 * 60) # 7 days
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("INSERT INTO refresh_tokens (token, client_id, expires_at) VALUES (?, ?, ?)", (token, client_id, expires_at))
    conn.commit()
    conn.close()

def get_refresh_token(token):
    conn = sqlite3.connect(DB_FILE)
    c = conn.cursor()
    c.execute("SELECT * FROM refresh_tokens WHERE token = ?", (token,))
    refresh_token = c.fetchone()
    conn.close()
    return refresh_token

@app.route("/authorize")
def authorize():
    client_id = request.args.get("client_id")
    redirect_uri = request.args.get("redirect_uri")
    client = get_client(client_id)
    if not client or client[2] != redirect_uri:
        return jsonify({"error": "invalid_client"}), 400
    code = secrets.token_urlsafe(16)
    save_auth_code(code, client_id)
    return redirect(f"{redirect_uri}?code={code}")

@app.route("/token", methods=["POST"])
def token():
    client_id = request.form.get("client_id")
    client_secret = request.form.get("client_secret")
    code = request.form.get("code")
    client = get_client(client_id)
    auth_code = get_auth_code(code)
    if not client or client[1] != client_secret:
        return jsonify({"error": "invalid_client"}), 400
    if not auth_code or auth_code[1] != client_id:
        return jsonify({"error": "invalid_code"}), 400
    if auth_code[3] == 1:
        return jsonify({"error": "code_already_used"}), 400
    if time.time() > auth_code[2]:
        return jsonify({"error": "code_expired"}), 400
    mark_auth_code_used(code)
    access_token = jwt.encode({"sub": client_id, "exp": time.time() + 3600}, SECRET_KEY, algorithm=ALGORITHM)
    refresh_token = secrets.token_urlsafe(32)
    save_refresh_token(refresh_token, client_id)
    return jsonify({"access_token": access_token, "token_type": "Bearer", "refresh_token": refresh_token})

@app.route("/refresh", methods=["POST"])
def refresh():
    refresh_token = request.form.get("refresh_token")
    stored_token = get_refresh_token(refresh_token)
    if not stored_token:
        return jsonify({"error": "invalid refresh_token"}), 400
    if time.time() > stored_token[2]:
        return jsonify({"error": "refresh_token expired"}), 400
    access_token = jwt.encode({"sub": stored_token[1], "exp": time.time() + 3600}, SECRET_KEY, algorithm=ALGORITHM)
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
