# Spotify Tracker (TFG)

Backend de Spotify Tracker para TFG, preparado para crecer sin romper el comportamiento actual.

## Stack
- Java 17, Spring Boot 3, Maven
- PostgreSQL + Flyway
- OAuth2 (Spotify) + JWT
- Docker Compose para entorno local

## Requisitos
- Java 17
- Maven (o Maven Wrapper)
- PostgreSQL 16+ (local o Docker)

## Configuracion
- Copia [.env.example](.env.example) a `.env` y rellena las variables.
- Variables clave:
	- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
	- `SPOTIFY_CLIENT_ID`, `SPOTIFY_CLIENT_SECRET`, `SPOTIFY_REDIRECT_URI`
	- `JWT_SECRET`
	- `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS`

## Base de datos con Docker (opcional)
- Levanta solo PostgreSQL:
	- `docker compose up -d postgres`
- El contenedor expone el puerto `5433` en tu host (ajusta `DB_PORT=5433` si conectas desde tu maquina).

## Ejecutar backend
- Desde [backend](backend):
	- `./mvnw clean test`
	- `./mvnw spring-boot:run`

## Login con Spotify (resumen)
- El backend usa OAuth2 Authorization Code para Spotify.
- El redirect debe coincidir con `SPOTIFY_REDIRECT_URI` y la configuracion de tu app en Spotify.

## Endpoints principales
- `/api/**` protegido por JWT
- `/api/public/**` publico
- `/swagger-ui.html` para documentacion interactiva
