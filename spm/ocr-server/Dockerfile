FROM python:3.12-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY server.py .
RUN pip install --no-cache-dir flask

EXPOSE 8080
CMD ["python", "server.py"]
