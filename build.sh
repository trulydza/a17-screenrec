#!/usr/bin/env bash
set -euo pipefail

SDK="$HOME/Android"
BT="$SDK/build-tools/35.0.0"
PLATFORM="$SDK/platforms/android-35/android.jar"
XPOSED="$SDK/xposed/api-82.jar"
KS="$SDK/debug.jks"
NAME="a17-screenrec"

cd "$(dirname "$0")"
rm -rf build
mkdir -p build/classes build/res

echo "== compile resources =="
"$BT/aapt2" compile --dir res -o build/res.zip

echo "== link =="
"$BT/aapt2" link -o build/unsigned.apk \
    -I "$PLATFORM" \
    --manifest AndroidManifest.xml \
    -R build/res.zip --auto-add-overlay \
    --min-sdk-version 29 --target-sdk-version 35

echo "== javac =="
find src -name "*.java" > build/sources.txt
javac -source 8 -target 8 -nowarn \
    -bootclasspath "$PLATFORM" \
    -classpath "$XPOSED" \
    -d build/classes \
    @build/sources.txt 2>&1 | grep -v "^warning:" || true

echo "== d8 =="
mkdir -p build/dex
find build/classes -name "*.class" > build/classes.txt
"$BT/d8" --release --min-api 29 --lib "$PLATFORM" --output build/dex $(cat build/classes.txt)

echo "== package =="
cd build
cp ../assets/xposed_init xposed_init
cp dex/classes.dex classes.dex
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:
    z.write('classes.dex','classes.dex')
    z.write('xposed_init','assets/xposed_init')
PY
cd ..

echo "== align + sign =="
"$BT/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
"$BT/apksigner" sign --ks "$KS" --ks-key-alias k \
    --ks-pass pass:android --key-pass pass:android \
    --out "build/$NAME.apk" build/aligned.apk

ls -l "build/$NAME.apk"
