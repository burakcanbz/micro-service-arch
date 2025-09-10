import logging
from pathlib import Path

def get_logger(name: str = "user-service"):
    logger = logging.getLogger(name)
    logger.setLevel(logging.DEBUG)

    log_file = Path(__file__).parent / ".logs"

    if not any(isinstance(h, logging.FileHandler) for h in logger.handlers):
        file_handler = logging.FileHandler(log_file, encoding="utf-8")
        formatter = logging.Formatter(
            "%(asctime)s | %(levelname)s | %(name)s | %(message)s",
            "%Y-%m-%d %H:%M:%S"
        )
        file_handler.setFormatter(formatter)
        logger.addHandler(file_handler)
        
    return logger
