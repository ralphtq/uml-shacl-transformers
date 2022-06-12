#!/usr/bin/env python3
import os.path
import subprocess
from pathlib import Path

CLSX_DIR_PATH = (
    Path(__file__).parent.parent
    / "module"
    / "lib"
    / "src"
    / "test"
    / "resources"
    / "clsx"
).absolute()
assert CLSX_DIR_PATH.is_dir(), CLSX_DIR_PATH

XSD_DIR_PATH = (
    Path(__file__).parent.parent
    / "module"
    / "lib"
    / "src"
    / "main"
    / "resources"
    / "xsd"
).absolute()
assert XSD_DIR_PATH.is_dir(), XSD_DIR_PATH

for file_name in os.listdir(CLSX_DIR_PATH):
    file_base_name, file_ext = os.path.splitext(file_name)
    if file_ext != ".clsx":
        continue
    clsx_file_path = CLSX_DIR_PATH / file_name
    for xsd_file_base_name in (
        file_base_name,
        "Union",
    ):
        xsd_file_path = XSD_DIR_PATH / (xsd_file_base_name + ".xsd")
        if not xsd_file_path.is_file():
            continue
        # print(clsx_file_path, "against", xsd_file_path)
        subprocess.check_call(
            ["xmllint", "--noout", "--schema", str(xsd_file_path), str(clsx_file_path)]
        )
        # print()
