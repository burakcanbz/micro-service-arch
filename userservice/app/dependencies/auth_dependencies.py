# dependencies/auth_dependencies.py
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.ext.asyncio import AsyncSession
from core.database import get_db
from service.user_service import UserService
from utils.jwt import decode_access_token
from crud import crud_get_user_by_email

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/login")

async def get_current_user(token: str = Depends(oauth2_scheme), db: AsyncSession = Depends(get_db)):
    payload = decode_access_token(token)
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid token")
    email = payload.get("sub") 
    if not email:
        raise HTTPException(status_code=401, detail="Invalid token")
    user = await crud_get_user_by_email(db, email)
    if not user:
        raise HTTPException(status_code=401, detail="User not found")
    
    return user
