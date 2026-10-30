#!/usr/bin/env bash
set -euo pipefail

# Xvfb supplies a display, but native minimize/restore events require a window manager.
openbox > /tmp/osps-openbox.log 2>&1 &
window_manager_pid=$!
trap 'kill "$window_manager_pid" 2>/dev/null || true' EXIT

for attempt in {1..50}; do
    if xprop -root _NET_SUPPORTING_WM_CHECK 2>/dev/null | grep -q 'window id'; then
        ./gradlew check coverage
        exit 0
    fi
    sleep 0.1
done

echo "Openbox did not become ready for the JavaFX tests" >&2
cat /tmp/osps-openbox.log >&2
exit 1
