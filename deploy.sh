#!/bin/bash

#remove the directories
ssh peakapi.whitespell.com "sudo rm -rf /usr/share/peak-api/bin";
ssh peakapi.whitespell.com "sudo rm -rf /usr/share/peak-api/lib";

#create the peak api directory if it doesn't already exist
ssh peakapi.whitespell.com "sudo mkdir -p /usr/share/peak-api && sudo chmod -R 777 /usr/share/peak-api";

#create the logging directory if it doesn't exist
ssh peakapi.whitespell.com "sudo mkdir -p /var/log/peak-api && sudo chmod -R 777 /var/log/peak-api";

#remove the bin and lib directories in case they've updated
ssh peakapi.whitespell.com "sudo mkdir /usr/share/peak-api/bin && sudo chmod -R 777 /usr/share/peak-api/bin";
ssh peakapi.whitespell.com "sudo mkdir /usr/share/peak-api/lib && sudo chmod -R 777 /usr/share/peak-api/lib";

#//todo make it zip based
#place the right content in the directories
scp -r bin/* peakapi.whitespell.com:/usr/share/peak-api/bin
scp -r lib/* peakapi.whitespell.com:/usr/share/peak-api/lib
scp run.sh peakapi.whitespell.com:/usr/share/peak-api
scp config.prop peakapi.whitespell.com:/usr/share/peak-api
scp build.sh peakapi.whitespell.com:/usr/share/peak-api

#kill current api, and nohup the new version
ssh peakapi.whitespell.com "cd /usr/share/peak-api && sudo pkill java && sudo nohup bash run.sh bin &";
echo "Deployment is finished";