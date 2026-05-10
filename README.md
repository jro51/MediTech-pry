![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-6DB33F?logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.0-6DB33F?logo=spring&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0.115-009688?logo=fastapi&logoColor=white)
![MySQL](https://img.shields.io/badge/Database-MySQL_8.0-4479A1?logo=mysql&logoColor=white)
![Kafka](https://img.shields.io/badge/Messaging-Apache_Kafka-231F20?logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED?logo=docker&logoColor=white)
![Claude AI](https://img.shields.io/badge/AI-Claude_Anthropic-7C3AED?logo=anthropic&logoColor=white)

# MediTech — Plataforma de E-commerce de Medicamentos

> Plataforma de comercio electrónico de medicamentos construida con arquitectura de microservicios. Integra comunicación síncrona (OpenFeign), mensajería asíncrona (Apache Kafka) y un servicio de recomendaciones médicas generadas por Inteligencia Artificial (Claude de Anthropic).

---

## Descripción

**MediTech** es un sistema distribuido que permite a usuarios registrarse, explorar un catálogo de medicamentos, realizar compras y recibir recomendaciones médicas personalizadas generadas por IA después de cada compra.

El proyecto fue diseñado siguiendo principios de arquitectura limpia: cada microservicio es autónomo, tiene su propia base de datos, se comunica a través de interfaces bien definidas y puede desplegarse de forma independiente.

---

## Arquitectura del Sistema

```
React Frontend (5173)
        │
        ▼
  API Gateway (8080)
  JWT · Rate Limiting · CORS · Enrutamiento
        │
  ┌─────┼──────────────────────────────────┐
  │     │                                  │
  ▼     ▼                                  ▼
auth  product                           purchase
(8081) (8082)                           (8083)
  │     │                                  │
  ▼     ▼                          Kafka (9092)
auth  product                             │
 _db   _db                                ▼
                                  notification-service
                                     Python (8084)
                                          │
                                   Claude Anthropic API
                                          │
                                   notification_db
                                       (3310)

Eureka Server (8761) ← todos los servicios se registran aquí
Config Server (8888) ← todos los servicios leen su configuración aquí
```

---

## Resumen Técnico

| Componente | Tecnología | Puerto |
|---|---|---|
| **Config Server** | Spring Cloud Config | 8888 |
| **Eureka Server** | Spring Cloud Netflix Eureka | 8761 |
| **API Gateway** | Spring Cloud Gateway + JWT | 8080 |
| **Auth Service** | Spring Boot + Spring Security + BCrypt | 8081 |
| **Product Service** | Spring Boot + JPA | 8082 |
| **Purchase Service** | Spring Boot + OpenFeign + Kafka | 8083 |
| **Notification Service** | Python + FastAPI + Kafka + Claude AI | 8084 |
| **Bases de datos** | MySQL 8.0 (una por servicio) | 3307–3310 |
| **Mensajería** | Apache Kafka + Zookeeper | 9092 / 2181 |

### Patrones y prácticas aplicadas

- **Database per Service** — cada microservicio tiene su propia base de datos aislada
- **API Gateway Pattern** — punto de entrada único con validación JWT centralizada
- **Service Discovery** — Eureka registra y resuelve los servicios dinámicamente
- **Centralized Configuration** — Config Server distribuye la configuración a todos los servicios
- **Event-Driven Architecture** — Kafka desacopla la compra de la notificación
- **Synchronous Communication** — OpenFeign para llamadas entre purchase-service y product-service
- **Polyglot Architecture** — Java (Spring Boot) para servicios de negocio, Python (FastAPI) para el servicio de IA
- **Clean Architecture** — separación en capas: controller → service (interfaz + implementación) → repository
- **DTO Pattern** — nunca se exponen entidades directamente en los endpoints

---

## Estructura del Proyecto

```
MediTech/
├── docker-compose.yaml              ← levanta toda la infraestructura
├── .gitignore
│
├── config-server/                   ← configuración centralizada (Puerto 8888)
│   └── src/main/resources/
│       ├── application.yaml
│       └── configs/
│           ├── api-gateway.yaml
│           ├── auth-service.yaml
│           ├── product-service.yaml
│           ├── purchase-service.yaml
│           └── notification-service.yaml
│
├── eureka-server/                   ← service discovery (Puerto 8761)
│
├── api-gateway/                     ← puerta de entrada + JWT (Puerto 8080)
│   └── src/main/java/
│       ├── config/SecurityConfig.java
│       └── filter/JwtAuthenticationFilter.java
│
├── auth-service/                    ← registro y login (Puerto 8081)
│   └── src/main/java/
│       ├── controller/AuthController.java
│       ├── dto/
│       ├── entity/User.java
│       ├── repository/UserRepository.java
│       └── security/
│           ├── JwtUtils.java
│           └── SecurityConfig.java
│
├── product-service/                 ← catálogo y stock (Puerto 8082)
│   └── src/main/java/
│       ├── controller/ProductController.java
│       ├── dto/
│       ├── entity/Product.java
│       ├── exception/
│       ├── repository/ProductRepository.java
│       └── service/
│           ├── ProductService.java
│           └── ProductServiceImpl.java
│
├── purchase-service/                ← compras + Kafka producer (Puerto 8083)
│   └── src/main/java/
│       ├── client/ProductClient.java        ← Feign client
│       ├── controller/PurchaseController.java
│       ├── dto/
│       ├── entity/Purchase.java
│       ├── exception/
│       ├── messaging/PurchaseEventPublisher.java
│       ├── repository/PurchaseRepository.java
│       └── service/
│           ├── PurchaseService.java
│           └── PurchaseServiceImpl.java
│
└── notification-service/            ← IA + Kafka consumer (Puerto 8084)
    ├── main.py                      ← FastAPI app
    ├── consumer.py                  ← Kafka consumer thread
    ├── ai_service.py                ← Claude Anthropic API
    ├── database.py                  ← SQLAlchemy config
    ├── models.py                    ← Notification model
    ├── requirements.txt
    └── Dockerfile
```

---

## Requisitos Previos

Antes de ejecutar el proyecto asegúrate de tener instalado:

- **Java 17+** (se recomienda Amazon Corretto 17)
- **Maven 3.9+**
- **Python 3.12+**
- **Docker Desktop** (con Docker Compose incluido)
- **Git**

---

## Configuración Inicial

### 1. Clonar el repositorio

```bash
git clone https://github.com/<tu-usuario>/MediTech.git
cd MediTech
```

### 2. Configurar la API Key de Anthropic

El `notification-service` necesita una API Key de Anthropic para generar recomendaciones con IA.

Regístrate gratis en [console.anthropic.com](https://console.anthropic.com) y crea una API Key. Luego crea el archivo `.env` en la carpeta `notification-service/`:

```bash
# notification-service/.env
DB_HOST=localhost
DB_PORT=3310
DB_NAME=notification_db
DB_USER=root
DB_PASSWORD=root

KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC=purchase-events
KAFKA_GROUP_ID=notification-group

ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxxxxx

HOST=0.0.0.0
PORT=8084
```

> El archivo `.env` está incluido en `.gitignore` y nunca debe subirse al repositorio.

### 3. Preparar el entorno virtual de Python

```bash
cd notification-service

# Crear entorno virtual
python -m venv venv

# Activar (Windows)
venv\Scripts\activate

# Activar (Mac/Linux)
source venv/bin/activate

# Instalar dependencias
pip install -r requirements.txt
```

---

## Cómo Ejecutar el Proyecto

El proyecto tiene dos modos de ejecución: **desarrollo** (servicios Java en IntelliJ + Docker para infraestructura) y **completo con Docker** (todo en contenedores).

---

### Modo Desarrollo (recomendado para desarrollo activo)

#### Paso 1 — Levantar la infraestructura con Docker

```bash
# Desde la raíz del proyecto
docker-compose up -d auth-db product-db purchase-db notification-db zookeeper kafka
```

Verifica que los contenedores estén corriendo:

```bash
docker ps
```

Debes ver 6 contenedores activos: las 4 bases de datos, zookeeper y kafka.

#### Paso 2 — Compilar todos los servicios Java

Antes de correr cualquier servicio, compila todos los módulos desde IntelliJ o desde la terminal:

```bash
# Desde cada carpeta de servicio Java
cd config-server && mvn clean package -DskipTests && cd ..
cd eureka-server && mvn clean package -DskipTests && cd ..
cd auth-service && mvn clean package -DskipTests && cd ..
cd product-service && mvn clean package -DskipTests && cd ..
cd purchase-service && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
```

#### Paso 3 — Arrancar los servicios Java en orden

Abre IntelliJ y arranca cada servicio esperando que el anterior esté completamente iniciado (verás `Started XxxApplication` en los logs):

```
1. ConfigServerApplication     → Puerto 8888
2. EurekaServerApplication      → Puerto 8761
3. AuthServiceApplication       → Puerto 8081
4. ProductServiceApplication    → Puerto 8082
5. PurchaseServiceApplication   → Puerto 8083
6. ApiGatewayApplication        → Puerto 8080 (último)
```

> El orden es obligatorio. El Config Server debe estar listo antes que cualquier otro servicio, y el API Gateway debe arrancar al final.

#### Paso 4 — Arrancar el notification-service Python

```bash
cd notification-service
venv\Scripts\activate        # Windows
# source venv/bin/activate   # Mac/Linux

python -m uvicorn main:app --host 0.0.0.0 --port 8084 --reload
```

Verifica que el servicio esté activo:
```
http://localhost:8084/health
→ {"status": "UP", "service": "notification-service"}
```

#### Paso 5 — Verificar Eureka

Abre el dashboard de Eureka en el navegador:

```
http://localhost:8761
```

Debes ver todos los servicios registrados en verde:

```
AUTH-SERVICE         UP (1) - localhost:auth-service:8081
PRODUCT-SERVICE      UP (1) - localhost:product-service:8082
PURCHASE-SERVICE     UP (1) - localhost:purchase-service:8083
API-GATEWAY          UP (1) - localhost:api-gateway:8080
```

---

### Modo Docker Completo

Para levantar todo el sistema con un solo comando (requiere compilar los JAR primero):

```bash
# 1. Compilar todos los JAR
cd config-server && mvn clean package -DskipTests && cd ..
cd eureka-server && mvn clean package -DskipTests && cd ..
cd auth-service && mvn clean package -DskipTests && cd ..
cd product-service && mvn clean package -DskipTests && cd ..
cd purchase-service && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..

# 2. Levantar todo con Docker Compose
docker-compose up -d

# 3. Arrancar el notification-service Python (aún no está en docker-compose)
cd notification-service
python -m uvicorn main:app --host 0.0.0.0 --port 8084 --reload
```

Para detener todos los contenedores:

```bash
docker-compose down
```

Para detener y eliminar los volúmenes (borra las bases de datos):

```bash
docker-compose down -v
```

---

## Endpoints de la API

Todos los endpoints pasan por el **API Gateway** en `http://localhost:8080`.

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/auth/register` | Registrar nuevo usuario | Pública |
| `POST` | `/api/auth/login` | Iniciar sesión, devuelve JWT | Pública |

**Ejemplo — Registro:**
```json
POST /api/auth/register
{
  "name": "Juan Pérez",
  "phone": "999888777",
  "email": "juan@meditech.com",
  "password": "123456"
}
```

**Ejemplo — Login:**
```json
POST /api/auth/login
{
  "email": "juan@meditech.com",
  "password": "123456"
}

// Respuesta:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan@meditech.com",
  "role": "USER"
}
```

---

### Productos

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/products` | Listar todos los productos | Pública |
| `GET` | `/api/products/{id}` | Ver producto por ID | Pública |
| `POST` | `/api/products` | Crear producto | ADMIN |
| `PUT` | `/api/products/{id}` | Actualizar producto | ADMIN |
| `DELETE` | `/api/products/{id}` | Eliminar producto | ADMIN |

**Ejemplo — Crear producto (requiere rol ADMIN):**
```json
POST /api/products
Authorization: Bearer <token>

{
  "name": "Ibuprofeno 400mg",
  "price": 12.50,
  "stock": 100,
  "description": "Antiinflamatorio no esteroideo para dolor y fiebre",
  "imageSrc": "https://example.com/ibuprofeno.jpg"
}
```

> Para asignar rol ADMIN, actualiza directamente en la base de datos:
> ```sql
> UPDATE users SET role = 'ADMIN' WHERE email = 'juan@meditech.com';
> ```
> Luego vuelve a hacer login para obtener un token con el rol actualizado.

---

### Compras

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/purchases/buy` | Realizar una compra | JWT requerido |
| `GET` | `/api/purchases/user/{userId}` | Ver historial de compras | JWT requerido |
| `DELETE` | `/api/purchases/{id}` | Eliminar compra | JWT requerido |

**Ejemplo — Realizar compra:**
```json
POST /api/purchases/buy
Authorization: Bearer <token>

{
  "productIds": [1, 2]
}

// Respuesta:
{
  "id": 1,
  "userId": 1,
  "products": [
    { "productId": 1, "productName": "Ibuprofeno 400mg", "price": 12.50 },
    { "productId": 2, "productName": "Paracetamol 500mg", "price": 5.99 }
  ],
  "total": 18.49,
  "purchaseDate": "2026-05-10T10:30:00"
}
```

---

### Notificaciones con IA

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/notifications/{userId}` | Ver recomendaciones del usuario | JWT requerido |

**Ejemplo — Ver notificaciones:**
```json
GET /api/notifications/1
Authorization: Bearer <token>

// Respuesta (generada por Claude):
[
  {
    "id": 1,
    "userId": 1,
    "message": "El Ibuprofeno 400mg y el Paracetamol 500mg pueden tomarse juntos con precaución. Tome ambos con alimentos para proteger el estómago y respete los intervalos de dosificación. Consulte a su médico si los síntomas persisten más de 3 días.",
    "purchaseId": 1,
    "createdAt": "2026-05-10T10:30:05"
  }
]
```

---

## Flujo Completo del Sistema

```
1. Usuario hace POST /api/purchases/buy + JWT
        │
        ▼
2. API Gateway valida el JWT
   Extrae userId, role, email del token
   Agrega headers internos: X-User-Id, X-User-Role, X-User-Email
        │
        ▼
3. purchase-service recibe la petición
   Llama a product-service via OpenFeign para verificar stock
   Reduce el stock de cada producto comprado
   Guarda la compra en purchase_db
        │
        ▼
4. purchase-service publica evento en Kafka
   Topic: purchase-events
   Payload: { purchaseId, userId, productNames, total, ... }
        │
        ▼
5. notification-service Python consume el evento
   Llama a Claude (Anthropic API) con los nombres de los medicamentos
   Genera recomendación médica personalizada
   Guarda la notificación en notification_db
        │
        ▼
6. Usuario hace GET /api/notifications/1
   Obtiene la recomendación generada por IA
```

---

## Variables de Entorno

### Servicios Java (application.yaml)

Los servicios Java leen su configuración desde el Config Server. Las variables clave son:

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `JWT_SECRET` | Clave secreta para firmar los tokens | `meditech-super-secret-key-...` |
| `DB_USER` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | `root` |
| `DB_HOST` | Host de la base de datos | `localhost` |

### Notification Service Python (.env)

| Variable | Descripción | Requerido |
|----------|-------------|----|
| `ANTHROPIC_API_KEY` | API Key de Anthropic para Claude | Sí |
| `DB_HOST` | Host de notification_db | Sí |
| `DB_PORT` | Puerto de notification_db | Sí |
| `DB_NAME` | Nombre de la base de datos | Sí |
| `DB_USER` | Usuario MySQL | Sí |
| `DB_PASSWORD` | Contraseña MySQL | Sí |
| `KAFKA_BOOTSTRAP_SERVERS` | Dirección del broker Kafka | Sí |
| `KAFKA_TOPIC` | Topic que consume | `purchase-events` |
| `KAFKA_GROUP_ID` | Grupo del consumer | `notification-group` |

---

## Solución de Problemas Frecuentes

### Los servicios no aparecen en Eureka
Verifica que el Config Server esté corriendo antes de arrancar cualquier otro servicio. El `application.yaml` de cada servicio apunta al Config Server para obtener su configuración.

### Error de conexión a MySQL al arrancar
Asegúrate de que los contenedores de Docker estén corriendo antes de arrancar los servicios Java:
```bash
docker ps  # deben aparecer auth-db, product-db, purchase-db, notification-db
```

### El notification-service no recibe eventos de Kafka
Verifica que Kafka esté corriendo y que el `purchase-service` tenga la dependencia `spring-kafka` en su `pom.xml`. Revisa los logs del `purchase-service` para confirmar que el evento se publicó.

### Error `ModuleNotFoundError: No module named 'kafka.vendor.six.moves'`
Este error ocurre con `kafka-python` en Python 3.12. Usa `kafka-python-ng` en su lugar:
```bash
pip uninstall kafka-python -y
pip install kafka-python-ng==2.2.3
```

### El API Gateway devuelve 401 en rutas que deberían ser públicas
El filtro JWT distingue entre GET y POST para `/api/products`. Los GET son públicos pero los POST (crear producto) requieren token ADMIN.

---

## Contribuir

Las contribuciones se gestionan mediante Pull Requests desde ramas propias contra `main`.

### Crear una rama

```bash
git checkout main
git pull
git checkout -b <prefijo>/<descripcion-corta>
```

Convenciones de nombre de rama:

| Tipo | Prefijo | Ejemplo |
|------|---------|---------|
| Nueva funcionalidad | `feature/` | `feature/circuit-breaker` |
| Corrección de bug | `fix/` | `fix/kafka-consumer-error` |
| Refactor | `refactor/` | `refactor/purchase-service-layers` |
| Documentación | `docs/` | `docs/update-readme` |

### Subir cambios

```bash
git add .
git commit -m "feat: descripción del cambio"
git push origin <prefijo>/<descripcion-corta>
```

Abre el Pull Request en GitHub contra `main` con un título descriptivo.

---

## Próximas Mejoras

- [ ] **Circuit Breaker con Resilience4j** — respuesta de fallback cuando `product-service` no responde
- [ ] **Bean Validation** — validaciones con `@NotNull`, `@NotBlank` en los DTOs
- [ ] **Tests unitarios** — `@SpringBootTest` y `@WebMvcTest` para cada servicio
- [ ] **Frontend actualizado** — migrar React para consumir la API a través del gateway
- [ ] **Docker Compose completo** — incluir el notification-service Python en docker-compose
- [ ] **Zipkin** — trazabilidad distribuida entre servicios

---

> Proyecto desarrollado como demostración de arquitectura de microservicios con Spring Cloud, Apache Kafka e Inteligencia Artificial aplicada a un dominio real.
