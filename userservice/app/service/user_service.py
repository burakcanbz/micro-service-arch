from sqlalchemy.ext.asyncio import AsyncSession
from crud import crud_create_user, crud_get_user_by_email, crud_get_users, crud_get_user_by_id, crud_replace_user_by_id, crud_update_user_by_id, crud_delete_user_by_id
from schemas.user_schema import UserCreate, UserResponse, UserUpdate, UserReplace
from utils.password import hash_password, verify_password
from logger import get_logger

class UserService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.logger = get_logger()
        self.logger.debug("UserService initialized")

    async def authenticate_user(self, email: str, password: str):
        existing_user = await crud_get_user_by_email(self.db, email)
        if not existing_user or not verify_password(password, existing_user.password):
            self.logger.info(f"User authentication failed with email: '{email}'")
            return None
        return existing_user

    async def create_user(self, user_data: UserCreate, role: str):
        existing_user = await crud_get_user_by_email(self.db, user_data.email)
        if existing_user:
            self.logger.info(f"User creation failed: email '{user_data.email}' already registered.")
            return None
        hashed = hash_password(user_data.password)
        self.logger.info(f"User created: {user_data.email} with role: {role}.")
        user = await crud_create_user(self.db, user_data, hashed, role)
        return user

    async def get_users(self):
        users = await crud_get_users(self.db)
        self.logger.info(f"All users fetched successfully from db.")
        return users

    async def get_user_by_id(self, id: int):
        user = await crud_get_user_by_id(self.db, id)
        self.logger.info(f"User with id '{id}' fetched successfully from db.")
        return user
    
    async def get_user_by_email(self, email: str):
        user = await crud_get_user_by_email(self.db, email)
        self.logger.info(f"User with email '{email}' fetched successfully from db.")
        return user

    async def replace_user_by_id(self, id: int, user_data: UserReplace):
        user = await crud_replace_user_by_id(self.db, id, user_data)
        self.logger.info(f"User with id '{id}' replaced successfully.")
        return user
    
    async def update_user_by_id(self, id: int, user_data: UserUpdate):
        user = await crud_update_user_by_id(self.db, id, user_data)
        self.logger.info(f"User with '{id}' updated successfully.")
        return user
    
    async def delete_user_by_id(self, id: int):
        user = await crud_delete_user_by_id(self.db, id)
        self.logger.info(f"User with '{id}' deleted successfully.")
        return user