# Use git diff to find all files that have been added/modified since the last commit.
# and then build a list of modules that need to be built.

function join_by {
  local d=${1-} f=${2-}
  if shift 2; then
    printf %s "$f" "${@/#/$d}"
  fi
}

# Get the list of modified files
mapfile -t MODIFIED_FILES < <(git diff --name-status HEAD~1..HEAD)

tasks=()

for file in "${MODIFIED_FILES[@]}"; do
  # Get the git modifier (A, M, D, etc.)
  # modifier=$(echo "$file" | cut -d$'\t' -f1)
  filepath=$(echo "$file" | cut -d$'\t' -f2)

  # Check if the filepath starts with "src/"
  if [[ "$filepath" == src/* ]]; then
    # src/en/novelfull/...
    language=$(echo "$filepath" | cut -d'/' -f2)
    source=$(echo "$filepath" | cut -d'/' -f3)

    # Add the task to the list in double quotes
    tasks+=("\":src:$language:$source:assembleRelease\"")
  fi
done

echo "[$(join_by , "${tasks[@]}")]"


