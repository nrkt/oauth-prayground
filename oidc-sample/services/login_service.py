from libs import exceptions
from models import dao

def execute(username, password):
    user = dao.get_user_by_name_and_password(username, password)

    if not user:
        raise exceptions.NotFoundError("User not found")
        
    return user[0] # user_id
