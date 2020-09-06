#!/bin/bash
TEST_GUIDELINE=$1
./send-command.sh NONE
sleep 3
adb push  $TEST_GUIDELINE /data/local/tmp/test_guideline.json
PKG=`python3 get_data.py pkg $TEST_GUIDELINE`
APK_PATH=`python3 get_data.py apk $TEST_GUIDELINE`
echo "The app with pkg $PKG is in $APK_PATH"
adb shell pm clear $PKG || adb install -r -t "$APK_PATH"
sleep 1
adb shell monkey -p $PKG 1
./send-command.sh delay_500
sleep 3
