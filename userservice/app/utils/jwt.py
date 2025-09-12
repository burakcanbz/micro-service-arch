import os
from datetime import datetime, timedelta
from jose import jwt

SECRET_KEY = "supersecret_supersecretkey12345678"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 1 * 300

def create_access_token(data: dict) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    token = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM) 
    return token

def decode_access_token(token: str) -> dict | None:
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except Exception:
        return None
