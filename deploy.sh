#!/bin/bash

#remove the directories
ssh $USER@peakapi.whitespell.com "sudo rm -rf /usr/share/peak-api";

#create the right directories
ssh $USER@peakapi.whitespell.com "sudo mkdir /usr/share/peak-api && sudo chmod -R 777 /usr/share/peak-api";
ssh $USER@peakapi.whitespell.com "sudo mkdir /usr/share/peak-api/bin && sudo chmod -R 777 /usr/share/peak-api/bin";
ssh $USER@peakapi.whitespell.com "sudo mkdir /usr/share/peak-api/lib && sudo chmod -R 777 /usr/share/peak-api/lib";

#//todo make it zip based
#place the right content in the directories
scp -r bin/* $USER@peakapi.whitespell.com:/usr/share/peak-api/bin
scp -r lib/* $USER@peakapi.whitespell.com:/usr/share/peak-api/lib
scp run.sh $USER@peakapi.whitespell.com:/usr/share/peak-api
scp config.prop $USER@peakapi.whitespell.com:/usr/share/peak-api
scp build.sh $USER@peakapi.whitespell.com:/usr/share/peak-api

#kill current api, and nohup the new version
ssh $USER@peakapi.whitespell.com "cd /usr/share/peak-api && pkill java && nohup bash run.sh bin &";