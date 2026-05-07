# Spotify Tracker (TFG)

Backend de Spotify Tracker para TFG, preparado para crecer sin romper el comportamiento actual.

## Stack
- Java 17, Spring Boot 3, Maven
- MySQL 8.4 (BBDD local UsuariosTFG)
- Flyway (desactivado por defecto para la BBDD local ya creada)
- OAuth2 (Spotify) + JWT
- Docker Compose para entorno local

## Requisitos
- Java 17
- Maven (o Maven Wrapper)
- Docker (para el contenedor MySQL externo)

## Configuracion
- Copia [.env.example](.env.example) a `.env` y rellena las variables.
- Variables clave:
	- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_TIMEZONE`
	- `SPOTIFY_CLIENT_ID`, `SPOTIFY_CLIENT_SECRET`, `SPOTIFY_REDIRECT_URI`
	- `JWT_SECRET`
	- `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS`

## Base de datos local UsuariosTFG (MySQL)
- Levanta la BBDD externa:
	- `cd /home/marcos/BBDD/UsuariosTFG`
	- `docker compose up -d`
- Acceso por terminal:
	- `docker exec -it mysql_usuarios_tfg mysql -u tfg_user -ptfg_password usuarios_tfg`

## Ejecutar backend
- Desde la raiz del proyecto:
	- `set -a`
	- `source .env`
	- `set +a`
	- `cd backend`
	- `./mvnw clean test`
	- `./mvnw spring-boot:run`

## Ejecutar frontend
- Desde [frontend](frontend):
	- `npm run dev`

## Comprobar datos en MySQL
- Usuarios:
	- `SELECT id, spotify_id, display_name, email, created_at FROM usuarios ORDER BY created_at DESC;`
- Estadisticas:
	- `SELECT usuario_id, total_playtime_ms, total_reproducciones, last_sync_at FROM usuario_estadisticas;`
- Reproducciones recientes:
	- `SELECT track_name, artist_names, played_at, duration_ms FROM reproducciones_recientes ORDER BY played_at DESC LIMIT 20;`

## Login con Spotify (resumen)
- El backend usa OAuth2 Authorization Code para Spotify.
- El redirect debe coincidir con `SPOTIFY_REDIRECT_URI` y la configuracion de tu app en Spotify.

## Endpoints principales
- `/api/**` protegido por JWT
- `/api/public/**` publico
- `/swagger-ui.html` para documentacion interactiva
