#!/bin/bash

OUTDIR=$1
ROOT=$(cd $(dirname $0); pwd)
CLASSPATH=$ROOT/lib/*:$ROOT/lib/jetty/*:$ROOT/lib/log4j/*
MAINCLASS=whitespell.Server

if [ -z "$OUTDIR" ]; then
  echo "Usage: $0 <output-directory>"
  echo "    Builds and runs the server in <output-directory>."
  exit 1
fi

# Compile the server.
$ROOT/build.sh $OUTDIR || exit 1
cd $OUTDIR

# Start the server.
java -cp .:$CLASSPATH $MAINCLASS
