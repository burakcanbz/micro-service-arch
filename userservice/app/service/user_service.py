from sqlalchemy.ext.asyncio import AsyncSession
from crud import crud_create_user, crud_get_user_by_email, crud_get_users, crud_get_user_by_id, crud_replace_user_by_id, crud_update_user_by_id, crud_delete_user_by_id
from schemas.user_schema import UserCreate, UserResponse, UserUpdate, UserReplace
from utils.password import hash_password, verify_password

class UserService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def authenticate_user(self, email: str, password: str):
        existing_user = await crud_get_user_by_email(self.db, email)
        if not existing_user or not verify_password(password, existing_user.hashed_password):
            return None
        return existing_user

    async def create_user(self, user_data: UserCreate, role: str):
        existing_user = await crud_get_user_by_email(self.db, user_data.email)
        if existing_user:
            return None
        hashed = hash_password(user_data.password)
        return await crud_create_user(self.db, user_data, hashed, role)

    async def get_users(self):
        return await crud_get_users(self.db)

    async def get_user_by_id(self, id: int):
        return await crud_get_user_by_id(self.db, id)
    
    async def get_user_by_email(self, email: str):
        return await crud_get_user_by_email(self.db, email)

    async def replace_user_by_id(self, id: int, user_data: UserReplace):
        return await crud_replace_user_by_id(self.db, id, user_data)
    
    async def update_user_by_id(self, id: int, user_data: UserUpdate):
        return await crud_update_user_by_id(self.db, id, user_data)
    
    async def delete_user_by_id(self, id: int):
        return await crud_delete_user_by_id(self.db, id)