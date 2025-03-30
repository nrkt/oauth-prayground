import jwt
import time
import secrets
from libs import tokens
from models import dao, database
from datetime import datetime, timezone
from flask import Flask, request, jsonify, redirect, session, render_template, url_for

app = Flask(__name__)
app.secret_key = "your-secret-key" # for session

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        username = request.form.get("username")
        password = request.form.get("password")

        user = dao.get_user_by_name_and_password(username, password)

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

    client = dao.get_client(client_id)
    if not client or client[2] != redirect_uri:
        return jsonify({"error": "invalid_client"}), 400
    
    if "user" not in session:
        session["next"] = request.url # save to back after login
        return redirect(url_for("login"))
    
    if request.method == "POST":
        if request.form.get("approve") == "yes":
            code = secrets.token_urlsafe(16)
            user_id = session["user"]
            dao.save_auth_code(code, client_id, user_id)
            return redirect(f"{redirect_uri}?code={code}")
        
        return "Access denied", 403
    
    return render_template("authorize.html", scope=scope)

@app.route("/token", methods=["POST"])
def token():
    now = datetime.now(timezone.utc).timestamp()
    client_id = request.form.get("client_id")
    client_secret = request.form.get("client_secret")
    code = request.form.get("code")

    client = dao.get_client(client_id)
    auth_code = dao.get_auth_code(code)
    if not client or client[1] != client_secret:
        return jsonify({"error": "invalid_client"}), 400
    if not auth_code or auth_code[1] != client_id:
        return jsonify({"error": "invalid_code"}), 400
    if auth_code[4] == 1:
        return jsonify({"error": "code_already_used"}), 400
    if time.time() > auth_code[3]:
        return jsonify({"error": "code_expired"}), 400
    dao.mark_auth_code_used(code)
    user_id = auth_code[2]

    expires_in = 3600
    access_token = tokens.encode_access_token(sub = user_id, aud = client_id, exp = now + expires_in)
    refresh_token = secrets.token_urlsafe(32)

    dao.save_token(access_token, refresh_token, user_id, expires_in)
    id_token = tokens.create_id_token(user_id, client_id)
    
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
        tokens.decode_access_token(access_token=access_token, aud=client_id)
        user_id = dao.get_user_id_by_token(access_token)
        if not user_id:
            return jsonify({"error": "Access token not found"}), 404
        
        user = dao.get_user_by_user_id(user_id)
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
    database.init_db()
    app.run(debug=True, host="0.0.0.0", port=5002)
