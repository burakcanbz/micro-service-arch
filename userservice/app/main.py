import redis.asyncio as redis
import os

from fastapi import FastAPI
from controller import router
from core import Base, engine, create_db_if_not_exist, create_tables
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="User Service")

@app.on_event("startup")
async def on_startup():
    await create_db_if_not_exist()
    await create_tables()
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

app.include_router(router)
