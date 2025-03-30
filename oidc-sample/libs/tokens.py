import jwt
import secrets
from datetime import datetime, timezone, timedelta

SECRET_KEY = "your_secret_key"
ALGORITHM = "HS256" # RS256 is generally used.
ISSUER = "http://localhost:5002"
ACCESS_TOKEN_AUDIENCES = ["http://localhost:50002/userinfo"]

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

def encode_access_token(sub, exp):
    return jwt.encode({"sub": sub, "aud": ACCESS_TOKEN_AUDIENCES, "exp": exp}, SECRET_KEY, algorithm=ALGORITHM)

def decode_access_token(access_token):
    return jwt.decode(access_token, SECRET_KEY, audience=ACCESS_TOKEN_AUDIENCES, algorithms=[ALGORITHM])

def generate_authorization_code():
    return secrets.token_urlsafe(16)

def generate_refresh_token():
    return secrets.token_urlsafe(32)
