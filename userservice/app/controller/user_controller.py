import os
import json
import time

from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Response
from sqlalchemy.ext.asyncio import AsyncSession
from core.database import get_db
from schemas.user_schema import UserCreate, UserResponse, UserUpdate, UserReplace, UserLogin, Token
from service.user_service import UserService
from messaging import send_user_registered_event, send_user_deleted_event
from dependencies import get_current_user, require_roles, oauth2_scheme
from core import redis_client as redis
from models import User
from logger import get_logger
from utils import *

# TODO add global exception handler

router = APIRouter(prefix="/users", tags=["Users"])
logger = get_logger()

@router.post("/login")
async def login(user_login: UserLogin, response: Response, db: AsyncSession = Depends(get_db)):
    logger.info(f"GET /login - Login request")
    service = UserService(db)
    user = await service.authenticate_user(user_login.email, user_login.password)
    if not user:
        logger.warn(f"GET /login - Login failed unknown email='{user_login.email}'")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    token = create_access_token({"email": str(user.email), "id": int(user.id), "role": str(user.role)})
    response.headers["Authorization"] = f"Bearer {token}"
    await add_user_to_redis(user)
    logger.info(f"GET /login - Login successfull with email='{user_login.email}'")
    return {"message": "Login successful!"}

@router.post("/logout")
async def logout(token: str = Depends(oauth2_scheme)):
    logger.info(f"POST /logout - Logout request.")
    await check_in_blacklist(token)
    expire = decode_access_token(token).get("exp", 0)
    ttl = max(expire - int(time.time()), 0)
    await redis.setex(f"blacklist:{token}", ttl, "true")
    logger.info(f"POST /logout - Logout successfull.")
    return {"message": "Logged out successfully"}

@router.post("/user", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def register_user(user: UserCreate, db: AsyncSession = Depends(get_db)):
    logger.info(f"POST /user - User create request.")
    service = UserService(db)
    new_user = await service.create_user(user, role="user")
    if new_user is None:
        logger.warn(f"POST /user - User creation request failed. Email already registered email:'{email}'")
        raise HTTPException(status_code=400, detail="Email already registered")
    await send_user_registered_event({
        "type": "UserRegistered",
        "user_id": new_user.id,
        "email": new_user.email
    })
    logger.info(f"POST /user - User registered event published to user.registered queue.")
    return new_user

@router.get("/", response_model=List[UserResponse])
async def get_users(db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin"))):
    logger.info(f"GET / - All users get request.")
    service = UserService(db)
    users = await service.get_users()
    if not users:
        logger.warn(f"GET / - Users not found.")
        raise HTTPException(status_code=404, detail="Users not found")
    return users

@router.get("/{id}", response_model=UserResponse)
async def get_user(id: int, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin", "moderator"))):
    logger.info(f"GET /id - Get user by id.")
    cached_user = await check_user_with_redis(id)
    logger.info(f"GET /id - User returned from Cache!")
    if cached_user is not None:
        logger.warn(f"GET /id - User not getting from cache with id:'{id}'")
        return cached_user
    service = UserService(db)
    user = await service.get_user_by_id(id)
    if user is None:
        logger.warn(f"GET /id - User not found with id:'{id}'")
        raise HTTPException(status_code=404, detail="User not found")
    await add_user_to_redis(user)
    logger.info(f"GET /id - User added to Cache.")
    logger.info(f"GET /id -  User returned from DB!")
    return user

@router.put("/{id}", response_model=UserReplace)
async def replace_user(id: int, user_data: UserReplace, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin", "moderator"))):
    logger.info(f"PUT /id - Replace user requested with id:'{id}'.")
    service = UserService(db)
    user_to_update = await check_user_with_redis(id) or await service.get_user_by_id(id)
    if not user_to_update:
        logger.warn("PUT /id - User not found.")
        raise HTTPException(status_code=404, detail="User not found")
    updated_user = await service.update_user_by_id(user_to_update.id, user_data)
    await add_user_to_redis(updated_user)
    logger.info(f"PUT /id - User added to Cache!")
    logger.info(f"PUT /id - Replace user with id:'{id}'")
    return updated_user

@router.patch("/{id}", response_model=UserUpdate)
async def update_user(id: int, user_data: UserUpdate, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin", "moderator"))):
    logger.info(f"PATCH /id - Update user requested with id:'{id}'.")
    service = UserService(db)
    user_to_update = await check_user_with_redis(id) or await service.get_user_by_id(id)
    if not user_to_update:
        logger.warn("PATCH /id - User not found.")
        raise HTTPException(status_code=404, detail="User not found")
    updated_user = await service.update_user_by_id(user_to_update.id, user_data)
    await add_user_to_redis(updated_user)
    logger.info(f"PATCH /id - User added to Cache!")
    logger.info(f"PATCH /id - User updated with id:'{id}'")
    return updated_user

@router.delete("/{id}")
async def delete_user(id: int, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin"))):
    logger.info(f"DELETE /id - Delete user requested with id:'{id}'.")
    service = UserService(db)
    user = await service.delete_user_by_id(id)
    if not user:
        logger.warn("DELETE /id - User not found.")
        raise HTTPException(status_code=404, detail="User not found")
    await delete_user_from_redis(id)
    await send_user_deleted_event({
        "type": "UserDeleted",
        "user_id": user.id,
        "email": user.email
    })
    logger.info(f"DELETE /id - User deleted from Cache!")
    logger.info(f"DELETE /id - User deleted with id:'{id}'")
    logger.info(f"DELETE /user - User deleted event published to user.deleted queue.")
    return {"message": f"User with id {id} has been deleted successfully"}