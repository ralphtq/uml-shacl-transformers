#!/usr/bin/env python3
import os
from argparse import ArgumentParser
from pathlib import Path

CLSX_DIR_PATH_DEFAULT = (
    Path(__file__).parent.parent
    / "module"
    / "lib"
    / "src"
    / "test"
    / "resources"
    / "clsx"
).absolute()

PREAMBLE = """\
<?xml version="1.0" encoding="UTF-8"?>
<RhapsodyArchive>
	<MagicNumber></MagicNumber>
	<CODE-PAGE>1252</CODE-PAGE>
	<version>109.0</version>
	<lang>C++</lang>
	<BuildNo>202002261110</BuildNo>
	<RMMMinimumClientVersion>100.0</RMMMinimumClientVersion>
	
	<RHAPSODY-MODEL>
"""

POSTAMBLE = """\
	</RHAPSODY-MODEL>
	<OslcLinks type="c">
		<IRPYRawContainer type="e">
		</IRPYRawContainer>
	</OslcLinks>

<INCLUDES-RHAPSODY-INDEX>false</INCLUDES-RHAPSODY-INDEX>
</RhapsodyArchive>

"""

assert __name__ == "__main__"

argument_parser = ArgumentParser()
argument_parser.add_argument(
    "-o",
    dest="output_file_path",
    help="path to an output CLSX file",
    default=(CLSX_DIR_PATH_DEFAULT / "Union.clsx"),
)
argument_parser.add_argument(
    "-i",
    dest="input_dir_path",
    help="path to a directory full of .clsx files",
    default=CLSX_DIR_PATH_DEFAULT,
)
args = argument_parser.parse_args()
input_dir_path = Path(args.input_dir_path).absolute()
output_file_path = Path(args.output_file_path).absolute()

clsx_union = []
for file_name in os.listdir(input_dir_path):
    if os.path.splitext(file_name)[1] != ".clsx":
        continue
    input_clsx_file_path = input_dir_path / file_name
    if input_clsx_file_path == output_file_path:
        continue
    with open(input_clsx_file_path, "r", encoding="utf-8") as clsx_file:
        clsx = clsx_file.read()
    assert clsx.startswith(PREAMBLE), input_clsx_file_path
    assert clsx.endswith(POSTAMBLE), input_clsx_file_path
    clsx_union.append(clsx[len(PREAMBLE) : -len(POSTAMBLE)])

with open(output_file_path, "w+", encoding="utf-8") as clsx_file:
    clsx_file.write(PREAMBLE + "\n".join(clsx_union) + POSTAMBLE)
