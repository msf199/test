#!/bin/bash
gcloud compute instances create $1 --preemptible --zone $2 --machine-type n1-highcpu-16 --image vp-autoupdate &> $1.log