# OAuth Sample
## Usage
1. Register client
```sh
sqlite3 oauth.db
```
```sql
INSERT INTO clients (client_id, client_secret, redirect_uri)
VALUES ('client123', 'secret123', 'http://localhost:5001/callback');
```

2. Start server
```sh
python3 oauth-server.py
```

3. Get authorization code
```sh
http://127.0.0.1:5001/authorize?client_id=client123&redirect_uri=http://localhost:5001/callback
```
-> http://localhost:5001/callback?code=...

4. Get access token
```sh
curl -X POST http://127.0.0.1:5001/token \
     -d "client_id=client123" \
     -d "client_secret=secret123" \
     -d "code=<code>"
```
-> response
```
{
    "access_token": "eyJhbGciOiJIUzI1...",
    "refresh_token": "...",
    "token_type": "Bearer"
}
```
5. Access with access token
```sh
curl -H "Authorization: Bearer <access_token>" http://127.0.0.1:5001/protected
```
-> response
```
{
    "message": "Access granted",
    "user": "client123"
}
```
6. Refresh token
```sh
❯ curl -X POST http://127.0.0.1:5001/refresh -d "refresh_token=..."
```
-> response
```
{
  "access_token": "eyJhbGciOiJIUzI1...",
  "token_type": "Bearer"
}
```