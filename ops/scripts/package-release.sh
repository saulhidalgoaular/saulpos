#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <release-version>" >&2
  exit 1
fi

release_version="$1"
if [[ ! "$release_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "release-version must be semantic version format (for example 2.0.0)" >&2
  exit 1
fi

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
release_dir="${repo_root}/dist/${release_version}"
target_dir="${repo_root}/pos-server/target"

mkdir -p "$release_dir"

mvn -q -ntp -DskipTests -pl pos-server -am clean package

artifact=""
for candidate in "${target_dir}"/pos-server-*.jar; do
  [[ -f "${candidate}" ]] || continue
  [[ "${candidate}" == *.original ]] && continue
  [[ "${candidate}" == *-sources.jar ]] && continue
  [[ "${candidate}" == *-javadoc.jar ]] && continue
  artifact="${candidate}"
  break
done

if [[ -z "${artifact}" ]]; then
  echo "Unable to locate packaged server JAR in ${target_dir}" >&2
  exit 1
fi

cp "${artifact}" "${release_dir}/saulpos-server-${release_version}.jar"

cat > "${release_dir}/manifest.txt" <<MANIFEST
release.version=${release_version}
artifact=saulpos-server-${release_version}.jar
build.timestamp=$(date -u +%Y-%m-%dT%H:%M:%SZ)
MANIFEST

echo "Release artifact created at ${release_dir}/saulpos-server-${release_version}.jar"
