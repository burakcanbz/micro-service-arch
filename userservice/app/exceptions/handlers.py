from fastapi import Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
from logger import get_logger

class GlobalExceptionHandler:
    LOGGER = get_logger()
    @staticmethod
    async def http_exception_handler(request: Request, exc: StarletteHTTPException):
        GlobalExceptionHandler.LOGGER.warning(f"[HTTPException] {exc.status_code} - {exc.detail}")
        return JSONResponse(
            status_code=exc.status_code,
            content={"detail": exc.detail}
        )

    @staticmethod
    async def validation_exception_handler(request: Request, exc: RequestValidationError):
        GlobalExceptionHandler.LOGGER.warning(f"[ValidationError] {exc.errors()}")
        return JSONResponse(
            status_code=422,
            content={"detail": exc.errors()}
        )

    @staticmethod
    async def unhandled_exception_handler(request: Request, exc: Exception):
        GlobalExceptionHandler.LOGGER.error(f"[UnhandledException] {str(exc)}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"detail": "Internal Server Error"}
        )