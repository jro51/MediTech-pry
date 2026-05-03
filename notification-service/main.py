from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from database import Base, engine, get_db
from models import Notification
from consumer import start_consumer_thread
from contextlib import asynccontextmanager
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Crea las tablas al iniciar
Base.metadata.create_all(bind=engine)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Al arrancar: inicia el consumer de Kafka
    logger.info("Arrancando notification-service...")
    start_consumer_thread()
    logger.info("Consumer de Kafka iniciado")
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