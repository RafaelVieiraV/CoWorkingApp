# CoWorking App 🏢

API REST para la gestión de un espacio de coworking, desarrollada con Spring Boot y MySQL.

## Integrantes
- Jennifer Guerra - front + seguridad
- Ronald Puruncajas - logica spring + test
- Rafael Vieira — Base de Datos + CI/CD

## Tecnologías
- Java 17
- Spring Boot 3.3
- Spring Data JPA
- MySQL 8
- Gradle
- Docker
- GitHub Actions

## Estructura del proyecto
CoWorkingApp/
├── src/
│   ├── main/java/ec/edu/espe/coworkingapp/
│   │   ├── config/        ← Configuración de Spring MVC
│   │   ├── domain/        ← Entidades JPA (Member, Workspace, Booking)
│   │   ├── dto/
│   │   │   ├── request/   ← DTOs de entrada
│   │   │   └── response/  ← DTOs de salida
│   │   ├── repository/    ← Interfaces JpaRepository
│   │   ├── service/       ← Interfaces de servicio
│   │   │   └── impl/      ← Implementaciones
│   │   ├── web/
│   │   │   ├── controller/ ← REST Controllers
│   │   │   └── advice/     ← Manejo global de excepciones
│   │   └── interceptor/   ← Logging de peticiones
│   └── main/resources/
│       └── application.yml
├── .github/workflows/
│   └── ci.yml             ← Pipeline CI/CD
├── init.sql               ← Script de base de datos
└── Dockerfile             ← Imagen Docker

## Endpoints

### Members
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/members` | Listar todos los miembros |
| GET | `/api/members/{id}` | Obtener miembro por ID |
| POST | `/api/members` | Crear miembro |
| PUT | `/api/members/{id}` | Actualizar miembro |
| DELETE | `/api/members/{id}` | Eliminar miembro |

### Workspaces
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/workspaces` | Listar todos los espacios |
| GET | `/api/workspaces/available` | Listar espacios disponibles |
| GET | `/api/workspaces/{id}` | Obtener espacio por ID |
| POST | `/api/workspaces` | Crear espacio |
| PUT | `/api/workspaces/{id}` | Actualizar espacio |
| DELETE | `/api/workspaces/{id}` | Eliminar espacio |

### Bookings
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/bookings` | Listar todas las reservas |
| GET | `/api/bookings/{id}` | Obtener reserva por ID |
| POST | `/api/bookings` | Crear reserva |
| PATCH | `/api/bookings/{id}/cancel` | Cancelar reserva |
| GET | `/api/bookings/member/{memberId}` | Reservas por miembro |

## Cómo correr el proyecto

### Requisitos
- Java 17
- MySQL 8
- Gradle

### Configuración
1. Crear la base de datos:
```sql
CREATE DATABASE coworking_db;
```

2. Configurar credenciales en `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: tu_usuario
    password: tu_contraseña
```

3. Ejecutar la aplicación:
```bash
./gradlew bootRun
```

4. La API estará disponible en `http://localhost:8080`

### Con Docker
```bash
docker build -t coworking-app .
docker run -p 8080:8080 coworking-app
```

## CI/CD
El pipeline de GitHub Actions se ejecuta automáticamente en cada push y:
1. Compila el proyecto
2. Ejecuta los tests con MySQL
3. Construye la imagen Docker (solo en `main` y `develop`)