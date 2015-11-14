#!/bin/bash
echo "Y" | gcloud compute instances delete $1 --zone $2 &> deletenode.log