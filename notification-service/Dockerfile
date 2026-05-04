FROM python:3.12-slim
WORKDIR /app
# Copia los requerimientos e instálalos
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
# Copia el resto del código
COPY . .
# Comando para iniciar
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]