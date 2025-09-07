import redis.asyncio as redis
import os

from fastapi import FastAPI
from controller import router
from core import Base, engine
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="User Service")

@app.on_event("startup")
async def on_startup():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

app.include_router(router)
