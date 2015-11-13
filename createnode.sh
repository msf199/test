#!/bin/bash
gcloud compute instances create $1 --preemptible --zone us-central1-a --machine-type n1-highcpu-16 --image vp-autoupdate &> createnode.log