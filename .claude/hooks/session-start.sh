#!/bin/bash
set -euo pipefail

# Only run this on Claude Code on the web; local users install once and it sticks.
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

# UI/UX Pro Max: 50 UI styles, 97 color palettes, 57 font pairings, searchable via the skill.
claude plugin marketplace add nextlevelbuilder/ui-ux-pro-max-skill || true
claude plugin install ui-ux-pro-max@ui-ux-pro-max-skill --scope project || true
