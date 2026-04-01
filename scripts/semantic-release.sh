#!/usr/bin/env bash
set -euo pipefail

mkdir -p build

latest_tag="$(git tag --list 'v*' --sort=-v:refname | head -n1 || true)"
if [[ -n "${latest_tag}" ]]; then
  range="${latest_tag}..HEAD"
  base_version="${latest_tag#v}"
else
  range=""
  base_version="0.0.0"
fi

records="$(git log ${range:+$range} --reverse --format='%s%x1f%b%x1e')"
if [[ -z "${records}" ]]; then
  echo "VERSION_NAME=${base_version}" > build/semantic-release-version.txt
  echo "RELEASE_NEEDED=false" > build/semantic-release-needed.txt
  printf '# Changelog\n\nAll notable changes to this project will be documented in this file.\n\n## Unreleased\n\n' > build/semantic-release-notes.md
  exit 0
fi

major=0
minor=0
patch=0
breaking=()
features=()
fixes=()

while IFS= read -r -d $'\x1e' record; do
  [[ -z "${record}" ]] && continue
  subject="${record%%$'\x1f'*}"
  body="${record#*$'\x1f'}"
  normalized="$(printf '%s' "${subject}" | tr '[:upper:]' '[:lower:]')"

  if [[ "${subject}" == chore\(release\):* ]]; then
    continue
  fi

  if [[ "${subject}" =~ !: ]] || [[ "${body}" == *"BREAKING CHANGE"* ]]; then
    major=1
    breaking+=("${subject}")
  elif [[ "${normalized}" == feat:* ]]; then
    [[ "${major}" -eq 0 ]] && minor=1
    features+=("${subject}")
  elif [[ "${normalized}" == fix:* ]] || [[ "${normalized}" == perf:* ]]; then
    [[ "${major}" -eq 0 && "${minor}" -eq 0 ]] && patch=1
    fixes+=("${subject}")
  fi
done < <(printf '%s' "${records}")

if [[ "${major}" -eq 0 && "${minor}" -eq 0 && "${patch}" -eq 0 ]]; then
  echo "VERSION_NAME=${base_version}" > build/semantic-release-version.txt
  echo "RELEASE_NEEDED=false" > build/semantic-release-needed.txt
  printf '# Changelog\n\nAll notable changes to this project will be documented in this file.\n\n## Unreleased\n\n' > build/semantic-release-notes.md
  exit 0
fi

IFS='.' read -r major_version minor_version patch_version <<< "${base_version}"
major_version="${major_version:-0}"
minor_version="${minor_version:-0}"
patch_version="${patch_version:-0}"

if [[ "${major}" -eq 1 ]]; then
  major_version=$((major_version + 1))
  minor_version=0
  patch_version=0
elif [[ "${minor}" -eq 1 ]]; then
  minor_version=$((minor_version + 1))
  patch_version=0
else
  patch_version=$((patch_version + 1))
fi

next_version="${major_version}.${minor_version}.${patch_version}"
next_tag="v${next_version}"

tmp_version="$(mktemp)"
awk -v version="${next_version}" '
  BEGIN { found = 0 }
  /^VERSION_NAME=/ {
    print "VERSION_NAME=" version
    found = 1
    next
  }
  { print }
  END {
    if (!found) {
      print "VERSION_NAME=" version
    }
  }
' gradle.properties > "${tmp_version}"
mv "${tmp_version}" gradle.properties

notes_file="build/semantic-release-notes.md"
{
  printf '# Changelog\n\n'
  printf '## %s\n\n' "${next_tag}"
  if [[ "${major}" -eq 1 ]]; then
    printf '### Breaking Changes\n'
    for entry in "${breaking[@]}"; do
      printf '- %s\n' "${entry}"
    done
    printf '\n'
  fi
  if [[ "${minor}" -eq 1 || "${#features[@]}" -gt 0 ]]; then
    printf '### Features\n'
    for entry in "${features[@]}"; do
      printf '- %s\n' "${entry}"
    done
    printf '\n'
  fi
  if [[ "${patch}" -eq 1 || "${#fixes[@]}" -gt 0 ]]; then
    printf '### Fixes\n'
    for entry in "${fixes[@]}"; do
      printf '- %s\n' "${entry}"
    done
    printf '\n'
  fi
  if [[ -f CHANGELOG.md ]]; then
    awk 'NR > 1 { print }' CHANGELOG.md
  fi
} > "${notes_file}.tmp"
mv "${notes_file}.tmp" "${notes_file}"
cp "${notes_file}" CHANGELOG.md

echo "VERSION_NAME=${next_version}" > build/semantic-release-version.txt
echo "RELEASE_NEEDED=true" > build/semantic-release-needed.txt
