import jwt
import time
import secrets
from services import login_service, authorize_service, token_service
from libs import tokens, exceptions
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

        user = login_service.execute(username, password)        
        session["user"] = user
        return redirect(session.pop("next") or "/")

    return render_template("login.html")

@app.route("/authorize", methods=["GET", "POST"]) # GET: for client, POST: for user
def authorize():
    client_id = request.args.get("client_id")
    redirect_uri = request.args.get("redirect_uri")
    scope = request.args.get("scope", "")

    if "user" not in session:
        session["next"] = request.url # save to back after login
        return redirect(url_for("login"))

    authorize_service.validate_client(client_id, redirect_uri)
    
    if request.method == "POST":
        authorization_code = authorize_service.execute(request, session, client_id)
        return redirect(f"{redirect_uri}?code={authorization_code}")        
    
    return render_template("authorize.html", scope=scope)

@app.route("/token", methods=["POST"])
def token():
    client_id = request.form.get("client_id")
    client_secret = request.form.get("client_secret")
    code = request.form.get("code")

    id_token, access_token, refresh_token, expires_in = token_service.execute(client_id, client_secret, code)

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
    if not auth_header or not auth_header.startswith("Bearer "):
        raise exceptions.UnauthorizedError("Missing or invalid token")
    access_token = auth_header.split(" ")[1]

    try:
        tokens.decode_access_token(access_token=access_token)
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
        raise exceptions.UnauthorizedError("ID token has expired")
    except jwt.InvalidTokenError:
        raise exceptions.UnauthorizedError("Invalid ID token")
    
@app.errorhandler(exceptions.CustomError)
def handle_custom_error(e):
    return jsonify({"error": e.message}), e.status_code

if __name__ == "__main__":
    database.init_db()
    app.run(debug=True, host="0.0.0.0", port=5002)
