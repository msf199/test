#!/bin/bash

OUTDIR=$1
ROOT=$(cd $(dirname $0); pwd)
CLASSPATH=$ROOT/lib/*:$ROOT/lib/jetty/*:$ROOT/lib/httpclient/*:$ROOT/lib/log4j/*
MAINCLASS=tests.com.whitespell.peak.Tests

cd $OUTDIR

# Start the server.
java -cp .:$CLASSPATH $MAINCLASS