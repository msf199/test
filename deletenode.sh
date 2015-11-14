#!/bin/bash
echo "Y" | gcloud compute instances delete $1 &> deletenode.log