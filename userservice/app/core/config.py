import redis.asyncio as aioredis
from sqlalchemy.engine import URL
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    REDIS_URL: str = "redis://localhost:6379"
    DRIVERNAME: str = "postgresql+asyncpg"
    PGHOST: str = "localhost"
    PGPORT: int = 5432
    PGUSER: str = "admin"
    PGPASSWORD: str = "admin123"
    PGDATABASE: str = "users_db"

    @property
    def db_url(self) -> URL:
        return URL.create(
            drivername=self.DRIVERNAME,
            username=self.PGUSER,
            password=self.PGPASSWORD,
            host=self.PGHOST,
            port=self.PGPORT,
            database=self.PGDATABASE
        )

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        extra = "allow"

# Settings instance
settings = Settings()

