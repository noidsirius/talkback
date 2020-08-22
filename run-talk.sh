#!/bin/bash
TEST_GUIDELINE=$1
./run-common.sh $TEST_GUIDELINE
sleep 2
./send-command.sh init
sleep 1
#./send-command.sh mask_context_xpath_resourceId
./send-command.sh mask_xpath_resourceId
sleep 1
./send-command.sh executor_talk
sleep 1
./send-command.sh delay_750
sleep 1
./send-command.sh start
./run-post.sh `basename $TEST_GUIDELINE` "talkPP"
