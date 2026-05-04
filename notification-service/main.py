from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from database import Base, engine, get_db
from models import Notification
from consumer import start_consumer_thread
from contextlib import asynccontextmanager
import logging
import time
from sqlalchemy.exc import OperationalError

# Configuración de logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def init_db():
    """Intenta conectar a la BD con reintentos."""
    retries = 5
    while retries > 0:
        try:
            # Intentamos crear las tablas
            Base.metadata.create_all(bind=engine)
            logger.info("Tablas creadas exitosamente.")
            return
        except OperationalError:
            logger.warning(f"BD no lista, reintentando en 5s... ({retries} intentos restantes)")
            time.sleep(5)
            retries -= 1
    raise Exception("No se pudo conectar a la base de datos tras varios intentos.")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # 1. Aseguramos que la BD esté lista
    init_db()

    # 2. Arrancamos Kafka
    logger.info("Arrancando notification-service...")
    start_consumer_thread()
    yield
    logger.info("Apagando notification-service...")

app = FastAPI(
    title="Notification Service",
    description="Servicio de notificaciones con IA para MediTech",
    version="1.0.0",
    lifespan=lifespan
)

@app.get("/notifications/{user_id}")
def get_notifications(user_id: int, db: Session = Depends(get_db)):
    """Obtiene todas las notificaciones de un usuario ordenadas por fecha"""
    notifications = (
        db.query(Notification)
        .filter(Notification.user_id == user_id)
        .order_by(Notification.created_at.desc())
        .all()
    )
    return [n.to_dict() for n in notifications]

@app.get("/health")
def health_check():
    return {"status": "UP", "service": "notification-service"}