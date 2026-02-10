#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <environment> <release-version> <work-dir>" >&2
  exit 1
fi

environment="$1"
release_version="$2"
work_dir="$3"

if [[ "$environment" != "dev" && "$environment" != "staging" && "$environment" != "prod" ]]; then
  echo "environment must be one of: dev, staging, prod" >&2
  exit 1
fi

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
artifact="${repo_root}/dist/${release_version}/saulpos-server-${release_version}.jar"
install_dir="${work_dir}/saulpos-${environment}"

if [[ ! -f "$artifact" ]]; then
  echo "Artifact not found: $artifact" >&2
  echo "Run ops/scripts/package-release.sh ${release_version} first." >&2
  exit 1
fi

mkdir -p "$install_dir"
cp "$artifact" "${install_dir}/saulpos-server.jar"

cat > "${install_dir}/run.sh" <<RUN
#!/usr/bin/env bash
set -euo pipefail
export SPRING_PROFILES_ACTIVE=${environment}
exec java -jar "\$(dirname "\$0")/saulpos-server.jar"
RUN
chmod +x "${install_dir}/run.sh"

echo "Deployment payload prepared in ${install_dir}"
echo "Start command: ${install_dir}/run.sh"
