# 🐾 Zoonet - Backend API

## 📋 Descripción del Proyecto

**Zoonet** es una plataforma integral de rastreo y gestión de mascotas que combina IoT, inteligencia artificial y comunidad para ayudar a los dueños a mantener a sus mascotas seguras. El backend está desarrollado en **Spring Boot 3.2.0** con **Java 17** y utiliza **MySQL** como base de datos.

### ✨ Características Principales

- 🔐 **Autenticación y Gestión de Usuarios** (Free/Premium)
- 📍 **Rastreo GPS en Tiempo Real** con geocercas inteligentes
- 🤖 **AI Matching** con Google Gemini para identificación de mascotas
- 🌐 **Comunidad** con reportes de mascotas perdidas y avistamientos
- 🔔 **Sistema de Notificaciones** Push (Firebase) y almacenadas en BD
- 💳 **Sistema de Pagos** simulado para plan Premium
- 🛠️ **Soporte Técnico** con sistema de tickets
- 📊 **Historial de Rutas** y métricas de actividad

---

## 🏗️ Arquitectura del Sistema

### Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje base |
| Spring Boot | 3.2.0 | Framework principal |
| Spring Security | 3.2.0 | Autenticación y autorización |
| Spring Data JPA | 3.2.0 | ORM y persistencia |
| MySQL | 8.x | Base de datos relacional |
| Lombok | Latest | Reducción de código boilerplate |
| Firebase Admin SDK | 9.2.0 | Notificaciones Push |
| Google Gson | Latest | Procesamiento JSON |
| Apache HttpClient | Latest | Llamadas HTTP a APIs externas |

### Estructura de Paquetes

```
com.tecsup.pe.back_zonet/
├── config/              # Configuraciones (Security, Firebase, Gemini, CORS)
├── controller/          # Endpoints REST organizados por módulo
│   ├── auth/           # Autenticación y pagos
│   ├── community/      # Comunidad y AI Matching
│   ├── iot/            # Dispositivos IoT (collares)
│   ├── location/       # Rastreo GPS y geocercas
│   ├── notification/   # Notificaciones
│   ├── pet/            # Gestión de mascotas
│   ├── support/        # Tickets de soporte
│   └── user/           # Perfil de usuario
├── dto/                # Data Transfer Objects
├── entity/             # Entidades JPA (modelos de BD)
├── exception/          # Excepciones personalizadas
├── repository/         # Repositorios JPA
├── service/            # Lógica de negocio
└── util/               # Utilidades (validadores, calculadores)
```

---

## 🚀 Instalación y Configuración

### Prerrequisitos

- ☕ **Java 17** o superior
- 🐘 **MySQL 8.x**
- 📦 **Maven 3.9+**
- 🔑 **Firebase Service Account Key** (para notificaciones Push)
- 🤖 **Google Gemini API Key** (para AI Matching)

### Paso 1: Clonar el Repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd back-zoonet
```

### Paso 2: Configurar la Base de Datos

Edita el archivo `src/main/resources/application.properties`:

```properties
# Configuración de MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/zoonet_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Tamaño de archivos (imágenes)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Google Gemini API
gemini.api.key=TU_API_KEY_DE_GEMINI
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/
```

### Paso 3: Configurar Firebase

1. Descarga tu archivo `serviceAccountKey.json` desde Firebase Console
2. Colócalo en `src/main/resources/`
3. La configuración en `FirebaseConfig.java` lo cargará automáticamente

### Paso 4: Compilar y Ejecutar

#### Usando Maven Wrapper (Recomendado)

```bash
# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run

# Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

#### Usando Maven Directamente

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

---

## 📡 API Endpoints

### 🔐 Autenticación (`/api/auth`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/register` | Registrar nuevo usuario | `{ name, email, password, plan }` |
| POST | `/login` | Iniciar sesión | `{ email, password }` |

**Ejemplo de Registro:**
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "123456",
  "plan": "FREE"
}
```

---

### 🐶 Mascotas (`/api/pets`)

| Método | Endpoint | Descripción | Body/Params |
|--------|----------|-------------|-------------|
| POST | `/{userId}/register` | Registrar mascota con foto | `multipart/form-data` |
| GET | `/user/{userId}` | Obtener mascota del usuario | - |

**Ejemplo de Registro de Mascota:**
```
POST /api/pets/1/register
Content-Type: multipart/form-data

petName: Max
planType: premium
photo: [archivo de imagen]
```

---

### 💳 Pagos (`/api/payment`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/process/{userId}` | Procesar pago Premium | `{ cardNumber, expirationMonth, expirationYear, cvv }` |
| POST | `/confirm/{userId}` | Confirmar pago (webhook simulado) | - |

**Ejemplo de Pago:**
```json
{
  "cardNumber": "1234567890123456",
  "expirationMonth": "12",
  "expirationYear": "25",
  "cvv": "123"
}
```

---

### 📍 Rastreo GPS (`/api/location/tracker`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/report` | Reportar ubicación (dispositivo IoT) | `{ petId, latitude, longitude, batteryLevel }` |
| GET | `/current/{petId}` | Obtener última ubicación | - |

**Ejemplo de Reporte de Ubicación:**
```json
{
  "petId": 1,
  "latitude": -12.0464,
  "longitude": -77.0428,
  "batteryLevel": 85.5
}
```

---

### 🛡️ Zonas Seguras (`/api/location/safezones`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/` | Crear zona segura | `{ userId, name, latitude, longitude, radius, address }` |
| GET | `/{userId}` | Obtener zonas del usuario | - |
| PUT | `/{id}` | Actualizar zona | `{ userId, name, latitude, longitude, radius, address }` |
| DELETE | `/{id}` | Eliminar zona | - |

**⚠️ Restricción:** Usuarios Free solo pueden tener **1 zona segura**.

**Ejemplo de Creación:**
```json
{
  "userId": 1,
  "name": "Casa",
  "latitude": -12.0464,
  "longitude": -77.0428,
  "radius": 500,
  "address": "Jr. Los Pinos 123, Lima"
}
```

---

### 📊 Historial de Rutas (`/api/location/routes`)

| Método | Endpoint | Descripción | Query Params |
|--------|----------|-------------|--------------|
| GET | `/{petId}` | Obtener métricas de rutas | `period: semana/mes/año` |

**Respuesta de Ejemplo:**
```json
{
  "totalDistanceKm": 12.5,
  "totalTimeMinutes": 360,
  "totalCalories": 625,
  "totalRoutes": 8
}
```

---

### 🚨 Mascotas Perdidas (`/api/pets/lost`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/` | Reportar mascota perdida | `{ petId, description, hoursLost, lastSeenLocation, lastSeenLatitude, lastSeenLongitude }` |
| GET | `/` | Ver mascotas perdidas activas | - |
| PUT | `/{reportId}/found` | Marcar como encontrada | - |

**⚠️ Restricción:** Usuarios Free pueden tener máximo **3 reportes activos**.

**Ejemplo de Reporte:**
```json
{
  "petId": 1,
  "description": "Visto con collar rojo",
  "hoursLost": 4,
  "lastSeenLocation": "Parque Kennedy",
  "lastSeenLatitude": -12.1196,
  "lastSeenLongitude": -77.0365
}
```

---

### 🌐 Comunidad (`/api/community`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| GET | `/posts` | Ver publicaciones de la comunidad | - |
| POST | `/posts/{userId}` | Crear avistamiento | `multipart/form-data` |
| POST | `/comments` | Agregar comentario | `{ postId, userId, content }` |
| POST | `/reactions` | Dar/quitar like (toggle) | `{ postId, userId }` |
| POST | `/contact` | Contactar al autor de un post | `{ postId, name, phone, email, message }` |

**Ejemplo de Avistamiento:**
```
POST /api/community/posts/1
Content-Type: multipart/form-data

description: Vi un perro perdido
locationName: Parque Kennedy
latitude: -12.1196
longitude: -77.0365
photo: [archivo de imagen]
```

---

### 🤖 AI Matching - Google Gemini (`/api/community/ai-matching`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/{userId}` | Buscar mascotas similares con IA | `multipart/form-data` |

**⚠️ Restricción:** **SOLO USUARIOS PREMIUM**

**Cómo Funciona:**
1. El usuario sube una foto de una mascota
2. La IA compara con todas las mascotas reportadas en la comunidad
3. Devuelve coincidencias con **porcentaje de similitud ≥ 40%**
4. Incluye justificación detallada de la IA

**Ejemplo de Uso:**
```
POST /api/community/ai-matching/1
Content-Type: multipart/form-data

photo: [archivo de imagen]
```

**Respuesta de Ejemplo:**
```json
[
  {
    "postId": 42,
    "petName": "Max",
    "description": "Perdido desde ayer",
    "imageUrl": "/uploads/1234567890_dog.jpg",
    "locationName": "Parque Kennedy",
    "timeAgo": "Perdido Hace 1 día",
    "matchPercentage": 87,
    "aiReasoning": "Ambos son Golden Retrievers con pelaje dorado similar. Mismo patrón de manchas blancas en el pecho..."
  }
]
```

**Tecnología Usada:**
- **Modelo:** Gemini 2.5 Flash
- **Comparación:** Imagen vs Imagen (análisis visual multimodal)
- **Criterios:** Raza, color, patrón, tamaño, marcas distintivas

---

### 🔔 Notificaciones (`/api/notifications`)

| Método | Endpoint | Descripción | Params |
|--------|----------|-------------|--------|
| GET | `/{userId}` | Ver notificaciones del usuario | - |

**Tipos de Notificaciones Automáticas:**
- 🚨 **LOST_ALERT:** Mascota perdida (dueño + comunidad)
- 📍 **LOCATION:** Ubicación actualizada
- ⚠️ **ZONE_RISK:** Salió de la geocerca
- 🔋 **LOW_BATTERY:** Batería baja del collar
- ✅ **FOUND:** Mascota marcada como encontrada
- 💬 **CONTACT_MESSAGE:** Alguien contactó sobre tu post
- 📢 **COMMUNITY_ALERT:** Nueva mascota perdida cerca

**Respuesta de Ejemplo:**
```json
[
  {
    "id": 1,
    "title": "🚨 ALERTA DE EMERGENCIA: Max PERDIDO!",
    "message": "Tu mascota se perdió en Parque Kennedy",
    "read": false,
    "type": "LOST_ALERT",
    "createdAt": "2024-12-04T10:30:00",
    "urgencyLevel": "HIGH"
  }
]
```

**Notificaciones Push (Firebase):**
- Se envían automáticamente si el usuario tiene `fcmToken` registrado
- Actualizarlo con: `PUT /api/user/profile/{userId}/fcm-token`

---

### 🛠️ Dispositivos IoT (`/api/devices`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/{petId}/action` | Conectar/Desconectar collar | `{ action: "connect" | "disconnect" | "search" }` |
| GET | `/{petId}/status` | Ver estado del dispositivo | - |

**Estados Posibles:**
- `CONNECTED`: Collar conectado vía Bluetooth
- `DISCONNECTED`: Sin conexión
- `SEARCHING`: Buscando dispositivo

**Ejemplo de Conexión:**
```json
{
  "action": "connect"
}
```

---

### 👤 Perfil de Usuario (`/api/user/profile`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| GET | `/{userId}` | Ver perfil | - |
| PUT | `/{userId}` | Actualizar perfil | `{ name, email, password }` |
| DELETE | `/{userId}` | Eliminar cuenta | - |
| PUT | `/{userId}/fcm-token` | Registrar token para Push | `{ token }` |

**Ejemplo de Actualización:**
```json
{
  "name": "Juan Carlos Pérez",
  "email": "juancarlos@example.com",
  "password": "nuevaContraseña123"
}
```

---

### 💼 Suscripciones (`/api/subscriptions`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| GET | `/{userId}` | Ver plan actual y fechas | - |
| POST | `/{userId}` | Seleccionar plan | `{ planType: "free" | "premium" }` |

**Respuesta de Ejemplo:**
```json
{
  "id": 1,
  "plan": "PREMIUM",
  "startDate": "2024-12-01",
  "endDate": "2025-01-01"
}
```

---

### 🎫 Soporte Técnico (`/api/support/tickets`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/` | Crear ticket | `{ userId, subject, description }` |
| GET | `/{userId}` | Ver tickets del usuario | - |

**Ejemplo de Ticket:**
```json
{
  "userId": 1,
  "subject": "No puedo ver mi mascota en el mapa",
  "description": "Al entrar a la app, la ubicación no se actualiza..."
}
```

---

## 🗄️ Modelo de Base de Datos

### Entidades Principales

#### **User** (Usuarios)
- `id`, `name`, `email`, `password`, `plan` (FREE/PREMIUM), `fcmToken`
- **Relaciones:** Mascotas, Posts, Comentarios, Reacciones, Notificaciones, Zonas Seguras, Suscripción

#### **Pet** (Mascotas)
- `id`, `name`, `photoUrl`, `nextVaccinationDate`, `deviceStatus`
- **Relaciones:** Usuario (dueño), Ubicaciones, Reportes de Pérdida

#### **Location** (Ubicaciones GPS)
- `id`, `latitude`, `longitude`, `timestamp`, `isRealTime`, `batteryLevel`
- **Relación:** Mascota

#### **SafeZone** (Zonas Seguras)
- `id`, `name`, `latitude`, `longitude`, `radius` (metros), `address`
- **Relación:** Usuario

#### **LostPet** (Reportes de Mascotas Perdidas)
- `id`, `reportDate`, `hoursLost`, `description`, `lastSeenLocation`, `found`
- **Relaciones:** Mascota, CommunityPost

#### **CommunityPost** (Publicaciones Comunitarias)
- `id`, `postType` (LOST_ALERT/SIGHTING), `description`, `imageUrl`, `locationName`
- **Relaciones:** Usuario (autor), LostPet (si aplica), Comentarios, Reacciones

#### **Comment** (Comentarios)
- `id`, `content`, `createdAt`
- **Relaciones:** Post, Usuario

#### **Reaction** (Reacciones/Likes)
- `id`, `createdAt`
- **Relaciones:** Post, Usuario (restricción única por par)

#### **Notification** (Notificaciones)
- `id`, `title`, `message`, `type`, `isRead`, `urgencyLevel`, `sentViaSMS`
- **Relación:** Usuario (receptor)

#### **AiMatchHistory** (Historial de Búsquedas con IA)
- `id`, `matchPercentage`, `aiReasoning` (LONGTEXT), `searchDate`
- **Relaciones:** Usuario (quien buscó), CommunityPost (coincidencia)

#### **Subscription** (Suscripciones Premium)
- `id`, `plan`, `startDate`, `endDate`
- **Relación:** Usuario (1:1)

#### **SupportTicket** (Tickets de Soporte)
- `id`, `subject`, `description`, `status` (OPEN/IN_PROGRESS/RESOLVED), `createdAt`
- **Relación:** Usuario

---

## 🔒 Seguridad y Restricciones

### Restricciones por Plan

| Funcionalidad | Plan FREE | Plan PREMIUM |
|---------------|-----------|--------------|
| Zonas Seguras | 1 zona | Ilimitadas |
| Reportes de Mascotas Perdidas | Máx. 3 activos | Ilimitados |
| Rastreo GPS | ✅ Básico | ✅ Tiempo Real |
| Alertas de Geocerca | ❌ | ✅ |
| AI Matching (Gemini) | ❌ | ✅ |
| Notificaciones SMS | ❌ | ✅ (Urgencia ALTA) |
| Historial de Rutas | ✅ | ✅ |

### CORS y Autenticación

- **CORS:** Habilitado para todos los orígenes (`*`) en desarrollo
- **Autenticación:** Actualmente en modo permisivo para pruebas
- **Contraseñas:** Encriptadas con BCrypt

**⚠️ Producción:** Configurar orígenes específicos y autenticación JWT

---

## 🧪 Testing

### Probar Endpoints con Postman/Thunder Client

**Colección de Ejemplo:**

1. **Registrar Usuario**
```
POST http://localhost:8080/api/auth/register
Body: { "name": "Test User", "email": "test@test.com", "password": "123456", "plan": "FREE" }
```

2. **Registrar Mascota**
```
POST http://localhost:8080/api/pets/1/register
Body (form-data): 
  - petName: Rex
  - planType: free
  - photo: [archivo]
```

3. **Simular Ubicación GPS**
```
POST http://localhost:8080/api/location/tracker/report
Body: { "petId": 1, "latitude": -12.0464, "longitude": -77.0428, "batteryLevel": 85 }
```

4. **Reportar Mascota Perdida**
```
POST http://localhost:8080/api/pets/lost
Body: { "petId": 1, "description": "Perdido cerca del parque", "hoursLost": 2, "lastSeenLocation": "Parque", "lastSeenLatitude": -12.05, "lastSeenLongitude": -77.04 }
```

5. **Buscar con IA (Solo Premium)**
```
POST http://localhost:8080/api/community/ai-matching/1
Body (form-data): photo: [archivo]
```

---

## 📂 Archivos Importantes

### Configuración
- **application.properties:** Credenciales de BD y APIs
- **SecurityConfig.java:** CORS y autenticación
- **FirebaseConfig.java:** Configuración de Firebase Cloud Messaging

### Servicios Clave
- **AiMatchingService.java:** Lógica de comparación con Gemini
- **NotificationService.java:** Generación y envío de notificaciones Push
- **GeoFenceAlertService.java:** Detección de salida de zonas seguras
- **TrackerService.java:** Gestión de ubicaciones GPS

### Utilidades
- **RoleValidator.java:** Validación de planes Free/Premium
- **DistanceCalculator.java:** Cálculo de distancias con fórmula Haversine

---

## 🐛 Troubleshooting

### Error: "serviceAccountKey.json no encontrado"
**Solución:** Verifica que el archivo esté en `src/main/resources/`

### Error: "Gemini API devuelve 403/429"
**Solución:** Verifica tu API Key y cuotas en Google AI Studio

### Error: "No se guardan las imágenes"
**Solución:** Verifica que exista la carpeta `uploads/` en la raíz del proyecto

### Error: "LazyInitializationException"
**Solución:** Ya corregido con `@JsonIgnoreProperties` en las entidades

### Error: "Token FCM no funciona"
**Solución:** Registra el token con `PUT /api/user/profile/{userId}/fcm-token`

---

## 📝 Notas Adicionales

### Próximas Mejoras Sugeridas
- [ ] Implementar JWT para autenticación segura
- [ ] Agregar WebSockets para actualizaciones en tiempo real
- [ ] Dashboard de administración
- [ ] Integración con pasarela de pagos real (Stripe/MercadoPago)
- [ ] Sistema de roles más granular (Admin/Moderador)
- [ ] Caché con Redis para mejorar rendimiento
- [ ] Rate limiting en endpoints críticos
- [ ] Tests unitarios e integración con JUnit

### Contacto y Soporte
Para reportar bugs o solicitar características, crea un issue en el repositorio.

---

## 📜 Licencia

Este proyecto es parte de un trabajo académico de **Tecsup**.

---

**¡Gracias por usar Zoonet Backend! 🐾**
