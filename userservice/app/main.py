import os

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
from fastapi.middleware.cors import CORSMiddleware

from dotenv import load_dotenv
from controller import router
from core import Base, engine, create_db_if_not_exist, create_tables
from exceptions import GlobalExceptionHandler

load_dotenv()

app = FastAPI(title="User Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
)

app.add_exception_handler(StarletteHTTPException, GlobalExceptionHandler.http_exception_handler)
app.add_exception_handler(RequestValidationError, GlobalExceptionHandler.validation_exception_handler)
app.add_exception_handler(Exception, GlobalExceptionHandler.unhandled_exception_handler)

@app.on_event("startup")
async def on_startup():
    await create_db_if_not_exist()
    await create_tables()
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

app.include_router(router)
