from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from models import Base
from sqlalchemy.engine import URL
from sqlalchemy import text
from .config import settings

DB_NAME = settings.PGDATABASE
url = settings.db_url
engine = create_async_engine(url, isolation_level="AUTOCOMMIT", echo=True)
SessionLocal = sessionmaker(bind=engine, class_=AsyncSession, expire_on_commit=False)

async def get_db():
    async with SessionLocal() as session:
        yield session

async def create_db_if_not_exist():
    engine = create_async_engine(url, isolation_level="AUTOCOMMIT", echo=True)
    async with engine.begin() as conn:
        result = await conn.execute(text(f"SELECT 1 FROM pg_database WHERE datname='{DB_NAME}'"))
        if result.scalar() is None:
            await conn.execute(text(f'CREATE DATABASE {DB_NAME}'))
            print(f"Database {DB_NAME} created!")
        else:
            print(f"Database {DB_NAME} already exists.")
    await engine.dispose()

async def create_tables():
    engine = create_async_engine(url, echo=True)
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all) # checking if table already created
    await engine.dispose()