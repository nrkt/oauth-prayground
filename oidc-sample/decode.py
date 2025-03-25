import jwt
SECRET_KEY = "your_secret_key"
ALGORITHM = "HS256"
AUDIENCE = "your_client_id"

def decode_id_token(id_token):
    decoded = jwt.decode(id_token, SECRET_KEY, algorithms=[ALGORITHM], audience=AUDIENCE)
    return decoded

if __name__ == "__main__":
    id_token = input()
    print(decode_id_token(id_token))
