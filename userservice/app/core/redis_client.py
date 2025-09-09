import redis.asyncio as aioredis
from .config import settings  # global settings import

# Global Redis client
redis_client = aioredis.from_url(settings.REDIS_URL, decode_responses=True)
