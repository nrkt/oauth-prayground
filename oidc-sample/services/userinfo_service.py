import jwt
from models import dao
from libs import tokens, exceptions

def get_access_token(request):
    auth_header = request.headers.get("Authorization")
    if not auth_header or not auth_header.startswith("Bearer "):
        raise exceptions.UnauthorizedError("Missing or invalid token")
    return auth_header.split(" ")[1]

def execute(access_token):
    try:
        tokens.decode_access_token(access_token=access_token)
        user_id = dao.get_user_id_by_token(access_token)
        if not user_id:
            raise exceptions.NotFoundError("Access token not found")
        
        user = dao.get_user_by_user_id(user_id)
        if not user:
            raise exceptions.NotFoundError("User not found")
        
        return user[0], user[1], user[2] # user_id, name, email
    except jwt.ExpiredSignatureError:
        raise exceptions.UnauthorizedError("ID token has expired")
    except jwt.InvalidTokenError:
        raise exceptions.UnauthorizedError("Invalid ID token")
