FROM python:3-slim

WORKDIR /app

RUN pip install pika redis haversine

COPY worker.py .

ENTRYPOINT [ "python", "./worker.py" ]

# Build: docker build -t dst/ass3-worker .
# Run: docker run --rm --network=dst dst/ass3-worker at_vienna