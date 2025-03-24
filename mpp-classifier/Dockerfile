# Build dockerfile of the tagging service

# Use the official image as a parent image
FROM python:3.11-slim

# Set the working directory
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY /src/main.py /app
COPY /src/api_service.py /app
COPY /src/classifier_service.py /app
COPY /src/processing_service.py /app
COPY requirements.txt /app
COPY /src/model-files/imagenet-simple-labels.json /app/model-files/
COPY /src/model-files/2.tflite /app/model-files/

# Install any needed packages specified in requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Make port 5000 available to the world outside this container
EXPOSE 5000

# Get environment variables from docker-compose.yml
ARG MPP_CORE_PORT
ARG MPP_CORE_API_KEY
ARG RABBITMQ_HOST
ARG RABBITMQ_PORT
ARG RABBITMQ_USERNAME
ARG RABBITMQ_PASSWORD

# Run app.py when the container launches
CMD ["python", "main.py"]
