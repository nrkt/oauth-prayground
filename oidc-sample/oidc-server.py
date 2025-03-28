import jwt
import sqlite3
import time
import secrets
from datetime import datetime, timezone, timedelta
from flask import Flask, request, jsonify, redirect, session, render_template, url_for

app = Flask(__name__)
app.secret_key = "your-secret-key" # for session

DATABASE="oidc.db"
SECRET_KEY = "your_secret_key"
ISSUER = "http://localhost:5002"
ALGORITHM = "HS256" # RS256 is generally used.

def init_db():
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS clients (
            client_id TEXT PRIMARY KEY,
            client_secret TEXT,
            redirect_uri TEXT
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS auth_codes (
            code TEXT PRIMARY KEY,
            client_id TEXT,
            user_id TEXT,
            expires_at INTEGER,
            used INTEGER DEFAULT 0
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            user_id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            email TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL
        )
    """)

    cursor.execute("""
        CREATE TABLE IF NOT EXISTS tokens (
            access_token TEXT PRIMARY KEY,
            refresh_token TEXT NOT NULL,
            user_id TEXT NOT NULL,
            expires_at INTEGER NOT NULL,
            FOREIGN KEY(user_id) REFERENCES users(user_id)
        )
    """)
    conn.commit()
    conn.close()


def get_client(client_id):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute("SELECT * FROM clients WHERE client_id = ?", (client_id,))
    client = c.fetchone()
    conn.close()
    return client

# TODO: scope
def save_auth_code(code, client_id, user_id):
    expires_at = int(time.time()) + 300 # Code valid for 5 minutes
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute("INSERT INTO auth_codes (code, client_id, user_id, expires_at) VALUES (?, ?, ?, ?)", (code, client_id, user_id, expires_at))
    conn.commit()
    conn.close()

def get_auth_code(code):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute("SELECT * FROM auth_codes WHERE code = ?", (code,))
    auth_code = c.fetchone()
    conn.close()
    return auth_code

def mark_auth_code_used(code):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute("UPDATE auth_codes SET used = 1 WHERE code = ?", (code,))
    conn.commit()
    conn.close()

def create_id_token(user_id, client_id):
    now = datetime.now(timezone.utc)
    exp = (now + timedelta(minutes=30)).timestamp() # unix time
    now_unixtime = now.timestamp()
    payload = {
        "sub": user_id,
        "iss": ISSUER,
        "aud": client_id,
        "exp": int(exp),
        "iat": int(now_unixtime),
        "auth_time": int(now_unixtime)
    }
    return jwt.encode(payload, SECRET_KEY, ALGORITHM)

def save_token(access_token, refresh_token, user_id, expires_in):
    now = datetime.now(timezone.utc)
    exp = (now + timedelta(seconds=expires_in)).timestamp() # unix time
    now_unixtime = now.timestamp()
    expires_at = int(now_unixtime + exp)
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute("INSERT INTO tokens (access_token, refresh_token, user_id, expires_at) VALUES (?, ?, ?, ?)",
                   (access_token, refresh_token, user_id, expires_at))
    conn.commit()
    conn.close()

def get_user_id_by_token(access_token):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute("SELECT user_id FROM tokens WHERE access_token = ?", (access_token,))
    row = cursor.fetchone()
    conn.close()
    
    return None if not row else row[0]

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        username = request.form.get("username")
        password = request.form.get("password")
        
        conn = sqlite3.connect(DATABASE)
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM users WHERE name = ? AND password = ?", (username, password,))
        user = cursor.fetchone()
        conn.close()

        if not user:
            return jsonify({"error": "User not found"}), 404
        
        session["user"] = user[0]
        
        return redirect(session.pop("next") or "/")

    return render_template("login.html")

@app.route("/authorize", methods=["GET", "POST"]) # GET: for client, POST: for user
def authorize():
    client_id = request.args.get("client_id")
    redirect_uri = request.args.get("redirect_uri")
    scope = request.args.get("scope", "")

    client = get_client(client_id)
    if not client or client[2] != redirect_uri:
        return jsonify({"error": "invalid_client"}), 400
    
    if "user" not in session:
        session["next"] = request.url # save to back after login
        return redirect(url_for("login"))
    
    if request.method == "POST":
        if request.form.get("approve") == "yes":
            code = secrets.token_urlsafe(16)
            user_id = session["user"]
            save_auth_code(code, client_id, user_id)
            return redirect(f"{redirect_uri}?code={code}")
        
        return "Access denied", 403
    
    return render_template("authorize.html", scope=scope)

@app.route("/token", methods=["POST"])
def token():
    now = datetime.now(timezone.utc).timestamp()
    client_id = request.form.get("client_id")
    client_secret = request.form.get("client_secret")
    code = request.form.get("code")

    client = get_client(client_id)
    auth_code = get_auth_code(code)
    if not client or client[1] != client_secret:
        return jsonify({"error": "invalid_client"}), 400
    if not auth_code or auth_code[1] != client_id:
        return jsonify({"error": "invalid_code"}), 400
    if auth_code[4] == 1:
        return jsonify({"error": "code_already_used"}), 400
    if time.time() > auth_code[3]:
        return jsonify({"error": "code_expired"}), 400
    mark_auth_code_used(code)
    user_id = auth_code[2]

    expires_in = 3600
    access_token = jwt.encode({"sub": user_id, "aud": client_id, "exp": now + expires_in}, SECRET_KEY, algorithm=ALGORITHM)
    refresh_token = secrets.token_urlsafe(32)

    save_token(access_token, refresh_token, user_id, expires_in)

    id_token = create_id_token(user_id, client_id)
    
    return jsonify({
        "access_token": access_token,
        "refresh_token": refresh_token,
        "id_token": id_token,
        "token_type": "Bearer",
        "expires_in": expires_in
    })

@app.route("/userinfo", methods=["GET"])
def userinfo():
    auth_header = request.headers.get("Authorization")
    client_id = request.args.get("client_id")

    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Missing or invalid token"}), 401
    
    access_token = auth_header.split(" ")[1]

    try:
        jwt.decode(access_token, SECRET_KEY, audience=client_id, algorithms=[ALGORITHM])

        user_id = get_user_id_by_token(access_token)

        if not user_id:
            return jsonify({"error": "Access token not found"}), 404
    
        conn = sqlite3.connect(DATABASE)
        cursor = conn.cursor()
        cursor.execute("SELECT user_id, name, email FROM users WHERE user_id = ?", (user_id,))
        user = cursor.fetchone()
        conn.close()

        if not user:
            return jsonify({"error": "User not found"}), 404
        
        return jsonify({
            "sub": user[0],
            "name": user[1],
            "email": user[2]
        })
    except jwt.ExpiredSignatureError:
        return jsonify({"error": "ID token has expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"error": "Invalid ID token"}), 401


if __name__ == "__main__":
    init_db()
    app.run(debug=True, host="0.0.0.0", port=5002)
