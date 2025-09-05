from pydantic import BaseModel, EmailStr
from typing import Optional

# Response Schema for creating a new user
class UserCreate(BaseModel):
    name: str
    email: EmailStr
    password: str

# Response Schema for user data
class UserResponse(BaseModel):
    id: int
    name: str
    email: EmailStr

    class Config:
        orm_mode = True

# Response Schema for user data
class UserReplace(BaseModel):
    name: str
    email: EmailStr
    password: str

# Response Schema for user data
class UserUpdate(BaseModel):
    name: Optional[str] = None
    email: Optional[str] = None
    password: Optional[str] = None

# User Login type
class UserLogin(BaseModel):
    email: EmailStr
    password: str

# Token type
class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
