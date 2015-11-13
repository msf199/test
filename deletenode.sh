#!/bin/bash
echo "Y" | gcloud compute instances delete $1 --zone us-central1-a &> deletenode.log