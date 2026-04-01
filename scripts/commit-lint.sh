#!/usr/bin/env bash
set -euo pipefail

base_sha="${1:-}"
head_sha="${2:-HEAD}"

if [[ -z "${base_sha}" ]]; then
  echo "usage: commit-lint.sh <base-sha> [head-sha]" >&2
  exit 2
fi

allowed_regex='^(feat|fix|perf|docs|refactor|test|build|ci|chore|style|revert)(\([a-z0-9_.-]+\))?(!)?: .+'
release_regex='^chore\(release\): v[0-9]+\.[0-9]+\.[0-9]+$'

mapfile -t commits < <(git log --format='%s' "${base_sha}..${head_sha}")

for subject in "${commits[@]}"; do
  if [[ "${subject}" =~ ^Merge\  ]]; then
    continue
  fi
  if [[ "${subject}" =~ ${allowed_regex} ]]; then
    continue
  fi
  if [[ "${subject}" =~ ${release_regex} ]]; then
    continue
  fi
  echo "Invalid commit message: ${subject}" >&2
  echo "Expected conventional commits like feat(scope): message or chore(release): v1.2.3" >&2
  exit 1
done
