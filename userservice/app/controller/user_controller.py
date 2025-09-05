from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from core.database import get_db
from schemas.user_schema import UserCreate, UserResponse, UserUpdate, UserReplace
from service.user_service import UserService

router = APIRouter(prefix="/users", tags=["Users"])

@router.post("/user", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def register_user(user: UserCreate, db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    new_user = await service.create_user(user)
    if new_user is None:
        raise HTTPException(status_code=400, detail="Email already registered")
    return new_user

@router.get("/", response_model=List[UserResponse])
async def get_users(db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    users = await service.get_users()
    if not users:
        raise HTTPException(status_code=404, detail="Users not found")
    return users

@router.get("/{id}", response_model=UserResponse)
async def get_user(id: int, db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    user = await service.get_user_by_id(id)
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return user

@router.put("/{id}", response_model=UserResponse)
async def replace_user(id: int, user_data: UserReplace, db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    updated_user = await service.replace_user_by_id(id, user_data)
    if not updated_user:
        raise HTTPException(status_code=404, detail="User not found")
    return updated_user

@router.patch("/{id}", response_model=UserResponse)
async def update_user(id: int, user_data: UserUpdate, db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    updated_user = await service.update_user_by_id(id, user_data)
    if not update_user:
        raise HTTPException(status_code=404, detail="User not found")
    return updated_user

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_user(id: int, db: AsyncSession = Depends(get_db)):
    service = UserService(db)
    user = await service.delete_user_by_id(id)
    if not user:
        return HTTPException(status_code=404, detail="User not found")
    return {"message": f"User with id {id} has been deleted successfully"}

