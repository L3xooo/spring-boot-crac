#!/usr/bin/env sh
set -eu

APP_JAR=${APP_JAR:-/app/app.jar}
CHECKPOINT_DIR=${CHECKPOINT_DIR:-/checkpoint}
MODE=${MODE:-normal}

mkdir -p "$CHECKPOINT_DIR"

# MODE=checkpoint-only: Spring auto-checkpoints at onRefresh phase, then exits
if [ "$MODE" = "checkpoint-only" ]; then
  java \
    -XX:CRaCCheckpointTo="$CHECKPOINT_DIR" \
    -Dspring.context.checkpoint=onRefresh \
    -jar "$APP_JAR" || EXIT_CODE=$?

  if [ "${EXIT_CODE:-0}" -eq 137 ]; then
    echo "Checkpointing completed successfully."
  else
    echo "Unexpected exit code: ${EXIT_CODE:-0}"
    exit "${EXIT_CODE:-0}"
  fi
  exit 0
fi

# MODE=restore: Resume from checkpoint
if [ "$MODE" = "restore" ]; then
  exec java -XX:CRaCRestoreFrom="$CHECKPOINT_DIR"
fi

# MODE=normal (default): Just run the app normally
exec java -jar "$APP_JAR"
