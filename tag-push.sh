#!/usr/bin/env bash
set -euo pipefail

# Ask for version
read -rp "Enter version (major.minor.patch): " VERSION

# Validate version format
if [[ ! "$VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    echo "Error: Invalid version format."
    echo "Expected format: major.minor.patch (e.g. 2.3.3)"
    exit 1
fi

MAJOR="${BASH_REMATCH[1]}"
MINOR="${BASH_REMATCH[2]}"
PATCH="${BASH_REMATCH[3]}"

echo "Major: $MAJOR"
echo "Minor: $MINOR"
echo "Patch: $PATCH"

# Ask for commit message
read -rp "Commit message [Release v-$VERSION]: " COMMIT_MSG
COMMIT_MSG="${COMMIT_MSG:-Release v-$VERSION}"

TAG="v$VERSION"

# Commit if there are changes
if ! git diff --quiet || ! git diff --cached --quiet; then
    git add .
    git commit -m "$COMMIT_MSG"
else
    echo "No changes to commit."
fi

# Ensure tag does not already exist
if git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "Error: Tag $TAG already exists."
    exit 1
fi

# Create annotated tag
git tag -a "$TAG" -m "$COMMIT_MSG"

# Push branch and tag
git push
git push origin "$TAG"

echo "Successfully released $TAG"