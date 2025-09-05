from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str = "postgresql+asyncpg://postgres:199512@localhost/users_db"

    class Config:
        env_file = ".env"
settings = Settings()