import redis.asyncio as redis
import os
import json
import time

from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Response
from sqlalchemy.ext.asyncio import AsyncSession
from core.database import get_db
from core import redis_client as redis
from schemas.user_schema import UserCreate, UserResponse, UserUpdate, UserReplace, UserLogin, Token
from messaging import send_user_registered_event
from service.user_service import UserService
from dependencies import get_current_user, require_roles, oauth2_scheme
from models import User
from utils import create_access_token, decode_access_token

# TODO add global exception handler and logger 
# TODO add docker.yaml for redis, rabbitmq, postgres

router = APIRouter(prefix="/users", tags=["Users"])

@router.post("/login")
async def login(user_login: UserLogin, response: Response, db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    user = await service.authenticate_user(user_login.email, user_login.password)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    token = create_access_token({"email": str(user.email), "id": int(user.id), "role": str(user.role)})
    response.headers["Authorization"] = f"Bearer {token}"
    user_data = {
    "user_id": user.id,
    "email": user.email,
    "name": user.name,
    "role": user.role
    }
    key = f"user:{user_data['user_id']}"
    await redis.set(key, json.dumps(user_data), ex=60*60)
    return {"message": "Login successful!"}

@router.post("/logout")
async def logout(token: str = Depends(oauth2_scheme)):
    is_blacklisted = await redis.get(f"blacklist:{token}")
    if is_blacklisted:
        raise HTTPException(status_code=401, detail="Token revoked")
    expire = decode_access_token(token).get("exp", 0)
    ttl = max(expire - int(time.time()), 0)
    await redis.setex(f"blacklist:{token}", ttl, "true")
    return {"message": "Logged out successfully"}

@router.post("/user", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def register_user(user: UserCreate, db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    new_user = await service.create_user(user, role="user")
    if new_user is None:
        raise HTTPException(status_code=400, detail="Email already registered")
    await send_user_registered_event({
        "type": "UserRegistered",
        "user_id": new_user.id,
        "email": new_user.email
    })
    return new_user

@router.get("/", response_model=List[UserResponse])
async def get_users(db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin"))):
    service = UserService(db)
    users = await service.get_users()
    if not users:
        raise HTTPException(status_code=404, detail="Users not found")
    return users

@router.get("/{id}", response_model=UserResponse)
async def get_user(id: int, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin", "moderator"))):
    cached_user = await redis.get(f"user:{id}")
    if cached_user:
        user_dict = json.loads(cached_user)
        print("user returned from Redis!")
        return UserResponse(
        id=user_dict['user_id'],
        email=user_dict['email'],
        name=user_dict['name'],
        role=user_dict['role'])
    service = UserService(db)
    user = await service.get_user_by_id(id)
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    print("user returned from DB!")
    return user

@router.put("/{id}", response_model=UserResponse) # TODO add redis check if user exists in cache. If exist update user and update the data in the db.
async def replace_user(id: int, user_data: UserReplace, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin", "moderator"))):
    service = UserService(db)
    updated_user = await service.replace_user_by_id(id, user_data)
    if not updated_user:
        raise HTTPException(status_code=404, detail="User not found")
    return updated_user

@router.patch("/{id}", response_model=UserResponse) # TODO add redis check if user exists in cache. If exist update user and update the data in the db.
async def update_user(id: int, user_data: UserUpdate, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin", "moderator"))):
    service = UserService(db)
    updated_user = await service.update_user_by_id(id, user_data)
    if not updated_user:
        raise HTTPException(status_code=404, detail="User not found")
    return updated_user

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT) # check user if exist in redis. If it is delete user from redis and delete user from db.
# with an addition user delete should be published over rabbitmq to delete deleted users orders.
async def delete_user(id: int, db: AsyncSession = Depends(get_db), _: User = Depends(require_roles("admin"))):
    service = UserService(db)
    user = await service.delete_user_by_id(id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return {"message": f"User with id {id} has been deleted successfully"}

