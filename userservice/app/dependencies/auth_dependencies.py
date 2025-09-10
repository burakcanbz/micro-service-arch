# dependencies/auth_dependencies.py
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.ext.asyncio import AsyncSession
from core.database import get_db
from core import redis_client as redis
from service.user_service import UserService
from utils.jwt import decode_access_token
from crud import crud_get_user_by_email
from models import User
from logger import get_logger

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="")
logger = get_logger()

async def get_current_user(token: str = Depends(oauth2_scheme), db: AsyncSession = Depends(get_db)):
    logger.info("Attempting to get current user from token")
    payload = decode_access_token(token)
    if not payload:
        logger.warn("Invalid token received")
        raise HTTPException(status_code=401, detail="Invalid token")

    # Check token whether in blacklist
    is_blacklisted = await redis.get(f"blacklist:{token}")
    if is_blacklisted:
        logger.warning(f"Token is blacklisted: {token}")
        raise HTTPException(status_code=401, detail="Token revoked")

    email = payload.get("email") 
    if not email:
        logger.error("Token payload does not contain email")
        raise HTTPException(status_code=401, detail="Invalid token")

    user = await crud_get_user_by_email(db, email)
    if not user:
        logger.warning(f"User not found for email: {email}")
        raise HTTPException(status_code=401, detail="User not found")

    logger.info(f"User authenticated: {user.email}")
    return user

def require_roles(*roles: str):
    def wrapper(current_user:User=Depends(get_current_user)):
        if current_user.role not in roles:
            logger.warn(f"User '{current_user.email}' does not have required roles: {roles}")
            raise HTTPException(status_code=403, detail="Forbidden")
        logger.info(f"User '{current_user.email}' passed role check: {roles}")
        return current_user
    return wrapper
