#!/usr/bin/env bash
set -euo pipefail

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$CURRENT_BRANCH" != "master" ]]; then
    echo "Error: Must be on 'master' branch to use this tool." >&2
    echo "Current branch: $CURRENT_BRANCH" >&2
    exit 1
fi

# Check for local changes
if [[ -n "$(git status --porcelain)" ]]; then
    echo "Error: There are uncommitted local changes." >&2
    git status --short >&2
    exit 1
fi

# Check for remote changes (fetch and compare)
echo "Checking for remote changes..."
REMOTE=$(git remote -v | head -n1 | awk '{print $1}')
if [[ -n "$REMOTE" ]]; then
    git fetch "$REMOTE" >/dev/null 2>&1 || true
    LOCAL=$(git rev-parse HEAD)
    REMOTE_HEAD=$(git rev-parse "${REMOTE}/master" 2>/dev/null) || true
    if [[ -z "$REMOTE_HEAD" ]]; then
        echo "Warning: Could not find master on remote '$REMOTE'." >&2
    elif [[ "$LOCAL" != "$REMOTE_HEAD" ]]; then
        echo "Error: Local and remote 'master' are out of sync. Please update or reset." >&2
        exit 1
    fi
fi

# Find the highest existing release branch matching release/x.y.z
get_latest_release() {
    git for-each-ref refs/heads/release/ --sort=-version:refname \
        --format='%(refname:short)' | grep -E '^release/[0-9]+\.[0-9]+\.[0-9]+$' | head -n1 || true
}

# Extract version components from a release branch name like "release/1.25.10"
parse_version() {
    local branch="$1"
    local ver="${branch#release/}"
    MAJOR="${ver%%.*}"
    ver="${ver#*.}"
    MINOR="${ver%%.*}"
    PATCH="${ver##*.}"
}

echo "=== Release Branch Tool ==="
echo ""

LATEST=$(get_latest_release)

if [[ -z "$LATEST" ]]; then
    echo "No existing release/x.y.z branches found."
    echo "Please create the initial release branch manually (e.g., git branch release/0.1.0)."
    exit 1
fi

echo "Latest release branch: $LATEST"
parse_version "$LATEST"
echo "Current version:       $MAJOR.$MINOR.$PATCH"
echo ""

# Prompt user for release type
select release_type in "point" "minor"; do
    case "$release_type" in
        point)
            NEW_BRANCH="release/$MAJOR.$MINOR.$((PATCH + 1))"
            source_branch="master"
            echo ""
            echo "Creating point release: $NEW_BRANCH (from $source_branch)"
            break
            ;;
        minor)
            NEW_BRANCH="release/$MAJOR.$((MINOR + 1)).0"
            echo ""
            echo "Creating minor release: $NEW_BRANCH (based on master)"
            source_branch="master"
            break
            ;;
    esac
done

echo ""
read -rp "Continue? [Y/n] " confirm
confirm="${confirm,,}"
if [[ "$confirm" == "n" || "$confirm" == "no" ]]; then
    echo "Aborted."
    exit 0
fi

# Create the new branch
git checkout "$source_branch"
git checkout -b "$NEW_BRANCH"

echo ""
echo "Done! New branch '$NEW_BRANCH' created from '$source_branch'."
echo "You can now make your release commits on this branch."
