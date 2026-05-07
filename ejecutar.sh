#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"
ENV_FILE="$PROJECT_DIR/.env"
DB_DIR="/home/marcos/BBDD/UsuariosTFG"
DB_COMPOSE="$DB_DIR/docker-compose.yml"
DB_CONTAINER="mysql_usuarios_tfg"
BACKEND_PORT=8080
FRONTEND_PORT=5173
PID_DIR="$PROJECT_DIR/.run"
BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"

log() { printf "[INFO] %s\n" "$*"; }
ok() { printf "[OK] %s\n" "$*"; }
warn() { printf "[WARN] %s\n" "$*"; }
error() { printf "[ERROR] %s\n" "$*" >&2; }

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    error "Falta el comando requerido: $1"
    exit 1
  fi
}

liberar_puerto() {
  local port="$1"
  if ! command -v lsof >/dev/null 2>&1; then
    warn "lsof no esta instalado. Instala con: sudo apt install lsof"
    return 0
  fi

  local pids
  pids="$(lsof -ti tcp:"$port" || true)"
  if [[ -n "$pids" ]]; then
    warn "Liberando puerto $port (PIDs: $pids)"
    kill -TERM $pids || true
    sleep 2
    if lsof -ti tcp:"$port" >/dev/null 2>&1; then
      warn "Forzando cierre en puerto $port"
      kill -KILL $pids || true
    fi
  fi
}

stop_pid_file() {
  local pid_file="$1"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file")"
    if [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1; then
      kill -TERM "$pid" || true
      local i
      for i in {1..10}; do
        if ! kill -0 "$pid" >/dev/null 2>&1; then
          break
        fi
        sleep 1
      done
      if kill -0 "$pid" >/dev/null 2>&1; then
        kill -KILL "$pid" || true
      fi
    fi
    rm -f "$pid_file" || true
  fi
}

cleanup_ran=false
apagar_todo() {
  if [[ "$cleanup_ran" == "true" ]]; then
    return 0
  fi
  cleanup_ran=true

  warn "Apagando servicios..."

  stop_pid_file "$FRONTEND_PID_FILE"
  stop_pid_file "$BACKEND_PID_FILE"

  liberar_puerto "$FRONTEND_PORT"
  liberar_puerto "$BACKEND_PORT"

  if [[ -f "$DB_COMPOSE" ]]; then
    docker compose -f "$DB_COMPOSE" down || true
  fi

  ok "Servicios apagados."
}

trap apagar_todo INT TERM EXIT

require_cmd docker
require_cmd npm

if ! docker compose version >/dev/null 2>&1; then
  error "Docker Compose no esta disponible. Instala el plugin docker compose."
  exit 1
fi

if [[ ! -d "$BACKEND_DIR" ]]; then
  error "No existe backend/ en $PROJECT_DIR"
  exit 1
fi
if [[ ! -d "$FRONTEND_DIR" ]]; then
  error "No existe frontend/ en $PROJECT_DIR"
  exit 1
fi
if [[ ! -f "$ENV_FILE" ]]; then
  error "No existe .env en $PROJECT_DIR"
  exit 1
fi
if [[ ! -f "$DB_COMPOSE" ]]; then
  error "No existe $DB_COMPOSE"
  exit 1
fi

mkdir -p "$PID_DIR"

log "Parando servicios anteriores..."
if [[ -f "$DB_COMPOSE" ]]; then
  docker compose -f "$DB_COMPOSE" down || true
fi
liberar_puerto "$BACKEND_PORT"
liberar_puerto "$FRONTEND_PORT"
stop_pid_file "$BACKEND_PID_FILE"
stop_pid_file "$FRONTEND_PID_FILE"

log "Cargando variables de entorno desde .env"
set -a
source "$ENV_FILE"
set +a

log "Levantando MySQL..."
docker compose -f "$DB_COMPOSE" up -d

log "Esperando a MySQL (contenedor $DB_CONTAINER)..."
mysql_ready=false
for i in {1..40}; do
  if docker exec "$DB_CONTAINER" mysqladmin ping \
      -u"${DB_USER:-tfg_user}" -p"${DB_PASSWORD:-tfg_password}" --silent >/dev/null 2>&1; then
    mysql_ready=true
    break
  fi
  sleep 2
  printf "."
  if [[ $i -eq 20 ]]; then
    warn "mysqladmin no responde aun, probando con SELECT 1..."
  fi
  if docker exec "$DB_CONTAINER" mysql \
      -u"${DB_USER:-tfg_user}" -p"${DB_PASSWORD:-tfg_password}" \
      -e "SELECT 1;" "${DB_NAME:-usuarios_tfg}" >/dev/null 2>&1; then
    mysql_ready=true
    break
  fi
done
printf "\n"

if [[ "$mysql_ready" != "true" ]]; then
  error "MySQL no ha arrancado. Mostrando logs..."
  docker logs --tail 200 "$DB_CONTAINER" || true
  exit 1
fi
ok "MySQL listo en localhost:3307"

log "Levantando backend..."
cd "$BACKEND_DIR"
if [[ -x "./mvnw" ]]; then
  chmod +x ./mvnw
  ./mvnw spring-boot:run &
  BACKEND_PID=$!
else
  require_cmd mvn
  mvn spring-boot:run &
  BACKEND_PID=$!
fi
printf "%s" "$BACKEND_PID" > "$BACKEND_PID_FILE"

log "Esperando backend en puerto $BACKEND_PORT..."
backend_ready=false
for i in {1..60}; do
  if command -v curl >/dev/null 2>&1; then
    code="$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:$BACKEND_PORT/actuator/health" || true)"
    if [[ "$code" != "000" ]]; then
      backend_ready=true
      break
    fi
  else
    if (echo > /dev/tcp/localhost/$BACKEND_PORT) >/dev/null 2>&1; then
      backend_ready=true
      break
    fi
  fi
  if ! kill -0 "$BACKEND_PID" >/dev/null 2>&1; then
    error "El backend se ha detenido antes de estar listo."
    exit 1
  fi
  sleep 2
  printf "."
 done
printf "\n"

if [[ "$backend_ready" != "true" ]]; then
  warn "No se pudo validar /actuator/health a tiempo. Continuo si el proceso sigue vivo."
fi

log "Levantando frontend..."
cd "$FRONTEND_DIR"
if [[ ! -d "node_modules" ]]; then
  npm install
fi
npm run dev -- --host 0.0.0.0 --port "$FRONTEND_PORT" --strictPort &
FRONTEND_PID=$!
printf "%s" "$FRONTEND_PID" > "$FRONTEND_PID_FILE"

ok "Servicios levantados:"
log "BBDD:     localhost:3307"
log "Backend:  http://localhost:$BACKEND_PORT"
log "Frontend: http://localhost:$FRONTEND_PORT"
log "Pulsa Ctrl + C para apagar todo"

wait "$BACKEND_PID" "$FRONTEND_PID"
