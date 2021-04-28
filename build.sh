#!/bin/bash
mkdir -p build
cd build
echo 'Decompressing dex file...' >&2
unzip -qo ../depixelizer.apk classes.dex
echo 'Converting to jar...' >&2
java -jar ../dex2jar-min.jar -f classes.dex
echo 'Extracting algorithm classes...' >&2
unzip -qo classes-dex2jar.jar 'de/theappguys/depixelizer/algorithm/*'
echo 'Patching polygon class...' >&2
POLY=de/theappguys/depixelizer/algorithm/PixelGraph\$Polygon.class
if ! diff <(echo -ne '\x2a\xb7\0\x2c') <(dd if=$POLY bs=1 skip=758 count=4 status=none)
  then echo 'ERROR: Patching failed, unexpected bytes at 0x2f6' >&2; exit 1
  fi
echo -ne '\x4\0\0\0' | dd of=$POLY bs=1 seek=758 conv=notrunc status=none
echo 'Compiling java application...' >&2
cp ../Depixelizer.java .
javac Depixelizer.java
echo 'Repacking...' >&2
jar cfe ../depix.jar Depixelizer *.class de
echo 'Cleaning up...' >&2
cd ..
rm -r build
echo 'Build finished' >&2

