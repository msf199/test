#!/bin/bash

#remove the directories
ssh peakapi-dev-internal.whitespell.com "sudo rm -rf /usr/share/peak-api/bin";
ssh peakapi-dev-internal.whitespell.com "sudo rm -rf /usr/share/peak-api/lib";

#create the peak api directory if it doesn't already exist
ssh peakapi-dev-internal.whitespell.com "sudo mkdir -p /usr/share/peak-api && sudo chmod -R 777 /usr/share/peak-api";

#create the logging directory if it doesn't exist
ssh peakapi-dev-internal.whitespell.com "sudo mkdir -p /var/log/peak-api && sudo chmod -R 777 /var/log/peak-api";

#remove the bin and lib directories in case they've updated
ssh peakapi-dev-internal.whitespell.com "sudo mkdir /usr/share/peak-api/bin && sudo chmod -R 777 /usr/share/peak-api/bin";
ssh peakapi-dev-internal.whitespell.com "sudo mkdir /usr/share/peak-api/lib && sudo chmod -R 777 /usr/share/peak-api/lib";

#//todo make it zip based
#place the right content in the directories
scp -r bin/* peakapi-dev-internal.whitespell.com:/usr/share/peak-api/bin
scp -r lib/* peakapi-dev-internal.whitespell.com:/usr/share/peak-api/lib
scp run-dev.sh peakapi-dev-internal.whitespell.com:/usr/share/peak-api
scp config-dev.prop peakapi-dev-internal.whitespell.com:/usr/share/peak-api
scp build.sh peakapi-dev-internal.whitespell.com:/usr/share/peak-api

#kill current api, and nohup the new version
echo 'running new api...';
ssh peakapi-dev-internal.whitespell.com "sudo pkill java";
nohup ssh -n -f peakapi-dev-internal.whitespell.com "cd /usr/share/peak-api && sudo nohup bash run-dev.sh bin &" &
echo "Deployment is finished";