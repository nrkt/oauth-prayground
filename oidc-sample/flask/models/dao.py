import time
from models.database import get_db_connection
from datetime import datetime, timedelta, timezone

def get_client(client_id):
    conn = get_db_connection()
    c = conn.cursor()
    c.execute("SELECT * FROM clients WHERE client_id = ?", (client_id,))
    client = c.fetchone()
    conn.close()
    return client

# TODO: scope
def save_auth_code(code, client_id, user_id):
    now = int(datetime.now(timezone.utc).timestamp())
    expires_at = now + 300 # Code valid for 5 minutes
    conn = get_db_connection()
    c = conn.cursor()
    c.execute("INSERT INTO auth_codes (code, client_id, user_id, expires_at) VALUES (?, ?, ?, ?)", (code, client_id, user_id, expires_at))
    conn.commit()
    conn.close()

def get_auth_code(code):
    conn = get_db_connection()
    c = conn.cursor()
    c.execute("SELECT * FROM auth_codes WHERE code = ?", (code,))
    auth_code = c.fetchone()
    conn.close()
    return auth_code

def mark_auth_code_used(code):
    conn = get_db_connection()
    c = conn.cursor()
    c.execute("UPDATE auth_codes SET used = 1 WHERE code = ?", (code,))
    conn.commit()
    conn.close()

def save_token(access_token, refresh_token, user_id, expires_in):
    now = datetime.now(timezone.utc)
    exp = (now + timedelta(seconds=expires_in)).timestamp() # unix time
    now_unixtime = now.timestamp()
    expires_at = int(now_unixtime + exp)
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("INSERT INTO tokens (access_token, refresh_token, user_id, expires_at) VALUES (?, ?, ?, ?)",
                   (access_token, refresh_token, user_id, expires_at))
    conn.commit()
    conn.close()

def get_user_id_by_token(access_token):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT user_id FROM tokens WHERE access_token = ?", (access_token,))
    user_id = cursor.fetchone()
    conn.close()
    
    return user_id[0] if user_id else None

def get_user_by_user_id(user_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT user_id, name, email FROM users WHERE user_id = ?", (user_id,))
    user = cursor.fetchone()
    conn.close()

    return user

def get_user_by_name_and_password(name, password):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE name = ? AND password = ?", (name, password,))
    user = cursor.fetchone()
    conn.close()

    return user
