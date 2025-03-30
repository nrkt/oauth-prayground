import services
from libs import exceptions
from models import database
from flask import Flask, request, jsonify, redirect, session, render_template, url_for

app = Flask(__name__)
app.secret_key = "your-secret-key" # for session

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        username = request.form.get("username")
        password = request.form.get("password")

        user = services.login_service.execute(username, password)        
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

    services.authorize_service.validate_client(client_id, redirect_uri)
    
    if request.method == "POST":
        authorization_code = services.authorize_service.execute(request, session, client_id)
        return redirect(f"{redirect_uri}?code={authorization_code}")        
    
    return render_template("authorize.html", scope=scope)

@app.route("/token", methods=["POST"])
def token():
    client_id = request.form.get("client_id")
    client_secret = request.form.get("client_secret")
    code = request.form.get("code")

    id_token, access_token, refresh_token, expires_in = services.token_service.execute(client_id, client_secret, code)

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

    user_id, name, email = services.userinfo_service.execute(access_token)
    return jsonify({
        "sub": user_id,
        "name": name,
        "email": email
    })

@app.errorhandler(exceptions.CustomError)
def handle_custom_error(e):
    return jsonify({"error": e.message}), e.status_code

if __name__ == "__main__":
    database.init_db()
    app.run(debug=True, host="0.0.0.0", port=5002)
