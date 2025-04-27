from libs import exceptions, tokens
from models import dao

def validate_client(client_id, redirect_uri):
    client = dao.get_client(client_id)
    if not client or client[2] != redirect_uri:
        raise exceptions.BadRequestError("invalid client")    

def execute(request, session, client_id):    
    if request.form.get("approve") == "yes":
        code = tokens.generate_authorization_code()
        user_id = session["user"]
        dao.save_auth_code(code, client_id, user_id)
        return code
    else:
        raise exceptions.ForbiddenError("Access denied")