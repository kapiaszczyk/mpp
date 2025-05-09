# Mini Photo Platform

A simple platform for storing photos.

## Features

- Basic upload/download operations on photos
- Album creation
- Shareable albums between accounts

## High-level architecture

- Three main services
  - mpp-core - business logic   
  - mpp-client - frontend
  - mpp-classifier - service for classfying photos based on content
- RabbitMQ
  - used for async communication between mpp-core and mpp-classifier, currently only for sending requests to classify a newly uploaded photo
- MongoDB
  - storage of the photos and all other data

## Built with

- mpp-core - Java + Spring Boot
- mpp-client - Angular
- mpp-classifier - Python + FastAPI

## How to run

Using Docker:
- All services come with `Dockerfile` files, so you can build images locally
- `docker-compose.yaml` for running with docker compose

Locally:
- all services come with some kind of build information (for mpp-core its Maven's pom.xml, mpp-classifier comes with `requrements`)
