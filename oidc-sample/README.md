# OIDC Sample

## Usage

1. Register RP
```sh
sqlite3 oidc.db
```
```sql
INSERT INTO clients (client_id, client_secret, redirect_uri)
VALUES ('client123', 'secret123', 'http://localhost:5002/callback');
```
2. Register User
```sql
INSERT INTO users (user_id, name, email, password)
VALUES ('userid1', 'username1', 'useremail1', 'userpassword1');
```

3. Start server
```sh
python3 oidc-server.py
```

4. Get authorization code
```sh
http://127.0.0.1:5002/authorize?client_id=client123&redirect_uri=http://localhost:5002/callback
```

5. Login

6. Get authorization code (Approve authorization request)  
-> http://localhost:5002/callbacl?code=...

7. Get access token and id token
```sh
curl -X POST http://127.0.0.1:5002/token \
    -d "client_id=client123" \
    -d "client_secret=secret123" \
    -d "code=KKmmXypFmyrGjZartJEBiw"
```
-> response
```
{
  "access_token": "...",
  "expires_in": 3600,
  "id_token": "...",
  "refresh_token": "...",
  "token_type": "Bearer"
}
```

8. Get userInfo (Access with access token)
```sh
curl -H "Authorization: Bearer <access_token>" "http://127.0.0.1:5002/userinfo?client_id=client123"
```
```
{
  "email": "useremail1",
  "name": "username1",
  "sub": "userid1"
}
```