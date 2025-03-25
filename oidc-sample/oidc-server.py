import jwt
from datetime import datetime, timezone, timedelta
from flask import Flask, request, jsonify

app = Flask(__name__)

SECRET_KEY = "your_secret_key"
ISSUER = "http://localhost:5002"
ALGORITHM = "HS256" # RS256 is generally used.

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

@app.route("/token", methods=["POST"])
def token():
    client_id = request.form.get("client_id")
    client_secret = request.form.get("client_secret")
    code = request.form.get("code")

    # sample user_id
    user_id = "user123"

    id_token = create_id_token(user_id, client_id)
    
    return jsonify({
        "access_token": "sample_access_token",
        "refresh_token": "sample_refresh_token",
        "id_token": id_token,
        "token_type": "Bearer",
        "expires_in": 3600
    })

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5002)
