#!/usr/bin/env bash
# commit-specs.sh
# Stages spec artifacts and prompt files, commits with a docs(specs): prefix,
# and pushes to origin on the current branch.
#
# Usage:
#   ./commit-specs.sh
#   ./commit-specs.sh --message "docs(specs): update plan for my-feature"
#   ./commit-specs.sh --dry-run

set -e

SCRIPT_DIR="$(CDPATH="" cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# ── Parse args ────────────────────────────────────────────────────────────────
CUSTOM_MESSAGE=""
DRY_RUN=false

for arg in "$@"; do
    case "$arg" in
        --message=*) CUSTOM_MESSAGE="${arg#--message=}" ;;
        --message)   shift; CUSTOM_MESSAGE="${1:-}" ;;
        --dry-run)   DRY_RUN=true ;;
        --help|-h)
            echo "Usage: $0 [--message <msg>] [--dry-run]"
            echo ""
            echo "  --message <msg>   Custom commit message (overrides default)"
            echo "  --dry-run         Show what would be staged/committed without doing it"
            exit 0
            ;;
    esac
    shift 2>/dev/null || true
done

# ── Resolve repo root and branch ──────────────────────────────────────────────
REPO_ROOT=$(get_repo_root)
CURRENT_BRANCH=$(get_current_branch)
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# ── Build commit message ───────────────────────────────────────────────────────
if [[ -n "$CUSTOM_MESSAGE" ]]; then
    COMMIT_MSG="$CUSTOM_MESSAGE"
else
    COMMIT_MSG="docs(specs): sync spec artifacts [$CURRENT_BRANCH] $TIMESTAMP"
fi

# ── Paths to stage (spec artifacts only — never source code) ──────────────────
STAGE_PATHS=(
    "$REPO_ROOT/.specify/specs/"
    "$REPO_ROOT/.github/prompts/"
)

# ── Dry-run mode ──────────────────────────────────────────────────────────────
if $DRY_RUN; then
    echo "[dry-run] Would stage:"
    for path in "${STAGE_PATHS[@]}"; do
        echo "  $path"
    done
    echo "[dry-run] Commit message: $COMMIT_MSG"
    echo "[dry-run] Push to: origin/$CURRENT_BRANCH"
    exit 0
fi

# ── Require git ───────────────────────────────────────────────────────────────
if ! has_git; then
    echo "ERROR: Not inside a git repository." >&2
    exit 1
fi

cd "$REPO_ROOT"

# ── Stage only spec paths ─────────────────────────────────────────────────────
STAGED_SOMETHING=false
for path in "${STAGE_PATHS[@]}"; do
    if [[ -e "$path" ]]; then
        git add "$path"
        STAGED_SOMETHING=true
    fi
done

if ! $STAGED_SOMETHING; then
    echo "[specify] Nothing to stage — spec paths do not exist yet."
    exit 0
fi

# ── Check if there's actually anything new to commit ─────────────────────────
if git diff --cached --quiet; then
    echo "[specify] No spec changes to commit."
    exit 0
fi

# ── Commit ────────────────────────────────────────────────────────────────────
git commit -m "$COMMIT_MSG"
echo "[specify] Committed: $COMMIT_MSG"

# ── Push ──────────────────────────────────────────────────────────────────────
git push origin "$CURRENT_BRANCH"
echo "[specify] Pushed to origin/$CURRENT_BRANCH"
