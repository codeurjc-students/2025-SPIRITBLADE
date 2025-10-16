#!/bin/bash
# Script to update version numbers in pom.xml, package.json and docker-compose.yml
# Usage: ./update-version.sh <version>
# Example: ./update-version.sh 0.1.0

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 0.1.0"
  exit 1
fi

VERSION=$1
DOCKER_TAG=$(echo $VERSION | sed 's/\.[0-9]*$//')  # 0.1.0 -> 0.1

echo "📝 Updating versions to ${VERSION} (Docker tag: ${DOCKER_TAG})"

# Update backend pom.xml
echo "  Updating backend/pom.xml..."
sed -i "s/<version>.*<\/version>/<version>${VERSION}<\/version>/" backend/pom.xml

# Update frontend package.json
echo "  Updating frontend/package.json..."
sed -i "s/\"version\": \".*\"/\"version\": \"${VERSION}\"/" frontend/package.json

# Update docker-compose.yml
echo "  Updating docker/docker-compose.yml..."
sed -i "s|spiritblade:.*|spiritblade:${DOCKER_TAG}\"|g" docker/docker-compose.yml

echo "✅ Version updated successfully!"
echo ""
echo "Next steps:"
echo "  1. Review changes: git diff"
echo "  2. Commit: git add . && git commit -m 'chore: bump version to ${VERSION}'"
echo "  3. Push: git push origin main"
