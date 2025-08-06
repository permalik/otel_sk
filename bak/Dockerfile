# Dockerfile for python-app

FROM python:3.11-slim

WORKDIR /app

# Copy requirements.txt and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application code
COPY app.py .

CMD ["python", "app.py"]

