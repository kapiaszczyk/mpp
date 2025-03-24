import json
import logging
import os
import uuid

import aio_pika
import aio_pika.abc
from tenacity import stop_never, wait_exponential, before_sleep_log, retry

from api_service import ApiService
from classifier_service import ClassifierService

LOG_LEVEL = os.environ.get("LOG_LEVEL", "INFO")
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'localhost')
RABBITMQ_PORT = int(os.getenv('RABBITMQ_PORT', 5672))
RABBITMQ_USERNAME = os.getenv('RABBITMQ_USERNAME', 'mpp-tagging-service')
RABBITMQ_PASSWORD = os.getenv('RABBITMQ_PASSWORD', 'mpp-tagging-service')

LOG_FORMAT = (
    "%(asctime)s - %(levelname)s - %(name)s - %(message)s"
)
logging.basicConfig(
    level=logging.getLevelName(LOG_LEVEL),
    format=LOG_FORMAT,
    datefmt="%Y-%m-%d %H:%M:%S",
)


class ProcessingService:
    """Service responsible for processing messages from the RabbitMQ queue.

        This service sets up a connection to RabbitMQ and consumes messages from the 'photo.tagging.request' queue.
        Each message is expected to contain a 'photoId' field, which is used to fetch a photo from the API service.

        The service then uses the ClassifierService to classify the photo
        and publishes the result to the 'photo.tagging.response' queue.
    """

    def __init__(self):
        self.api_service = ApiService()
        self.classifier_service = ClassifierService()
        self.logger = logging.getLogger("processing_service")

    def parse_message_body(self, body):
        """
        Parses the body of a message and returns the photoId.
        Args:
            body: The body of the message.

        Returns: The photoId from the message body.

        """
        stripped_body = body.decode().strip("'b")
        task = json.loads(stripped_body)

        if "photoId" not in task:
            self.logger.error(f"Message malformed")
            return None

        return task['photoId']

    @retry(
        stop=stop_never,
        wait=wait_exponential(min=10, max=600),
        before_sleep=before_sleep_log(logging.getLogger(), logging.DEBUG),
        reraise=True
    )
    async def setup_channel(self):
        """
        Setup RabbitMQ connection and returns a channel.
        Returns: The RabbitMQ channel.
        """
        self.logger.info("Setting up RabbitMQ channel...")

        try:
            connection = await aio_pika.connect_robust(
                host=RABBITMQ_HOST,
                port=RABBITMQ_PORT,
                login=RABBITMQ_USERNAME,
                password=RABBITMQ_PASSWORD,
            )
            channel = await connection.channel()
        except Exception as e:
            self.logger.error(f"Failed to connect to RabbitMQ: {e}")
            raise e

        self.logger.info("Channel setup complete.")

        return channel

    async def consume(self, channel):
        """Consume messages from the RabbitMQ queue.
        Args:
            channel: The RabbitMQ channel to consume messages from.

        """

        queue = await channel.declare_queue("photo.tagging.request", durable=True)

        async with queue.iterator() as queue_iter:
            self.logger.info("Waiting for messages...")

            async for message in queue_iter:
                async with message.process(requeue=False):
                    message_id = (
                        message.properties.message_id
                        if message.properties and message.properties.message_id
                        else str(uuid.uuid4())
                    )

                    self.logger.info(f"Received message: {message_id}")

                    try:
                        self.logger.info(f"Attempting to process: {message_id}")
                        tag = await self.process_message(message.body, message_id)
                        if tag:
                            await channel.default_exchange.publish(
                                aio_pika.Message(body=json.dumps(tag).encode()),
                                routing_key="photo.tagging.response"
                            )
                            self.logger.debug(f"Published response for {message_id}: {json.dumps(tag)}")
                            self.logger.info(f"Finished processing message: {message_id}")
                    except Exception as e:
                        self.logger.error(f"Error processing message {message_id}: {e}")

    async def process_message(self, body: bytes, message_id: str = None):
        """Processes a message from the queue.

        This method fetches a photo from the API service, classifies it using the ClassifierService,
        and returns the result.

        Args:
            body: The body of the message.
            message_id: The ID of the message.

        Returns: The result of processing the message.
        """

        try:
            self.logger.info(f"Processing message: {message_id}")

            photo_id = self.parse_message_body(body)

            self.logger.info(f"Fetching photo {photo_id} for message: {message_id}")

            photo_data = await self.api_service.fetch_photo(photo_id)

            if photo_data is None:
                self.logger.info(f"Failed to fetch photo {photo_id} for message: {message_id}")
                return None
            else:
                tag = await self.classifier_service.classify(photo_data)
                self.logger.info(f"Generated tag for photo {photo_id}: {tag} for message: {message_id}")

                response = tag.copy()
                response["photoId"] = photo_id

                return response

        except json.JSONDecodeError as e:
            self.logger.error(f"Failed to decode message body {message_id}: {e}")
        except Exception as e:
            self.logger.error(f"An unexpected error occurred while processing {message_id}: {e}")
