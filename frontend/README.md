# Spotify Tracker Frontend

Frontend Vue 3 + Vite para el TFG Spotify Tracker.

## Stack

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios

## Estructura principal

```text
src/
├── assets/styles/         # CSS global y variables
├── layouts/               # Layout autenticado y layout público
├── modules/               # Funcionalidades de la app por dominio
├── router/                # Rutas y guards
├── shared/                # Componentes, servicios y utilidades reutilizables
└── stores/                # Stores globales Pinia
```

## Variables de entorno

Copia `.env.example` a `.env` y ajusta la URL del backend si hace falta:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=Spotify Tracker
```

## Ejecutar

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
```

Este frontend está preparado para consumir el backend Spring Boot mediante los endpoints `/api/...` y el flujo OAuth2 de Spotify.
