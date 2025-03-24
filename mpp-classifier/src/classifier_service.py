import json
import logging
import os
from io import BytesIO

import numpy as np
import tensorflow as tf
from PIL import Image

LOG_LEVEL = os.environ.get("LOG_LEVEL", "INFO")
LOG_FORMAT = (
    "%(asctime)s - %(levelname)s - %(name)s - %(message)s"
)
MODEL_PATH = "model-files/2.tflite"
LABELS_PATH = "model-files/imagenet-simple-labels.json"
logging.basicConfig(
    level=logging.getLevelName(LOG_LEVEL),
    format=LOG_FORMAT,
    datefmt="%Y-%m-%d %H:%M:%S",
)


class ClassifierService:
    """Service responsible for classifying images using a TensorFlow Lite model.

        This service is responsible for loading a TensorFlow Lite model and using it to classify images.
        The model is loaded from a file located at 'model-files/2.tflite', and the labels are loaded from
        a file located at 'model-files/imagenet-simple-labels.json'.

        The model is used to classify images by reading an image, preprocessing it, and then passing it to the model.

        Attributes:
            __interpreter: The TensorFlow Lite interpreter used to classify images.
            __labels: The labels used to map the output of the model to human-readable tags.
            logger: The logger used to log messages.
    """

    def __init__(self):
        self.__interpreter = None
        self.__labels = None
        self.setup()
        self.logger = logging.getLogger("classifier_service")

    async def classify(self, image_data: bytes):
        return self.classify_image(image_data)

    def setup(self):
        """
        Set up the TensorFlow Lite interpreter and load labels.
        Returns:

        """
        self.setup_interpreter()
        self.load_labels()

    def setup_interpreter(self):
        self.__interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
        self.__interpreter.allocate_tensors()

    def load_labels(self):
        """
        Load labels from a JSON file.

        This method attempts to load image classification labels from a JSON file
        located at 'model-files/imagenet-simple-labels.json'. If the file is not found,
        an error message is logged.

        Those labels are used to map the output of the model to human-readable tags.

        Raises:
            FileNotFoundError: If the labels file is not found.
        """
        try:
            with open(LABELS_PATH, "r", encoding="utf-8") as f:
                self.__labels = json.load(f)
        except FileNotFoundError:
            self.logger.error("Labels file not found.")

    @staticmethod
    def read_image(image_data):
        """
        Read an image and convert it to a format that can be used by the TensorFlow Lite model.

        This is needed because the model expects images to be in a specific format, the same format
        that the training data was in.
        Args:
            image_data: The image data to read.

        Returns: The image data as a NumPy array.
        """
        image = Image.open(BytesIO(image_data))
        image = image.resize((224, 224)).convert("RGB")
        image_array = np.array(image) / 255.0
        return np.expand_dims(image_array, axis=0).astype(np.float32)

    def classify_image(self, image_data):
        """Classify an image using the TensorFlow Lite model.

        This method reads an image, preprocesses it, and then classifies it using the TensorFlow Lite model.

        Args:
            image_data: The image data to classify.

        Returns:
            A dictionary containing the tag and confidence of the top prediction.
        """
        try:
            input_data = self.read_image(image_data)

            """Retrieve metadata about the input tensor and output tensor."""
            input_index = self.__interpreter.get_input_details()[0]['index']
            output_index = self.__interpreter.get_output_details()[0]['index']

            """Set the input tensor and invoke the interpreter."""
            self.__interpreter.set_tensor(input_index, input_data)
            self.__interpreter.invoke()

            """Retrieve the output tensor and extract the top prediction."""
            predictions = self.__interpreter.get_tensor(output_index)[0]
            top_index = np.argmax(predictions)
            confidence = predictions[top_index]
            return {"tag": self.__labels[top_index] if self.__labels else "Unknown", "confidence": float(confidence)}
        except Exception as e:
            self.logger.error(f"Error while classifying image: {e}")
            return None
