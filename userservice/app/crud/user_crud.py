from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from models.user_model import User
from schemas.user_schema import UserCreate, UserResponse, UserUpdate, UserReplace
from utils import hash_password

async def crud_get_user_by_id(db: AsyncSession, id: int):
    result = await db.execute(select(User).where(User.id == id))
    return result.scalars().first()

async def crud_get_user_by_email(db: AsyncSession, email: str):
    result = await db.execute(select(User).where(User.email == email))
    return result.scalars().first()

async def crud_create_user(db: AsyncSession, user: UserCreate, hashed_password: str, role: str):
    new_user = User(
        email=user.email,
        name=user.name,
        password=hashed_password,
        role=role
    )
    db.add(new_user)
    await db.commit()
    await db.refresh(new_user)
    return new_user

async def crud_get_users(db: AsyncSession):
    users = await db.execute(select(User))
    return users.scalars().all()

async def crud_replace_user_by_id(db: AsyncSession, user_id: int, user_data: UserReplace):
    user = await crud_get_user_by_id(db, user_id)
    if not user:
        return None
    update_data = user_data.dict(exclude_unset=True)
    for field, value in update_data.items():
        if value is None:
            continue
        if field == "password":
            setattr(user, "password", hash_password(value))
        else:
            setattr(user, field, value)
    await db.commit()
    await db.refresh(user)
    return user

async def crud_update_user_by_id(db: AsyncSession, user_id: int, user_data: UserUpdate):
    user = await crud_get_user_by_id(db, user_id)
    if not user:
        return None
    update_user_data = user_data.dict(exclude_unset=True)
    if not update_user_data:
        raise HTTPException(400, detail="No data update")
    for field, value in update_user_data.items():
        if value is None:
            continue
        if field == "password":
            setattr(user, "password", hash_password(value))
        else:
            setattr(user, field, value)
    await db.commit()
    await db.refresh(user)
    return user

async def crud_delete_user_by_id(db: AsyncSession, user_id: int):
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalars().first()
    if not user:
        return None
    await db.delete(user)
    await db.commit()
    return user
    
    