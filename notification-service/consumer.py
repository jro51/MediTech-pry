import json
import threading
import logging
import os
from confluent_kafka import Consumer, KafkaException
from sqlalchemy.orm import Session
from database import SessionLocal
from models import Notification
from ai_service import generate_recommendation
from dotenv import load_dotenv

load_dotenv()

# Configuración de Logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def process_purchase_event(event_data: dict, db: Session):
    try:
        user_id = event_data.get("userId")
        purchase_id = event_data.get("purchaseId")
        product_names = event_data.get("productNames", [])
        total = event_data.get("total", 0.0)

        logger.info(f"Procesando compra #{purchase_id} del usuario #{user_id}")

        # Intentamos obtener la recomendación (ya sea Real o Mock)
        try:
            ai_message = generate_recommendation(product_names, total)
        except Exception as e:
            logger.error(f"Fallo en servicio de IA: {e}. Usando mensaje por defecto.")
            ai_message = "Gracias por tu compra. Por favor, lee las instrucciones del empaque y consulta a tu médico ante cualquier duda."

        # Guardamos en la base de datos
        notification = Notification(
            user_id=user_id,
            purchase_id=purchase_id,
            message=ai_message
        )
        db.add(notification)
        db.commit()

        logger.info(f"Notificación guardada para usuario #{user_id}")

    except Exception as e:
        db.rollback()
        logger.error(f"Error crítico en proceso de evento: {e}")


def start_consumer():
    """Inicia el consumidor de Kafka usando confluent-kafka"""
    logger.info("Iniciando consumidor de Kafka (Confluent)...")

    conf = {
        'bootstrap.servers': os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
        'group.id': os.getenv("KAFKA_GROUP_ID", "notification-group"),
        'auto.offset.reset': 'earliest'
    }

    consumer = Consumer(conf)
    topic = os.getenv("KAFKA_TOPIC", "purchase-events")
    consumer.subscribe([topic])

    logger.info(f"Escuchando topic: {topic}")

    try:
        while True:
            # poll espera 1 segundo por un mensaje
            msg = consumer.poll(timeout=1.0)

            if msg is None:
                continue
            if msg.error():
                logger.error(f"Error de Kafka: {msg.error()}")
                continue

            # Procesar el mensaje
            try:
                event_data = json.loads(msg.value().decode('utf-8'))
                db = SessionLocal()
                try:
                    process_purchase_event(event_data, db)
                finally:
                    db.close()
            except Exception as e:
                logger.error(f"Error al decodificar mensaje: {e}")

    except KeyboardInterrupt:
        logger.info("Consumidor detenido manualmente.")
    finally:
        consumer.close()


def start_consumer_thread():
    """Inicia el consumer en un thread para no bloquear FastAPI"""
    thread = threading.Thread(target=start_consumer, daemon=True)
    thread.start()
    return thread