import itertools
import json
import os
import re
import subprocess
import sys
from pathlib import Path
from typing import NoReturn

EXTENSION_REGEX = re.compile(r"^src/(?P<lang>\w+)/(?P<extension>\w+)")
MODULE_REGEX = re.compile(r"^:src:(?P<lang>\w+):(?P<extension>\w+)$")
BASE_FILES_REGEX = re.compile(
    r"^(buildSrc/|base/|gradle/|build\.gradle\.kts|common\.gradle|gradle\.properties|settings\.gradle\.kts)"
)


def run_command(cmd):
    result = subprocess.run(cmd, capture_output=True, text=True, shell=True)

    if result.returncode != 0:
        print(result.stderr.strip())
        sys.exit(result.returncode)

    return result.stdout.strip()


def get_module_list(ref: str) -> tuple[list[str], list[str]]:
    changed_files = run_command(f"git diff --name-only {ref}").splitlines()

    modules = set()
    libs = set()
    deleted = set()

    for file in map(lambda x: Path(x).as_posix(), changed_files):
        if BASE_FILES_REGEX.search(file):
            return get_all_modules()
        if match := EXTENSION_REGEX.search(file):
            lang = match.group("lang")
            extension = match.group("extension")
            if Path("src", lang, extension).is_dir():
                modules.add(f":src:{lang}:{extension}")
            deleted.add(f"{lang}.{extension}")

    def is_extension_module(module: str) -> bool:
        if not (match := MODULE_REGEX.search(module)):
            return False
        lang = match.group("lang")
        extension = match.group("extension")
        deleted.add(f"{lang}.{extension}")
        return True

    if len(libs) != 0:
        modules.update(
            [
                module
                for module in run_command("./gradlew -q " + " ".join(libs)).splitlines()
                if is_extension_module(module)
            ]
        )

    if os.getenv("IS_PR_CHECK") != "true":
        with Path(".github/always_build.json").open() as always_build_file:
            always_build = json.load(always_build_file)
        for extension in always_build:
            modules.add(":src:" + extension.replace(".", ":"))
            deleted.add(extension)

    return list(modules), list(deleted)


def get_all_modules() -> tuple[list[str], list[str]]:
    modules = []
    deleted = []
    for lang in Path("src").iterdir():
        for extension in lang.iterdir():
            modules.append(f":src:{lang.name}:{extension.name}")
            deleted.append(f"{lang.name}.{extension.name}")
    return modules, deleted


def main() -> NoReturn:
    _, ref, build_type = sys.argv
    modules, deleted = get_module_list(ref)

    chunked = {
        "chunk": [
            {"number": i + 1, "modules": modules}
            for i, modules in enumerate(
                itertools.batched(
                    map(lambda x: f"{x}:assemble{build_type}", modules),
                    int(os.getenv("CI_CHUNK_SIZE", 65)),
                )
            )
        ]
    }

    print(
        f"Module chunks to build:\n{json.dumps(chunked, indent=2)}\n\nModule to delete:\n{json.dumps(deleted, indent=2)}"
    )

    if os.getenv("CI") == "true":
        with open(os.getenv("GITHUB_OUTPUT"), "a") as out_file:
            out_file.write(f"matrix={json.dumps(chunked)}\n")
            out_file.write(f"delete={json.dumps(deleted)}\n")


if __name__ == "__main__":
    main()
