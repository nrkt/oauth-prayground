from libs import exceptions, tokens
from datetime import datetime, timezone
from models import dao

EXPIRES_IN = 3600

def execute(client_id, client_secret, code):
    now = int(datetime.now(timezone.utc).timestamp())
    client = dao.get_client(client_id)
    auth_code = dao.get_auth_code(code)
    if not client or client[1] != client_secret:
        raise exceptions.BadRequestError("invalid client")
    if not auth_code or auth_code[1] != client_id:
        raise exceptions.BadRequestError("invalid code")
    if auth_code[4] == 1:
        raise exceptions.BadRequestError("code already used")
    if now > auth_code[3]:
        raise exceptions.BadRequestError("code expired")
    dao.mark_auth_code_used(code)
    user_id = auth_code[2]

    access_token = tokens.encode_access_token(sub = user_id, exp = now + EXPIRES_IN)
    refresh_token = tokens.generate_refresh_token()

    dao.save_token(access_token, refresh_token, user_id, EXPIRES_IN)
    id_token = tokens.create_id_token(user_id, client_id)
    return id_token, access_token, refresh_token, EXPIRES_IN
