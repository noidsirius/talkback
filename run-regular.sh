#!/bin/bash
TEST_GUIDELINE=$1
./run-common.sh $TEST_GUIDELINE
sleep 2
./send-command.sh init
sleep 1
./send-command.sh mask_context
sleep 1
./send-command.sh executor_regular
sleep 1
./send-command.sh delay_500
sleep 1
./send-command.sh start
./run-post.sh `basename $TEST_GUIDELINE` "regular"
