#!/bin/bash

SITE=104.197.116.113
#remove the directories
ssh $SITE "sudo rm -rf /usr/share/peak-api/bin";
ssh $SITE "sudo rm -rf /usr/share/peak-api/lib";

#create the peak api directory if it doesn't already exist
ssh $SITE "sudo mkdir -p /usr/share/peak-api && sudo chmod -R 777 /usr/share/peak-api";

#create the logging directory if it doesn't exist
ssh $SITE "sudo mkdir -p /var/log/peak-api && sudo chmod -R 777 /var/log/peak-api";

#remove the bin and lib directories in case they've updated
ssh $SITE "sudo mkdir /usr/share/peak-api/bin && sudo chmod -R 777 /usr/share/peak-api/bin";
ssh $SITE "sudo mkdir /usr/share/peak-api/lib && sudo chmod -R 777 /usr/share/peak-api/lib";

#//todo make it zip based
#place the right content in the directories
scp -r bin/* $SITE:/usr/share/peak-api/bin
scp -r lib/* $SITE:/usr/share/peak-api/lib
scp run-dev.sh $SITE:/usr/share/peak-api
scp config-dev.prop $SITE:/usr/share/peak-api
scp build.sh $SITE:/usr/share/peak-api

#kill current api, and nohup the new version
echo 'running new api...';
ssh $SITE "sudo pkill java";
nohup ssh -n -f $SITE "cd /usr/share/peak-api && sudo nohup bash run-dev.sh bin &" &
echo "Deployment is finished";
