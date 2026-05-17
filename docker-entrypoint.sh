#!/usr/bin/env sh
set -eu

APP_JAR=${APP_JAR:-/app/app.jar}
CHECKPOINT_DIR=${CHECKPOINT_DIR:-/checkpoint}
MODE=${MODE:-normal}
STARTUP_WAIT_SECONDS=${STARTUP_WAIT_SECONDS:-15}

mkdir -p "$CHECKPOINT_DIR"

# MODE=checkpoint-only: Start app, wait, create checkpoint, exit
if [ "$MODE" = "checkpoint-only" ]; then
  java -XX:CRaCCheckpointTo="$CHECKPOINT_DIR" -jar "$APP_JAR" &
  APP_PID=$!

  sleep "$STARTUP_WAIT_SECONDS"

  jcmd "$APP_PID" JDK.checkpoint
  wait "$APP_PID" || true

  echo "Checkpoint completed and saved to $CHECKPOINT_DIR"
  exit 0
fi

# MODE=restore: Resume from checkpoint
if [ "$MODE" = "restore" ]; then
  exec java -XX:CRaCRestoreFrom="$CHECKPOINT_DIR"
fi

# MODE=normal (default): Just run the app normally
exec java -jar "$APP_JAR"




