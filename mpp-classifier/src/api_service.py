import logging
import os

import aiohttp
from tenacity import retry, wait_exponential, before_sleep_log, stop_never

LOG_LEVEL = os.environ.get("LOG_LEVEL", "INFO")
MPP_CORE_PORT = str(os.environ.get("MPP-CORE-PORT", 8080))
MPP_CORE_HOST = os.environ.get("MPP_CORE_HOST", "localhost")
MPP_CORE_URL = f"http://{MPP_CORE_HOST}:{MPP_CORE_PORT}"
MPP_CORE_PHOTO_URL = MPP_CORE_URL + "/photos/internal/photo/"
MPP_CORE_AUTH_URL = MPP_CORE_URL + "/auth/internal-service/token"
MPP_CORE_API_KEY = os.getenv("MPP_API_KEY", "tagging-service-key")

LOG_FORMAT = (
    "%(asctime)s - %(levelname)s - %(name)s - %(message)s"
)
logging.basicConfig(
    level=logging.getLevelName(LOG_LEVEL),
    format=LOG_FORMAT,
    datefmt="%Y-%m-%d %H:%M:%S",
)


class ApiService:
    """
    Service responsible for fetching photos from the mpp-core service.

    This service is responsible for fetching photos from the mpp-core service.
    It uses a JWT token to authenticate with the mpp-core service and fetch photos.

    Attributes:
        jwt_token: The JWT token used to authenticate with the mpp-core service.
        logger: The logger used to log messages
    """

    def __init__(self):
        self.jwt_token = None
        self.logger = logging.getLogger("api_service")

    async def get_new_jwt_token(self):
        """Request a new JWT token from the mpp-core service."""
        self.logger.info(f"Fetching new JWT token from address: {MPP_CORE_AUTH_URL} with API key: {MPP_CORE_API_KEY}")
        async with aiohttp.ClientSession() as session:
            async with session.post(MPP_CORE_AUTH_URL, headers={"Authorization": MPP_CORE_API_KEY}) as response:
                if response.status != 200:
                    error_details = await response.text()
                    self.logger.error(f"Failed to fetch JWT token: {response.status}, {error_details}")
                    raise Exception(f"Failed to fetch JWT token: {response.status}, {error_details}")

                data = await response.json()
                self.jwt_token = data

    async def get_token(self):
        """Returns a valid JWT token and fetches a new one if necessary.

        Returns: The JWT token as a string.
        """
        if not self.jwt_token:
            await self.get_new_jwt_token()
            self.logger.info("Fetched new JWT token.")
            return self.jwt_token["token"]
        else:
            self.logger.info("Using existing JWT token.")
            return self.jwt_token["token"]

    @retry(
        stop=stop_never,
        wait=wait_exponential(min=2, max=600),
        before_sleep=before_sleep_log(logging.getLogger(), logging.DEBUG),
        reraise=True
    )
    async def fetch_photo(self, photo_id: str):
        """Retrieves a photo from the mpp-core service.

        Args:
            photo_id: The ID of the photo to fetch.
        Returns: The photo data as bytes.
        """
        token = await self.get_token()
        async with aiohttp.ClientSession() as session:
            async with session.get(f"{MPP_CORE_PHOTO_URL}{photo_id}",
                                   headers={"Authorization": f"Bearer {token}"}) as response:
                if response.status == 404:
                    self.logger.error(f"Photo not found: {photo_id}")
                    return None
                elif response.status == 401:
                    self.logger.error(f"Unauthorized to fetch photo: {response.status}")
                    self.jwt_token = None
                    await self.get_new_jwt_token()
                    raise Exception(f"Unauthorized to fetch photo: {response.status}")
                elif response.status != 200:
                    self.logger.error(f"Failed to fetch photo: {response.status}")
                    return None

                photo_data = await response.read()

                self.logger.info(f"Fetched photo {photo_id}")
                return photo_data
