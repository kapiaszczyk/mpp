import asyncio
import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI

from processing_service import ProcessingService

LOG_LEVEL = os.environ.get("LOG_LEVEL", "INFO")
LOG_FORMAT = "%(asctime)s - %(levelname)s - %(name)s - %(message)s"
logging.basicConfig(level=logging.getLevelName(LOG_LEVEL), format=LOG_FORMAT, datefmt="%Y-%m-%d %H:%M:%S")

app = FastAPI()

@asynccontextmanager
async def lifespan(app):
    """Manages the lifespan of the RabbitMQ consumer."""
    consume_task = None
    processing_service = ProcessingService()

    try:
        channel = await processing_service.setup_channel()
        consume_task = asyncio.create_task(processing_service.consume(channel))
        logging.info("RabbitMQ consumer started.")

        yield

    finally:
        logging.info("Shutting down RabbitMQ consumer...")
        if consume_task and not consume_task.cancelled():
            consume_task.cancel()
            try:
                await consume_task
            except asyncio.CancelledError:
                pass
        logging.info("RabbitMQ consumer stopped.")


app = FastAPI(lifespan=lifespan)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
