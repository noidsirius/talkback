#!/bin/bash
TEST_NAME=$1
TAG=$2
adb shell screenrecord /data/local/tmp/clip.mp4 &
VIDEO_PID=$!
while [ ! -f my_result.txt ]
do
  echo "Wait.."
  sleep 2 # or less like 0.2
  adb exec-out run-as com.google.android.accessibility.talkback cat files/test_result.txt > my_result.txt
 cat my_result.txt | grep -q "No such file or directory" && rm my_result.txt
done
sleep 1
echo "Stop recording " $VIDEO_PID
echo "kill -INT $VIDEO_PID"
kill -INT $VIDEO_PID
sleep 1
kill -9 $VIDEO_PID
echo "-----"
sleep 1
cat my_result.txt
TDIR=results/$TAG
mkdir -p $TDIR
cp my_result.txt $TDIR/$TEST_NAME.txt
rm my_result.txt
sleep 1
adb pull /data/local/tmp/clip.mp4
mv clip.mp4 $TDIR/$TEST_NAME.mp4
