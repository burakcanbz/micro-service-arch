from pydantic import BaseModel, EmailStr
from typing import Optional

# Response Schema for creating a new user
class UserCreate(BaseModel):
    name: str
    email: EmailStr
    password: str

    class Config:
        extra = "forbid"

# Response Schema for user data
class UserResponse(BaseModel):
    id: int
    name: str
    email: EmailStr
    role: str

    class Config:
        orm_mode = True

# Response Schema for user data
class UserReplace(BaseModel):
    name: str
    email: EmailStr
    password: str

    class Config:
        extra = "forbid"

# Response Schema for user data
class UserUpdate(BaseModel):
    name: Optional[str] = None
    email: Optional[str] = None
    password: Optional[str] = None
    
    class Config:
        extra = "forbid"

# User Login type
class UserLogin(BaseModel):
    email: EmailStr
    password: str

# Token type
class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
