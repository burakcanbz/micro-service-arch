import json
from Optional
from core import redis_client as redis
from schemas import UserResponse
from models import User

async def check_user_with_redis(id: String) -> Optional[UserResponse]:
    cached_user = await redis.get(f"user:{id}")
    if cached_user:
        user_dict = json.loads(cached_user)
        print("user returned from cache!")
        return UserResponse(
        id=user_dict['user_id'],
        email=user_dict['email'],
        name=user_dict['name'],
        role=user_dict['role'])
    return None

async def check_token_in_blacklist(token: str) -> Optional[bytes]:
    is_blacklisted = await redis.get(f"blacklist:{token}")
    if is_blacklisted:
        raise HTTPException(status_code=401, detail="Token revoked")
    return is_blacklisted

async def delete_user_from_redis(id: int) -> None:
    deleted_count = await redis.delete(f"user:{id}")
    if deleted_count == 0:
        print(f"User {id} not found in Redis")
    else:
        print(f"User {id} deleted from Redis")
    return

async def add_user_to_redis(user: User) -> None:
    await redis.set(
        f"user:{user.id}",
        json.dumps({
            "user_id": user.id,
            "email": user.email,
            "name": user.name,
            "password": user.password,
            "role": user.role
        }),
        ex=60*60)
    return